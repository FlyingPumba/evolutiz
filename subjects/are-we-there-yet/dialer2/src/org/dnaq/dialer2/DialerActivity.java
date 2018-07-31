package org.dnaq.dialer2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class DialerActivity extends Activity implements OnClickListener, OnLongClickListener, OnItemClickListener, OnItemLongClickListener, OnSharedPreferenceChangeListener, TextWatcher, FilterQueryProvider {
    public static final String PREFERENCE_VIBRATION_LENGTH = "vibration_length";
    public static final String PREFERENCE_SPEED_DIAL_SLOT = "speedDialSlot";
    public static final String PREFERENCE_SENSOR_ROTATION = "sensor_rotation";
    public static final String PREFERENCE_T9_MATCH_START_OF_NAMES_ONLY = "t9_match_start_of_names_only";
    public static final String PREFERENCE_T9_SORT_BY_TIMES_CONTACTED = "t9_sort_by_times_contacted";
    public static final String PREFERENCE_T9_MATCH_PHONE_NUMBERS = "t9_match_phone_numbers";
    public static final String PREFERENCE_FAVORITE_CONTACTS_FIRST = "favorite_contacts_first";
    public static final String PREFERENCE_SHOW_CALL_COUNTER = "show_call_counter";
    
    public static final String DEFAULT_VIBRATION_LENGTH = "25";
    
    
    public static final int ACTIVITY_CONTACT_VIEW_DIALOG = 1;
    public static final int ACTIVITY_SELECT_NUMBER_DIALOG = 2;
    public static final int ACTIVITY_SELECT_SPEED_DIAL_SLOT_DIALOG = 3;
    
    private QueryHandler mQueryHandler;
    private ViewHolder mViewHolder;
    private AsyncContactImageLoader mAsyncContactImageLoader;
    private CallLogAdapter mCallLogAdapter;
    private Cursor mContactsCursor;
    private ContactsAdapter mContactsAdapter;
    private boolean mInCallLogMode;
    private boolean mDialerEnabled;
    private int mVibrationLength;
    private GestureDetector mGestureDetector;
    private boolean mT9MatchStartOfNamesOnly;
    private boolean mT9OrderByTimesContacted;
    private boolean mFavoriteContactsFirst;
    private boolean mT9MatchPhoneNumbers;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mQueryHandler = new QueryHandler(getContentResolver());
        mViewHolder = new ViewHolder();
        mContactsCursor = null;
        mAsyncContactImageLoader = new AsyncContactImageLoader(getApplicationContext(), getResources().getDrawable(R.drawable.default_icon));
        mCallLogAdapter = new CallLogAdapter(getApplicationContext(), null, mAsyncContactImageLoader);
        mContactsAdapter = new ContactsAdapter(getApplicationContext(), mContactsCursor, mAsyncContactImageLoader);
        mContactsAdapter.setFilterQueryProvider(this);
        mInCallLogMode = true;
        mDialerEnabled = true;
        
        mGestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
        	@Override
        	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        		if (event2.getY() < event1.getY() && 
        				mDialerEnabled && 
        				getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
        			disableDialer();
        		}
        		return false;
        	}
        });
        
        mT9MatchStartOfNamesOnly = false;
        mT9OrderByTimesContacted = false;
        
        prepareListeners();
        mViewHolder.phoneNumber.setInputType(0);
        mViewHolder.phoneNumber.addTextChangedListener(this);
        mViewHolder.phoneNumber.requestFocus();
        mViewHolder.contactList.setAdapter(mCallLogAdapter);
        
        mQueryHandler.getCallLog();
        
        	
        parseIntent(getIntent());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(this);
        applySettings(preferences);
        if (mInCallLogMode) {
            new Thread() {
                public void run() {
                    clearMissedCalls();
                    // we don't have permission to clear the call log on Gingerbread
                    if (Build.VERSION.SDK_INT != 9) {
                        clearMissedCallsNotification();
                    }
                    updateMissedCalls();
                }
            }.start();
        }
    }
    
    // parse the incoming intent and take specific action if needed
    private void parseIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())
                || Intent.ACTION_DIAL.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String scheme = data.getScheme();
                if (scheme != null && scheme.equals("tel")) {
                    String number = data.getSchemeSpecificPart();
                    if (number != null) {
                        dialNumber(number);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_item_show_hide)
            .setTitle(mDialerEnabled ? R.string.hide_dialer : R.string.show_dialer);
        menu.findItem(R.id.menu_item_contact_list).setVisible(mInCallLogMode).setEnabled(mInCallLogMode);
        menu.findItem(R.id.menu_item_call_log).setVisible(!mInCallLogMode).setEnabled(!mInCallLogMode);
        menu.findItem(R.id.menu_item_clear_call_log)
            .setVisible(mInCallLogMode)
            .setEnabled(mInCallLogMode);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_item_show_hide:
            toggleDialer();
            break;
        case R.id.menu_item_contact_list:
        	mContactsAdapter.getFilter().filter(mViewHolder.phoneNumber.getText());
            switchToContactListMode();
            break;
        case R.id.menu_item_call_log:
        	switchToCallLogMode();
        	break;
        case R.id.menu_item_clear_call_log:
            askToClearCallLog();
            break;
        case R.id.menu_item_settings:
            Intent intent = new Intent(getApplicationContext(), DialerPreferenceActivity.class);
            startActivity(intent);
            break;
        }
        return true;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_CALL || keyCode == KeyEvent.KEYCODE_ENTER) 
        		&& event.getRepeatCount() == 0 
        		&& mDialerEnabled) {
            callNumberAndFinish(mViewHolder.phoneNumber.getText());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void prepareListeners() {
    	mViewHolder.button0.setOnLongClickListener(this);
    	mViewHolder.button1.setOnLongClickListener(this);
    	mViewHolder.button2.setOnLongClickListener(this);
    	mViewHolder.button3.setOnLongClickListener(this);
    	mViewHolder.button4.setOnLongClickListener(this);
    	mViewHolder.button5.setOnLongClickListener(this);
    	mViewHolder.button6.setOnLongClickListener(this);
    	mViewHolder.button7.setOnLongClickListener(this);
    	mViewHolder.button8.setOnLongClickListener(this);
    	mViewHolder.button9.setOnLongClickListener(this);
        mViewHolder.buttonDelete.setOnLongClickListener(this);
        mViewHolder.contactList.setOnItemClickListener(this);
        mViewHolder.contactList.setOnItemLongClickListener(this);
        mViewHolder.dialerView.setOnTouchListener(new OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mViewHolder.contactList.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (mGestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		});
        mViewHolder.phoneNumber.setOnClickListener(this);
    }

    @Override
	public void onClick(View v) {
		final Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		new Thread() {
			@Override
			public void run() {
				vibrator.vibrate(mVibrationLength);
			}
		}.start();
		
		
		
		switch(v.getId()) {
		case R.id.Button0:
			onCharacterPressed('0');
			break;
		case R.id.Button1:
		    onCharacterPressed('1');
			break;
		case R.id.Button2:
		    onCharacterPressed('2');
			break;
		case R.id.Button3:
		    onCharacterPressed('3');
			break;
		case R.id.Button4:
		    onCharacterPressed('4');
			break;
		case R.id.Button5:
		    onCharacterPressed('5');
			break;
		case R.id.Button6:
		    onCharacterPressed('6');
			break;
		case R.id.Button7:
		    onCharacterPressed('7');
			break;
		case R.id.Button8:
		    onCharacterPressed('8');
			break;
		case R.id.Button9:
		    onCharacterPressed('9');
			break;
		case R.id.ButtonStar:
		    onCharacterPressed('*');
			break;
		case R.id.ButtonHash:
		    onCharacterPressed('#');
			break;
		case R.id.ButtonDelete:
		    onDeletePressed();
			break;
		case R.id.ButtonCall:
		    if (mViewHolder.phoneNumber.getText().length() != 0) {
		        callNumberAndFinish(mViewHolder.phoneNumber.getText());
		    }
			break;
		case R.id.DialerExpandButton:
			enableDialer();
			break;
		case R.id.ButtonContract:
		    disableDialer();
		    break;
		case R.id.EditTextPhoneNumber:
		    mViewHolder.phoneNumber.setCursorVisible(true);
		    break;
		}
	}

    private void onDeletePressed() {
        CharSequence cur = mViewHolder.phoneNumber.getText();
        int start = mViewHolder.phoneNumber.getSelectionStart();
        int end = mViewHolder.phoneNumber.getSelectionEnd();
        if (start == end) { // remove the item behind the cursor
            if (start != 0) {
                cur = cur.subSequence(0, start-1).toString() + cur.subSequence(start, cur.length()).toString();
                mViewHolder.phoneNumber.setText(cur);
                mViewHolder.phoneNumber.setSelection(start-1);
                if (cur.length() == 0) {
                    mViewHolder.phoneNumber.setCursorVisible(false);
                }
            }
        } else { // remove the whole selection
            cur = cur.subSequence(0, start).toString() + cur.subSequence(end, cur.length()).toString();
            mViewHolder.phoneNumber.setText(cur);
            mViewHolder.phoneNumber.setSelection(end - (end - start));
            if (cur.length() == 0) {
                mViewHolder.phoneNumber.setCursorVisible(false);
            }
        }
    }
    
    private void onCharacterPressed(char digit) {
        CharSequence cur = mViewHolder.phoneNumber.getText();
	
        int start = mViewHolder.phoneNumber.getSelectionStart();
		int end = mViewHolder.phoneNumber.getSelectionEnd();
		int len = cur.length();


		if (cur.length() == 0) {
		    mViewHolder.phoneNumber.setCursorVisible(false);
		}
		
		cur = cur.subSequence(0, start).toString() + digit + cur.subSequence(end, len).toString();
		mViewHolder.phoneNumber.setText(cur);
		mViewHolder.phoneNumber.setSelection(start+1);
    }

    @Override
    public boolean onLongClick(View view) {
        CharSequence cur = mViewHolder.phoneNumber.getText();
        int start = mViewHolder.phoneNumber.getSelectionStart();
        int end = mViewHolder.phoneNumber.getSelectionEnd();

        switch(view.getId()) {
		case R.id.Button0:
		    mViewHolder.phoneNumber.setText(cur.subSequence(0, start) + "+" + cur.subSequence(end, cur.length()));
		    mViewHolder.phoneNumber.setSelection(start+1);
			return true;
		case R.id.ButtonDelete:
		    cur = cur.subSequence(end, cur.length());
		    mViewHolder.phoneNumber.setText(cur);
		    if (cur.length() == 0) {
		        mViewHolder.phoneNumber.setCursorVisible(false);
		    }
			return true;
		case R.id.ButtonCall:
			return false;
		case R.id.ButtonHash:
			return false;
		case R.id.Button1:
			return trySpeedDial(1);
		case R.id.Button2:
			return trySpeedDial(2);
		case R.id.Button3:
			return trySpeedDial(3);
		case R.id.Button4:
			return trySpeedDial(4);
		case R.id.Button5:
			return trySpeedDial(5);
		case R.id.Button6:
			return trySpeedDial(6);
		case R.id.Button7:
			return trySpeedDial(7);
		case R.id.Button8:
			return trySpeedDial(8);
		case R.id.Button9:
			return trySpeedDial(9);
		default:
			break;
		}
		return false;
    }

    private void applySettings(SharedPreferences preferences) {
        mVibrationLength = Integer.parseInt(preferences.getString(PREFERENCE_VIBRATION_LENGTH, DEFAULT_VIBRATION_LENGTH));
        
        if (!preferences.getBoolean(PREFERENCE_SENSOR_ROTATION, true)) {  // or user wants only portrait mode
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        mT9MatchStartOfNamesOnly = preferences.getBoolean(PREFERENCE_T9_MATCH_START_OF_NAMES_ONLY, true);
        mT9OrderByTimesContacted = preferences.getBoolean(PREFERENCE_T9_SORT_BY_TIMES_CONTACTED, true);
        mContactsAdapter.setShowCallCounter(preferences.getBoolean(PREFERENCE_SHOW_CALL_COUNTER, false));
        mFavoriteContactsFirst = preferences.getBoolean(PREFERENCE_FAVORITE_CONTACTS_FIRST, false);
        mT9MatchPhoneNumbers = preferences.getBoolean(PREFERENCE_T9_MATCH_PHONE_NUMBERS, false);
        
        CharSequence s = mViewHolder.phoneNumber.getText(); 
        if (!mInCallLogMode || s.length() > 0) {
        	mContactsAdapter.getFilter().filter(s);
        	switchToContactListMode();
        }
        
        
    }
    
    private boolean trySpeedDial(int speedDialSlot) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    	String phoneNumber = preferences.getString(PREFERENCE_SPEED_DIAL_SLOT+speedDialSlot, null);
    	if (phoneNumber == null) {
    		return false;
    	}
        Uri uri = Uri.parse("tel:"+phoneNumber);
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        startActivity(intent);
        finish();
        return true;
    }

    private void toggleDialer() {
        if (mDialerEnabled) {
            disableDialer();
        } else {
            enableDialer();
        }
    }
    private void disableDialer() {
        mDialerEnabled = false;
        mViewHolder.dialerView.setVisibility(View.GONE);
        mViewHolder.dialerExpandMenu.setVisibility(View.VISIBLE);
        mViewHolder.phoneNumber.setSelected(false);
        mViewHolder.phoneNumber.setCursorVisible(false);
	}
	
	private void enableDialer() {
	    mDialerEnabled = true;
	    mViewHolder.dialerView.setVisibility(View.VISIBLE);
	    mViewHolder.dialerExpandMenu.setVisibility(View.GONE);
	    mViewHolder.phoneNumber.requestFocus();
	}



    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
            long id) {
        if (mInCallLogMode) {
            Cursor c = (Cursor)mCallLogAdapter.getItem(position);
            String phoneNumber = c.getString(CallLogAdapter.COLUMN_NUMBER);
            if (!TextUtils.isEmpty(phoneNumber) && !TextUtils.equals("-1", phoneNumber)) {
                mQueryHandler.showContactForNumber(phoneNumber);
            }
        } else {
        	Cursor c = (Cursor)mContactsAdapter.getItem(position);
        	mQueryHandler.showContactForLookupKey(c.getString(ContactsAdapter.COLUMN_LOOKUP_KEY));
        }
        return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case ACTIVITY_CONTACT_VIEW_DIALOG:
            onContactViewDialogResult(resultCode, data);
            break;
            
        case ACTIVITY_SELECT_NUMBER_DIALOG:
            onSelectNumberDialogResult(resultCode, data);
            break;

        case ACTIVITY_SELECT_SPEED_DIAL_SLOT_DIALOG:
            onSelectSpeedDialSlotDialogResult(resultCode, data);
            break;
            
        default:
            break;
        }
    }

    private void onSelectSpeedDialSlotDialogResult(int resultCode, Intent data) {
        if (resultCode == SelectSpeedDialSlotDialog.RESULT_OK) {
            String phoneNumber = data.getStringExtra(SelectSpeedDialSlotDialog.PHONE_NUMBER);
            int slot = data.getIntExtra(SelectSpeedDialSlotDialog.SPEED_DIAL_SLOT, -1);
            if (slot != -1) {
                setSpeedDial(phoneNumber, slot);
            }
        }
    }

    private void onSelectNumberDialogResult(int resultCode, Intent data) {
        if (resultCode == SelectNumberDialog.RESULT_OK) {
            String phoneNumber = data.getStringExtra(SelectNumberDialog.PHONE_NUMBER);
            selectSpeedDialSlot(phoneNumber);
        }
    }

    private void onContactViewDialogResult(int resultCode, Intent data) {
        switch (resultCode) {
        case ContactViewDialog.RESULT_VIEW_CONTACT:
        {
            String lookupKey = data.getStringExtra(ContactViewDialog.LOOKUP_KEY);
            viewContact(lookupKey);
        }
            break;
        case ContactViewDialog.RESULT_CALL_NUMBER:
        {
            String phoneNumber = data.getStringExtra(ContactViewDialog.PHONE_NUMBER);
            callNumberAndFinish(phoneNumber);
        }
            break;
        case ContactViewDialog.RESULT_SEND_SMS:
        {
            String phoneNumber = data.getStringExtra(ContactViewDialog.PHONE_NUMBER);
            sendSms(phoneNumber);
        }
            break;
        case ContactViewDialog.RESULT_SEND_EMAIL:
        {
            String emailAdress = data.getStringExtra(ContactViewDialog.EMAIL_ADDRESS);
            sendEmail(emailAdress);
        }
            break; 
        case ContactViewDialog.RESULT_SET_SPEED_DIAL:
        {
            ArrayList<String> phoneNumbers = data.getStringArrayListExtra(ContactViewDialog.PHONE_NUMBERS);
            ArrayList<String> phoneTypes = data.getStringArrayListExtra(ContactViewDialog.PHONE_TYPES);
            selectSpeedDialNumber(phoneNumbers, phoneTypes);
        }
            break;
        case ContactViewDialog.RESULT_DIAL_NUMBER:
        {
            String phoneNumber = data.getStringExtra(ContactViewDialog.PHONE_NUMBER);
            dialNumber(phoneNumber);
        }
            break;
        default:
            break;
        }
    }

    private void dialNumber(String phoneNumber) {
        enableDialer();
        mViewHolder.phoneNumber.setText(phoneNumber);
        mViewHolder.phoneNumber.setSelection(mViewHolder.phoneNumber.getText().length());
        mViewHolder.phoneNumber.setSelected(false);
    }

    private void addPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, Uri.parse("tel:"+phoneNumber));
        startActivity(intent);
    }

    private void selectSpeedDialNumber(ArrayList<String> phoneNumbers,
            ArrayList<String> phoneTypes) {
        if (phoneNumbers.size() == 1) {
            selectSpeedDialSlot(phoneNumbers.get(0));
        } else {
            Intent intent = new Intent(getApplicationContext(), SelectNumberDialog.class);
            intent.putExtra(SelectNumberDialog.PHONE_NUMBERS, phoneNumbers);
            intent.putExtra(SelectNumberDialog.PHONE_TYPES, phoneTypes);
            startActivityForResult(intent, ACTIVITY_SELECT_NUMBER_DIALOG);
        }
    }
    
    private void selectSpeedDialSlot(String phoneNumber) {
        Intent intent = new Intent(getApplicationContext(), SelectSpeedDialSlotDialog.class);
        intent.putExtra(SelectSpeedDialSlotDialog.PHONE_NUMBER, phoneNumber);
        startActivityForResult(intent, ACTIVITY_SELECT_SPEED_DIAL_SLOT_DIALOG);
    }
    
    private void setSpeedDial(String phoneNumber, int slot) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Editor editor = preferences.edit();
        editor.putString(PREFERENCE_SPEED_DIAL_SLOT+slot, phoneNumber);
        editor.commit();
    }



    private void sendEmail(String emailAdress) {
        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+emailAdress)));
    }

    private void sendSms(String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phoneNumber)));
    }

    private void viewContact(final String lookupKey) {
    	// can't start an activity on the lookup uri itself on android 2.1 phones
        new Thread() {
            @Override
            public void run() {
                Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
                startActivity(new Intent(Intent.ACTION_VIEW, Contacts.lookupContact(getContentResolver(), lookupUri)));
            }
        }.start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mInCallLogMode) {
            Cursor c = (Cursor)mCallLogAdapter.getItem(position);
            String phoneNumber = c.getString(CallLogAdapter.COLUMN_NUMBER);
            if (!TextUtils.isEmpty(phoneNumber) && !TextUtils.equals("-1", phoneNumber)) {
                callNumberAndFinish(c.getString(c.getColumnIndex(Calls.NUMBER))); // TODO: this needs to be checked.
            }
        } else {
        	Cursor c = (Cursor)mContactsAdapter.getItem(position);
        	callContactAndFinish(c.getString(ContactsAdapter.COLUMN_LOOKUP_KEY));
        }
    }

    private void callNumberAndFinish(CharSequence number) {
        if (number == null || number.length() == 0) {
        	Toast.makeText(this, R.string.contact_has_no_phone_number, Toast.LENGTH_SHORT).show();
            return;
        }         	

        Uri uri = Uri.parse("tel:"+Uri.encode(number.toString()));
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        startActivity(intent);
        finish();
    }
    
    private void callContactAndFinish(String lookupKey) {
    	mQueryHandler.callContactAndFinish(lookupKey);
    }

    private void switchToCallLogMode() {
        if (mInCallLogMode) {
            return;
        }
        mInCallLogMode = true;
        mViewHolder.contactList.setAdapter(mCallLogAdapter);
    }
    private void switchToContactListMode() {
        if (!mInCallLogMode) {
            return;
        }
    	mInCallLogMode = false;
    	mViewHolder.contactList.setAdapter(mContactsAdapter);
    }
    
    private void askToClearCallLog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.clear_call_log)
               .setMessage(R.string.are_you_sure_you_want_to_clear_the_call_log)
               .setCancelable(false)
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       clearCallLog();
                   }
                })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.cancel();
                   }
               })
               .setIcon(R.drawable.ic_dialog_alert);
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    private void clearCallLog() {
        getContentResolver().delete(Calls.CONTENT_URI, null, null);
    }
    
    private void clearMissedCalls() {
        // Clear the new missed calls (the actual missed calls, doesn't clear the notification)
    	StringBuilder where = new StringBuilder();
    	where.append(Calls.TYPE);
    	where.append("=");
    	where.append(Calls.MISSED_TYPE);
    	where.append(" AND ");
    	where.append(Calls.NEW);
    	where.append("=1");
    	ContentValues cv = new ContentValues(1);
    	cv.put(Calls.NEW, 0);
    	getContentResolver().update(Calls.CONTENT_URI, cv, where.toString(), null);
    }
    
    private void updateMissedCalls() {
    	
    	// Start with sleeping for a while, maybe the user wants to do something else, give them time to
    	try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	ContentValues cv = new ContentValues(3);
    	ContentResolver cr = getContentResolver();
    	final String[] phoneProjection = new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup.TYPE, PhoneLookup.LABEL};
    	
    	
    	
    	Cursor c = cr.query(Calls.CONTENT_URI,
    			new String[] {Calls.CACHED_NAME, Calls.NUMBER}, null, null, null);
    	if (!c.moveToFirst()) {
    		c.close();
    		return;
    	}
    	HashSet<String> processedNumbers = new HashSet<String>(c.getCount());
    	
    	do {
    		String cachedName = c.getString(0);
    		String number = c.getString(1);
    		
    		if (processedNumbers.contains(number)) {
    			continue;
    		}
    		processedNumbers.add(number);
    		Cursor c2 = cr.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)), 
    				phoneProjection, null, null, null);
    		if (c2.moveToFirst()) {
    			String name = c2.getString(0);
    			if (!TextUtils.equals(name, cachedName)) {
    				cv.clear();
    				cv.put(Calls.CACHED_NAME, c2.getString(0));
    				cv.put(Calls.CACHED_NUMBER_TYPE, c2.getInt(1));
    				cv.put(Calls.CACHED_NUMBER_LABEL, c2.getString(2));
    				cr.update(Calls.CONTENT_URI, cv, Calls.NUMBER + "=?", new String[] {number});
    			}
    		} else if (cachedName != null) {
    			cv.clear();
    			cv.putNull(Calls.CACHED_NAME);
    			cv.putNull(Calls.CACHED_NUMBER_TYPE);
    			cv.putNull(Calls.CACHED_NUMBER_LABEL);
    			cr.update(Calls.CONTENT_URI, cv, Calls.NUMBER + "=?", new String[]  {number});
    		}
    		c2.close();
    	} while (c.moveToNext());
    	c.close();
    }
    
    private void clearMissedCallsNotification() {
    	/*This actually uses non-public API's and reflection to access them
    	 * Code was taken from http://stackoverflow.com/questions/2720967/update-missed-calls-notification-on-android
    	 * only clears the notification */ 
		String LOG_TAG = "DialerActivity";
		try {
	        Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
	        Method getServiceMethod = serviceManagerClass.getMethod("getService", String.class);
	        Object phoneService = getServiceMethod.invoke(null, "phone");
	        Class<?> ITelephonyClass = Class.forName("com.android.internal.telephony.ITelephony");
	        Class<?> ITelephonyStubClass = null;
	        for(Class<?> clazz : ITelephonyClass.getDeclaredClasses()) {
	            if (clazz.getSimpleName().equals("Stub")) {
	                ITelephonyStubClass = clazz;
	                break;
	            }
	        }
	        if (ITelephonyStubClass != null) {
	            Class<?> IBinderClass = Class.forName("android.os.IBinder");
	            Method asInterfaceMethod = ITelephonyStubClass.getDeclaredMethod("asInterface",
	                    IBinderClass);
	            Object iTelephony = asInterfaceMethod.invoke(null, phoneService);
	            if (iTelephony != null) {
	                Method cancelMissedCallsNotificationMethod = iTelephony.getClass().getMethod(
	                        "cancelMissedCallsNotification");
	                cancelMissedCallsNotificationMethod.invoke(iTelephony);
	            } else {
	                Log.w(LOG_TAG, "Telephony service is null, can't call "
	                        + "cancelMissedCallsNotification");
	            }
	        }
	        else {
	            Log.d(LOG_TAG, "Unable to locate ITelephony.Stub class!");
	        }
	    } catch (ClassNotFoundException ex) {
	        Log.e(LOG_TAG,
	                "Failed to clear missed calls notification due to ClassNotFoundException!", ex);
	    } catch (InvocationTargetException ex) {
	        Log.e(LOG_TAG,
	                "Failed to clear missed calls notification due to InvocationTargetException!",
	                ex);
	    } catch (NoSuchMethodException ex) {
	        Log.e(LOG_TAG,
	                "Failed to clear missed calls notification due to NoSuchMethodException!", ex);
	    } catch (Throwable ex) {
	        Log.e(LOG_TAG, "Failed to clear missed calls notification due to Throwable!", ex);
	    }
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// start, before and count are zero, then nothing has actually happened
		// and we've probably just been restarted from an orientation change, then do nothing
		if (start == 0 &&
				before == 0 &&
				count == 0) {
			return;
		}
		// otherwise we filter on the text and switch to contact list mode
		mContactsAdapter.getFilter().filter(s);
		switchToContactListMode();
		mViewHolder.contactList.setSelection(0);
	}

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        applySettings(sharedPreferences);
    }

	@Override
	public Cursor runQuery(CharSequence constraint) {
		stopManagingCursor(mContactsCursor);
		if (constraint == null || constraint.length() == 0) {
			mContactsCursor = managedQuery(Contacts.CONTENT_URI, ContactsAdapter.PROJECTION, Contacts.HAS_PHONE_NUMBER + "=1", null, 
			        mFavoriteContactsFirst ? Contacts.STARRED + " DESC, " + Contacts.DISPLAY_NAME : Contacts.DISPLAY_NAME);
			return mContactsCursor;
		}
		String[] t9Lookup = getResources().getStringArray(R.array.t9lookup);
		StringBuilder builder = new StringBuilder();
		String constraintString = constraint.toString();
		for (int i=0,constraintLength=constraintString.length(); i<constraintLength; i++) {
			char c = constraintString.charAt(i);

			if (c >= '0' && c <= '9') {
				builder.append(t9Lookup[c-'0']);
			} else if (c == '+') {
				builder.append(c);
			} else {
				builder.append("[" + Character.toLowerCase(c) + Character.toUpperCase(c) + "]");
			}
		}
		
		Uri contentUri = mT9MatchPhoneNumbers ? Phone.CONTENT_URI : Contacts.CONTENT_URI;
		
		String prefix = mT9MatchStartOfNamesOnly ? "" : "*";
		String whereStmt = mT9MatchPhoneNumbers ? "(" + Phone.DISPLAY_NAME + " GLOB ?) OR (" + Phone.NUMBER + " LIKE ?)" : "(" + Contacts.HAS_PHONE_NUMBER + " = 1) AND " + Contacts.DISPLAY_NAME + " GLOB ?";
		String[] whereArgs = mT9MatchPhoneNumbers ? new String[] {prefix + builder.toString() + "*", constraintString+"%"} : new String[] {prefix + builder.toString() + "*"};
		StringBuilder orderByBuilder = new StringBuilder();
		if (mFavoriteContactsFirst) {
		    orderByBuilder.append(Contacts.STARRED);
		    orderByBuilder.append(" DESC,");
		}
		if (mT9OrderByTimesContacted) {
		    orderByBuilder.append(Contacts.TIMES_CONTACTED);
		    orderByBuilder.append(" DESC,");
		    orderByBuilder.append(Contacts.LAST_TIME_CONTACTED);
		    orderByBuilder.append(" DESC, ");
		}
		orderByBuilder.append(Phone.DISPLAY_NAME);
		mContactsCursor = managedQuery(contentUri, ContactsAdapter.PROJECTION, whereStmt, whereArgs, orderByBuilder.toString());
		return mContactsCursor;
	}
	
	private class QueryHandler extends AsyncQueryHandler {
	    private static final int TOKEN_GET_CALL_LOG = 1;
	    private static final int TOKEN_CALL_CONTACT = 2;
	    private static final int TOKEN_SHOW_CONTACT_FOR_NUMBER = 3;
	    private static final int TOKEN_SHOW_CONTACT_FOR_LOOKUP_KEY = 4;

	    public QueryHandler(ContentResolver cr) {
	        super(cr);
	    }

	    @Override
	    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
	        switch (token) {
	        case TOKEN_GET_CALL_LOG:
	            startManagingCursor(cursor);
	            mCallLogAdapter.changeCursor(cursor);
	            break;
	        case TOKEN_CALL_CONTACT:
	            // loop through all numbers, if one of them is_super_primary call it, otherwise call the first number found
	            if (cursor.moveToFirst()) {
	                String numberToCall = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
	                do {
	                    if (cursor.getInt(cursor.getColumnIndex(Phone.IS_SUPER_PRIMARY)) == 1) {
	                        numberToCall = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
	                        break;
	                    }
	                } while (cursor.moveToNext());
	                cursor.close();
	                Uri uri = Uri.parse("tel:"+Uri.encode(numberToCall));
	                Intent intent = new Intent(Intent.ACTION_CALL, uri);
	                startActivity(intent);
	                finish();
	            } else {
	                Toast.makeText(DialerActivity.this, R.string.contact_has_no_phone_number, Toast.LENGTH_SHORT).show();
	            }
	            break;
	        case TOKEN_SHOW_CONTACT_FOR_NUMBER:
	            if (!cursor.moveToFirst()) {
	                addPhoneNumber((String)cookie);
	                cursor.close();
	                return;
	            }
	            String lookupKey = cursor.getString(0);
	            cursor.close();
	            showContactForLookupKey(lookupKey);
	            break;
	        case TOKEN_SHOW_CONTACT_FOR_LOOKUP_KEY:
	            if (!cursor.moveToFirst()) {
	                // no data for this lookup key, should never happen
	            	cursor.close();
	                return;
	            }
	            ArrayList<String> phoneNumbers = new ArrayList<String>(cursor.getCount());
	            ArrayList<Integer> phoneTypes = new ArrayList<Integer>(cursor.getCount());
	            ArrayList<String> emailAddresses = new ArrayList<String>(cursor.getCount());
	            String displayName = cursor.getString(0);
	            do {
	                if (Phone.CONTENT_ITEM_TYPE.equals(cursor.getString(1))) {
	                    phoneNumbers.add(cursor.getString(2));
	                    phoneTypes.add(cursor.getInt(3));
	                } else {
	                    emailAddresses.add(cursor.getString(2));
	                }
	            } while (cursor.moveToNext());
	            cursor.close();

	            Intent intent = new Intent(getApplicationContext(), ContactViewDialog.class);
	            intent.putExtra(ContactViewDialog.LOOKUP_KEY, (String)cookie);
	            intent.putExtra(ContactViewDialog.DISPLAY_NAME, displayName);
	            intent.putExtra(ContactViewDialog.PHONE_NUMBERS, phoneNumbers);
	            intent.putExtra(ContactViewDialog.PHONE_TYPES, phoneTypes);
	            intent.putExtra(ContactViewDialog.EMAIL_ADDRESSES, emailAddresses);
	            startActivityForResult(intent, ACTIVITY_CONTACT_VIEW_DIALOG);
	            break;

	        }
	    }

	    public void getCallLog() {
	        startQuery(TOKEN_GET_CALL_LOG, null, Calls.CONTENT_URI, CallLogAdapter.PROJECTION, null, null, Calls.DEFAULT_SORT_ORDER);
	    }

	    public void callContactAndFinish(String lookupKey) {
	        startQuery(TOKEN_CALL_CONTACT, null, Phone.CONTENT_URI, null, Phone.LOOKUP_KEY + "=?", new String[] {lookupKey}, null);
	    }

	    public void showContactForNumber(String phoneNumber) {
	        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
	        startQuery(TOKEN_SHOW_CONTACT_FOR_NUMBER, phoneNumber, uri, new String[] {PhoneLookup.LOOKUP_KEY}, null, null, null);
	    }

	    public void showContactForLookupKey(String lookupKey) {
	        startQuery(TOKEN_SHOW_CONTACT_FOR_LOOKUP_KEY, lookupKey, Data.CONTENT_URI,
	                new String[] {Data.DISPLAY_NAME, Data.MIMETYPE, Data.DATA1, Data.DATA2},
	                Data.LOOKUP_KEY + "=? AND (" + Data.MIMETYPE + "=? OR " + Data.MIMETYPE + "=?)",
	                new String[] {lookupKey, Phone.CONTENT_ITEM_TYPE, Email.CONTENT_ITEM_TYPE},
	                Data.MIMETYPE + " DESC");
	    }
	}
	
	private class ViewHolder {
	    public final ListView contactList;
	    public final EditText phoneNumber;
	    public final ImageButton button0;
	    public final ImageButton button1;
	    public final ImageButton button2;
	    public final ImageButton button3;
	    public final ImageButton button4;
	    public final ImageButton button5;
	    public final ImageButton button6;
	    public final ImageButton button7;
	    public final ImageButton button8;
	    public final ImageButton button9;
	    public final ImageButton buttonDelete;
	    public final View dialerView;
	    public final View dialerExpandMenu;

	    public ViewHolder() {
	        contactList = (ListView)findViewById(R.id.ContactListView);
	        phoneNumber = (EditText)findViewById(R.id.EditTextPhoneNumber);
	        button0 = (ImageButton)findViewById(R.id.Button0);
	        button1 = (ImageButton)findViewById(R.id.Button1);
	        button2 = (ImageButton)findViewById(R.id.Button2);
	        button3 = (ImageButton)findViewById(R.id.Button3);
	        button4 = (ImageButton)findViewById(R.id.Button4);
	        button5 = (ImageButton)findViewById(R.id.Button5);
	        button6 = (ImageButton)findViewById(R.id.Button6);
	        button7 = (ImageButton)findViewById(R.id.Button7);
	        button8 = (ImageButton)findViewById(R.id.Button8);
	        button9 = (ImageButton)findViewById(R.id.Button9);
	        buttonDelete = (ImageButton)findViewById(R.id.ButtonDelete);
	        dialerView = (View)findViewById(R.id.DialerView);
	        dialerExpandMenu = (View)findViewById(R.id.DialerExpandMenu);

	    }
	}
}