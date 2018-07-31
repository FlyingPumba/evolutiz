package com.addi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;


public class EditTextExtend extends EditText {
	
	private AddiBase _parent = null;
	public int _prevPos = 0;
	public boolean _isTextEditorReturn = true;

    public EditTextExtend(Context context, AttributeSet atts)
    {
        super(context,atts);
        _parent = (AddiBase)context;
    }
    
    public EditTextExtend(Context context)
    {
        super(context);
        _parent = (AddiBase)context;
    }
	
	@Override 
	protected void onSelectionChanged(int selStart, int selEnd) {
		if (_parent != null) {
			if (_prevPos != getSelectionStart()) { 
				_parent.updateSuggestions();
				_prevPos = getSelectionStart();
			}
		}
	}
	 
	@Override      
	public boolean onCheckIsTextEditor() {   
		return _isTextEditorReturn;     
	} 
	
}
