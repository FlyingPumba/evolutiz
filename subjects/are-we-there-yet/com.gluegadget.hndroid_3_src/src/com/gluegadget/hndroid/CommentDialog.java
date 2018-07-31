package com.gluegadget.hndroid;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CommentDialog extends Dialog {
	
	Button submitButton;
	String name;
	String replyUrl;
    private ReadyListener readyListener;
    EditText text;
	
	public interface ReadyListener {
        public void ready(String text);
        public void ready(String text, String replyUrl);
    }
	
	public CommentDialog(Context context, String name, ReadyListener readyListener) {
		super(context);
		this.readyListener = readyListener;
		this.name = name;
	}
	
	public CommentDialog(Context context, String name, String replyUrl, ReadyListener readyListener) {
		super(context);
		this.readyListener = readyListener;
		this.name = name;
		this.replyUrl = replyUrl;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);
        setTitle(name);
		submitButton = (Button)findViewById(R.id.submit_button);
		submitButton.setOnClickListener(new loginListener());
		text = (EditText)findViewById(R.id.text);
    }
	
	private class loginListener implements android.view.View.OnClickListener {
		 @Override
	     public void onClick(View v) {
			 if (replyUrl == null)
				 readyListener.ready(String.valueOf(text.getText()));
			 else
				 readyListener.ready(String.valueOf(text.getText()), replyUrl);
			 
			 dismiss();
		 }
	}
	
	public void onClick(View v) {
		if (v == submitButton) {
			dismiss();
		}
	}

}
