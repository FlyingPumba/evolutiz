package org.dnaq.dialer2;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;

public class SelectNumberDialog extends Activity implements OnClickListener {
    
    public static final String PHONE_NUMBERS = "phoneNumbers";
    public static final String PHONE_TYPES = "phoneTypes";
    
    public static final String PHONE_NUMBER = "phoneNumber";

    private ArrayList<String> mPhoneNumbers;
    private ArrayList<Integer> mPhoneTypes;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        
        setContentView(R.layout.empty_dialog);
        
        LinearLayout phoneContainer = (LinearLayout)findViewById(R.id.EmptyDialogContainer);
        
        mPhoneNumbers = bundle.getStringArrayList(PHONE_NUMBERS); 
        mPhoneTypes = bundle.getIntegerArrayList(PHONE_TYPES);
        
        for (int i=0,length=mPhoneNumbers.size(); i<length; i++) {
            RadioButton rb = (RadioButton)getLayoutInflater().inflate(R.layout.contactview_dialog_item_phone, null);
            rb.setText(mPhoneNumbers.get(i));
            rb.setTag(mPhoneNumbers.get(i));
            if (mPhoneTypes.get(i) == Phone.TYPE_MOBILE) {
                rb.setButtonDrawable(R.drawable.button_cell);
            }
            phoneContainer.addView(rb);
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(PHONE_NUMBERS, mPhoneNumbers);
        outState.putIntegerArrayList(PHONE_TYPES, mPhoneTypes);
    }

    @Override
    public void onClick(View view) {
        String phoneNumber = (String)view.getTag();
        Intent data = new Intent();
        data.putExtra(PHONE_NUMBER, phoneNumber);
        setResult(RESULT_OK, data);
        finish();
    }
}
