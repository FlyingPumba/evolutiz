package org.dnaq.dialer2;

import org.dnaq.dialer2.AsyncContactImageLoader.ImageCallback;
import org.dnaq.libs.GroupingCursorAdapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.CallLog.Calls;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public class CallLogAdapter extends GroupingCursorAdapter {
    public static final String[] PROJECTION = new String[] {
        Calls._ID,
        Calls.NUMBER,
        Calls.CACHED_NAME,
        Calls.DATE,
        Calls.DURATION,
        Calls.NEW,
        Calls.TYPE
    };
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_NUMBER = 1;
    public static final int COLUMN_CACHED_NAME = 2;
    public static final int COLUMN_DATE = 3;
    public static final int COLUMN_DURATION = 4;
    public static final int COLUMN_NEW = 5;
    public static final int COLUMN_TYPE = 6;

    private class ViewCache {
        public final TextView contactName;
        public final TextView contactLastDialed;
        public final TextView contactInformation;
        public final TextView contactCallCount;
        public final ImageView contactImage;
        public final ImageView contactOverlayImage;

        public ViewCache(View base) {
            contactName = (TextView)base.findViewById(R.id.ContactName);
            contactLastDialed = (TextView)base.findViewById(R.id.ContactLastDialed);
            contactInformation = (TextView)base.findViewById(R.id.ContactInformation);
            contactCallCount = (TextView)base.findViewById(R.id.ContactCallCount);
            contactImage = (ImageView)base.findViewById(R.id.ContactImage);
            contactOverlayImage = (ImageView)base.findViewById(R.id.ContactOverlayImage);
        }

    }


    private Resources mResources;
    private AsyncContactImageLoader mAsyncContactImageLoader;
    private final Drawable mIncomingDrawable;
    private final Drawable mOutgoingDrawable;
    private final Drawable mMissedDrawable;
    
    public CallLogAdapter(Context context, Cursor cursor, AsyncContactImageLoader asyncContactImageLoader) {
        super(context, cursor, Calls.NUMBER);
        mResources = context.getResources();
        mAsyncContactImageLoader = asyncContactImageLoader;
        mIncomingDrawable = mResources.getDrawable(R.drawable.overlay_incoming);
        mOutgoingDrawable = mResources.getDrawable(R.drawable.overlay_outgoing);
        mMissedDrawable = mResources.getDrawable(R.drawable.overlay_missed);
    }
    
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        
        if (shouldBeGrouped(cursor)) {
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
        if (viewCache == null) { // empty view, shouldn't bind it
        	return;
        }
        
        String cachedName = cursor.getString(COLUMN_CACHED_NAME);
        
//        CallLogEntry entry = CallLogEntry.fromCursor(cursor);
        viewCache.contactName.setText(TextUtils.isEmpty(cachedName) ? mResources.getString(R.string.unknown) : cachedName);

        long date = cursor.getLong(COLUMN_DATE);
        viewCache.contactLastDialed.setText(date == 0 ? mResources.getString(R.string.not_contacted) : DateUtils.getRelativeTimeSpanString(date));

        String number = cursor.getString(COLUMN_NUMBER); // number of -1 does not have a number
        viewCache.contactInformation.setText(TextUtils.equals(number, "-1") || TextUtils.isEmpty(number) ? "" : number);
        viewCache.contactCallCount.setVisibility(View.GONE);
//        viewCache.contactCallCount.setTextColor(getColorForCallType(entry.type));
        int type = cursor.getInt(COLUMN_TYPE); 
        
        switch (type) {
        case Calls.INCOMING_TYPE:
        	viewCache.contactOverlayImage.setImageDrawable(mIncomingDrawable);
        	break;
        case Calls.OUTGOING_TYPE:
        	viewCache.contactOverlayImage.setImageDrawable(mOutgoingDrawable);
        	break;
        case Calls.MISSED_TYPE:
        	viewCache.contactOverlayImage.setImageDrawable(mMissedDrawable);
        	break;
        }
        viewCache.contactOverlayImage.setVisibility(View.VISIBLE);
        
        viewCache.contactImage.setTag(number); // set a tag for the callback to be able to check, so we don't set the contact image of a reused view
        Drawable d = mAsyncContactImageLoader.loadDrawableForNumber(number, new ImageCallback() {
            
            @Override
            public void imageLoaded(Drawable imageDrawable, String phoneNumber) {
                if (TextUtils.equals(phoneNumber, (String)viewCache.contactImage.getTag())) {
                    viewCache.contactImage.setImageDrawable(imageDrawable);
                }
            }
        });
        viewCache.contactImage.setImageDrawable(d);

        
    }
}
