package com.addi;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

public class KeyboardViewExtend extends KeyboardView implements KeyboardView.OnKeyboardActionListener
{

	private AddiBase _parent = null;
	private Keyboard _myKeyboard = null;
	private Keyboard _myKeyboardShifted = null;
	private Keyboard _myKeyboardSymbols = null;
	private Keyboard _myKeyboardOps = null;

	public KeyboardViewExtend(Context c)
	{
		super(c,null);
		init(c);
	}
	public KeyboardViewExtend(Context c, AttributeSet a)
	{
		super(c,a);
		init(c);
		
	}

	private void init(Context c) {
		_parent = (AddiBase)c;	
		setOnKeyboardActionListener(this);
		setEnabled(true);  
		setPreviewEnabled(true);   
		setVisibility(View.GONE);
		setFocusable(false);
		setFocusableInTouchMode(false);
		setBackgroundColor( Color.BLACK );
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
		lp.gravity = Gravity.BOTTOM;
		setLayoutParams(lp);
		makeKeyboardView();
	}
	
	public void swipeUp() {
		_parent.swipeUp();
	}
	
	public void swipeDown() {
		_parent.swipeDown();
	}
	
	public void swipeLeft() {
		_parent.swipeLeft();
	}
	
	public void swipeRight() {
		_parent.swipeRight();
	}
	
	public void onPress(int k) {}
	
	public void onRelease(int k) {}
	
	public void onText(CharSequence s) {
		if (s.toString().endsWith("()") || s.toString().endsWith("[]")) {
			_parent._backUpOne = true;
		}
		_parent.sendText(s.toString());
		if (getKeyboard() != _myKeyboard) {
			setKeyboard(_myKeyboard);
		}
	}
	
	public void onKey(int k,int[] ignore) {
		if (k == -1) {
			if (getKeyboard() == _myKeyboard) {
				setKeyboard(_myKeyboardShifted);
			} else if (getKeyboard() == _myKeyboardShifted) {
				setKeyboard(_myKeyboard);
			} else if (getKeyboard() == _myKeyboardOps) {
				setKeyboard(_myKeyboardSymbols);
			} else if (getKeyboard() == _myKeyboardSymbols) {
				setKeyboard(_myKeyboardOps);
			}
		} else if (k == -100) {
			setKeyboard(_myKeyboardOps);
		} else if (k == -102) {
			setKeyboard(_myKeyboard);
		} else if (k == -5) {
			_parent.handleBackspace();
			if (getKeyboard() != _myKeyboard) {
				setKeyboard(_myKeyboard);
			}
		} else if (k == 10) {
			_parent.handleEnter();
			if (getKeyboard() != _myKeyboard) {
				setKeyboard(_myKeyboard);
			}
		} else if (k == 32) {
			_parent.sendText(" ");
		}
		
	}
	
	public void makeKeyboardView () {
		_myKeyboard = new Keyboard(_parent, R.xml.qwerty);
		_myKeyboardShifted = new Keyboard(_parent,R.xml.qwerty_shifted);
		_myKeyboardSymbols = new Keyboard(_parent,R.xml.symbols);
		_myKeyboardOps = new Keyboard(_parent,R.xml.ops);
		setKeyboard(_myKeyboard);  
	}
	
	public void myOnConfigurationChanged(Configuration newConfig)
	{ 
		makeKeyboardView();
	}
	
}