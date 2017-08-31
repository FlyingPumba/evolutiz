package org.dnaq.dialer2;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;

public class ContactViewDialog extends Activity implements OnClickListener, OnLongClickListener {
    public static final String LOOKUP_KEY = "lookupKey";
    public static final String DISPLAY_NAME = "displayName";
    public static final String PHONE_NUMBERS = "phoneNumbers";
    public static final String PHONE_TYPES = "phoneTypes";
    public static final String EMAIL_ADDRESSES = "emailAddresses";
    
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String EMAIL_ADDRESS = "emailAddress";

    public static final int RESULT_VIEW_CONTACT = 1;
    public static final int RESULT_CALL_NUMBER = 2;
    public static final int RESULT_SEND_SMS = 3;
    public static final int RESULT_SEND_EMAIL = 4;
    public static final int RESULT_SET_SPEED_DIAL = 5;
    public static final int RESULT_DIAL_NUMBER = 6;
    

    private static final int ID_CALL = 2;
    private static final int ID_SMS = 3;
    private static final int ID_EMAIL = 4;
    
    private String mLookupKey;
    private ArrayList<String> mPhoneNumbers;
    private ArrayList<Integer> mPhoneTypes;
    private ArrayList<String> mEmailAddresses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.contactview_dialog);
        
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        
        mLookupKey = bundle.getString(LOOKUP_KEY);
        mPhoneNumbers = bundle.getStringArrayList(PHONE_NUMBERS);
        mPhoneTypes = bundle.getIntegerArrayList(PHONE_TYPES);
        mEmailAddresses = bundle.getStringArrayList(EMAIL_ADDRESSES);
        String displayName = bundle.getString(DISPLAY_NAME);

        setContactName(displayName, mLookupKey);
        
        RadioButton rbSetSpeedDial = (RadioButton)findViewById(R.id.ContactViewDialogSpeedDial);
        if (mPhoneNumbers.size() > 0) {
            rbSetSpeedDial.setVisibility(View.VISIBLE);
        } else {
            rbSetSpeedDial.setVisibility(View.GONE);
        }
        
        for (int i=0,length=mPhoneNumbers.size(); i<length; i++) {
            addContactNumber(mPhoneNumbers.get(i), mPhoneTypes.get(i));
        }
        
        for (String emailAddress : mEmailAddresses) {
            addContactAddress(emailAddress);
        }
    }
    
    private void setContactName(String name, String lookupKey) {
        RadioButton rb = (RadioButton)findViewById(R.id.ContactViewDialogName);
        rb.setText(name);
        rb.setTag(lookupKey);
    }
    
    private void addContactNumber(String number, int type) {
        LinearLayout ll = (LinearLayout)findViewById(R.id.ContactViewDialogPhoneContainer);
        RadioButton rb = (RadioButton)getLayoutInflater().inflate(R.layout.contactview_dialog_item_phone, null);
        rb.setText(number);
        if (type == Phone.TYPE_MOBILE) {
            rb.setButtonDrawable(R.drawable.button_cell);
        } else {
            rb.setButtonDrawable(R.drawable.button_phone);
        }
        rb.setId(ID_CALL);
        rb.setTag(number);
        
        rb.setOnLongClickListener(this);
        
        ll.addView(rb);
        if (type == Phone.TYPE_MOBILE) {
            RadioButton smsRb = (RadioButton)getLayoutInflater().inflate(R.layout.contactview_dialog_item_phone, null);
            smsRb.setText(number);
            smsRb.setButtonDrawable(R.drawable.button_message);
            smsRb.setId(ID_SMS);
            smsRb.setTag(number);
            
            smsRb.setOnLongClickListener(this);
            ll.addView(smsRb);
        }
    }
    
    private void addContactAddress(String address) {
        LinearLayout ll = (LinearLayout)findViewById(R.id.ContactViewDialogPhoneContainer);
        RadioButton rb = (RadioButton)getLayoutInflater().inflate(R.layout.contactview_dialog_item_phone, null);
        rb.setText(address);
        rb.setButtonDrawable(R.drawable.button_message);
        rb.setId(ID_EMAIL);
        rb.setTag(address);
        ll.addView(rb);
    }

    @Override
    public void onClick(View view) {
        Intent data = new Intent();
        switch (view.getId()) {
        
        case ID_CALL:
            data.putExtra(PHONE_NUMBER, (String)view.getTag());
            setResult(RESULT_CALL_NUMBER, data);
            break;
        case ID_SMS:
            data.putExtra(PHONE_NUMBER, (String)view.getTag());
            setResult(RESULT_SEND_SMS, data);
            break;
        case ID_EMAIL:
            data.putExtra(EMAIL_ADDRESS, (String)view.getTag());
            setResult(RESULT_SEND_EMAIL, data);
            break;
        case R.id.ContactViewDialogName:
            data.putExtra(LOOKUP_KEY, (String)view.getTag());
            setResult(RESULT_VIEW_CONTACT, data);
            break;
        case R.id.ContactViewDialogSpeedDial:
            data.putExtra(PHONE_NUMBERS, mPhoneNumbers);
            data.putExtra(PHONE_TYPES, mPhoneTypes);
            setResult(RESULT_SET_SPEED_DIAL, data);
            break; 
        }
        finish();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOOKUP_KEY, mLookupKey);
        outState.putStringArrayList(PHONE_NUMBERS, mPhoneNumbers);
        outState.putIntegerArrayList(PHONE_TYPES, mPhoneTypes);
        outState.putStringArrayList(EMAIL_ADDRESSES, mEmailAddresses);
    }

    @Override
    public boolean onLongClick(View v) {
        String phoneNumber = (String)v.getTag();
        Intent data = new Intent();
        data.putExtra(PHONE_NUMBER, phoneNumber);
        setResult(RESULT_DIAL_NUMBER, data);
        finish();
        return true;
    }
}