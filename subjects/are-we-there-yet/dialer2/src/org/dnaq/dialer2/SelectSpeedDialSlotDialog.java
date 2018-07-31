package org.dnaq.dialer2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;

public class SelectSpeedDialSlotDialog extends Activity implements OnClickListener {

    public static final String PHONE_NUMBER = "phoneNumber";
    
    public static final String SPEED_DIAL_SLOT = "speedDialSlot";
    private String mPhoneNumber;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        
        mPhoneNumber = bundle.getString(PHONE_NUMBER);
        
        setContentView(R.layout.empty_dialog);

        LinearLayout speedDialSlotContainer = (LinearLayout)findViewById(R.id.EmptyDialogContainer);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        for (int i=1; i<=9; i++) {
            RadioButton rb = (RadioButton)getLayoutInflater().inflate(R.layout.contactview_dialog_item_phone, null);
            String phoneNumber = preferences.getString(DialerActivity.PREFERENCE_SPEED_DIAL_SLOT+i, null);
            
            String buttonText = phoneNumber == null ? getString(R.string.not_set) : phoneNumber;
            
            rb.setText(i+": "+buttonText);
            rb.setButtonDrawable(R.drawable.button_speed_dial);
            rb.setTag(i);
            speedDialSlotContainer.addView(rb);
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PHONE_NUMBER, mPhoneNumber);
    }
    
    @Override
    public void onClick(View v) {

        int slot = (Integer)v.getTag();
        Intent data = new Intent();
        data.putExtra(PHONE_NUMBER, mPhoneNumber);
        data.putExtra(SPEED_DIAL_SLOT, slot);
        setResult(RESULT_OK, data);
        finish();
    }
}
