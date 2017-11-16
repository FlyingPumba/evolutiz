package com.leafdigital.kanji.android.EmmaInstrument;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.os.Environment;
import java.io.File;

public class SMSInstrumentedReceiver extends BroadcastReceiver {
                public static String TAG = "M3SMSInstrumentedReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "Hola, recibi el broadcast");
			File coverageFile = new File(context.getFilesDir(), "coverage.ec");
			String path = coverageFile.getAbsolutePath();
			Log.v(TAG, "Voy a escribir el archivo de coverage en: " + path);
			Bundle extras = intent.getExtras();
                        FinishListener mListener = new EmmaInstrumentation();
			if (mListener != null) {
                                mListener.dumpIntermediateCoverage(path);
			}
		}

}
