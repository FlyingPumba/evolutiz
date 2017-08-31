package org.dnaq.dialer2;

import org.dnaq.dialer2.AsyncContactImageLoader.ImageCallback;
import org.dnaq.libs.GroupingCursorAdapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsAdapter extends GroupingCursorAdapter {
    public static final String[] PROJECTION = new String[] {
        Phone._ID,
        Phone.LOOKUP_KEY,
        Phone.DISPLAY_NAME,
        Phone.CONTACT_STATUS,
        Phone.TIMES_CONTACTED,
        Phone.LAST_TIME_CONTACTED,
        Phone.STARRED
    };
    public static final int COLUMN_PHONE_ID = 0;
    public static final int COLUMN_LOOKUP_KEY = 1;
    public static final int COLUMN_DISPLAY_NAME = 2;
    public static final int COLUMN_CONTACT_STATUS = 3;
    public static final int COLUMN_TIMES_CONTACTED = 4;
    public static final int COLUMN_LAST_TIME_CONTACTED = 5;
    public static final int COLUMN_STARRED = 6;

    private class ViewCache {
        public final TextView contactName;
        public final TextView contactLastDialed;
        public final TextView contactInformation;
        public final TextView contactCallCount;
        public final ImageView contactImage;
        public final ImageView contactStarred;

        public ViewCache(View base) {
            contactName = (TextView)base.findViewById(R.id.ContactName);
            contactLastDialed = (TextView)base.findViewById(R.id.ContactLastDialed);
            contactInformation = (TextView)base.findViewById(R.id.ContactInformation);
            contactCallCount = (TextView)base.findViewById(R.id.ContactCallCount);
            contactImage = (ImageView)base.findViewById(R.id.ContactImage);
            contactStarred = (ImageView)base.findViewById(R.id.ContactStarred);
        }

    }


    private Resources mResources;
    private AsyncContactImageLoader mAsyncContactImageLoader;
    private boolean mShowCallCounter;
    
    public ContactsAdapter(Context context, Cursor cursor, AsyncContactImageLoader asyncContactImageLoader) {
        super(context, cursor, Phone.LOOKUP_KEY);
        mResources = context.getResources();
        mAsyncContactImageLoader = asyncContactImageLoader;
        mShowCallCounter = false;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (shouldBeGrouped(cursor)) { // we just hide the grouped views
        	View view = new View(context);
        	view.setLayoutParams(new AbsListView.LayoutParams(0, 0));
            return view;
        }
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item, null);
    	ViewCache viewCache = new ViewCache(view);
    	view.setTag(viewCache);

        return view;
    }
    
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        final ViewCache viewCache = (ViewCache)view.getTag();
        if (viewCache == null) { // empty view has no viewcache, and we do nothing with it
        	return;
        }
        String displayName = cursor.getString(COLUMN_DISPLAY_NAME);
        if (displayName == null) {
            displayName = "";
        }
        
        viewCache.contactName.setText(displayName);

        long lastTimeContacted = cursor.getLong(COLUMN_LAST_TIME_CONTACTED);
        if (lastTimeContacted == 0) {
        	viewCache.contactLastDialed.setText(mResources.getString(R.string.not_contacted));
        } else {
        	viewCache.contactLastDialed.setText(DateUtils.getRelativeTimeSpanString(lastTimeContacted));
        }
        
        String status = cursor.getString(COLUMN_CONTACT_STATUS);
        if (status == null) {
            status = "";
        }
        if (status.length() > 137) {
            status = status.substring(0, 137) + "...";
        }
        status = status.replace('\n', ' ');
        viewCache.contactInformation.setText(status);
        
        int timesContacted = cursor.getInt(COLUMN_TIMES_CONTACTED);
        if (mShowCallCounter) {
        	viewCache.contactCallCount.setText("(" + timesContacted + ")");
        } else {
        	viewCache.contactCallCount.setText("");
        }
        
        int starred = cursor.getInt(COLUMN_STARRED);
        viewCache.contactStarred.setVisibility(starred == 1? View.VISIBLE : View.GONE);

        String lookupKey = cursor.getString(COLUMN_LOOKUP_KEY);
        if (lookupKey == null) { // should absolutely never happen
            lookupKey = "";
        }
        viewCache.contactImage.setTag(lookupKey); // set a tag for the callback to be able to check, so we don't set the contact image of a reused view
        Drawable d = mAsyncContactImageLoader.loadDrawableForContact(lookupKey, new ImageCallback() {
            
            @Override
            public void imageLoaded(Drawable imageDrawable, String lookupKey) {
                if (lookupKey.equals(viewCache.contactImage.getTag())) {
                    viewCache.contactImage.setImageDrawable(imageDrawable);
                }
            }
        });
        viewCache.contactImage.setImageDrawable(d);
    }
	
	public void setShowCallCounter(boolean showCallCounter) {
		mShowCallCounter = showCallCounter;
	}
}