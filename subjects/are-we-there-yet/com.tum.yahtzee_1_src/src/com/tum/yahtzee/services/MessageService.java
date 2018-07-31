package com.tum.yahtzee.services;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class MessageService {
	public static void showMessage(Context context, String message)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setCancelable(false);
		alertDialog.setMessage(message);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialog.show();
	}
	
	public static void showMessage(Context context, String message, final MethodPointer methodPointer)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setCancelable(false);
		alertDialog.setMessage(message);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				methodPointer.execute();
			}
		});
		alertDialog.show();
	}
}
