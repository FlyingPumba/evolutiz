package com.gluegadget.hndroid;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginDialog extends Dialog {
	
	Button loginButton;
	String name;
    private ReadyListener readyListener;
    EditText username;
    EditText password;
	
	public interface ReadyListener {
        public void ready(String username, String password);
    }
	
	public LoginDialog(Context context, String name, ReadyListener readyListener) {
		super(context);
		this.readyListener = readyListener;
		this.name = name;

	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        setTitle("Sign In");
		loginButton = (Button)findViewById(R.id.login_button);
		loginButton.setOnClickListener(new loginListener());
		username = (EditText)findViewById(R.id.username);
		password = (EditText)findViewById(R.id.password);
    }
	
	private class loginListener implements android.view.View.OnClickListener {
		 @Override
	     public void onClick(View v) {
			 readyListener.ready(String.valueOf(username.getText()), String.valueOf(password.getText()));
			 dismiss();
		 }
	}
	
	public void onClick(View v) {
		if (v == loginButton) {
			dismiss();
		}
	}

}
