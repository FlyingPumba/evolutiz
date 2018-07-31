package i4nc4mp.myLock;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class DummyPrompt extends Activity {
	//When the phone app stops us, it means it is time to launch the call prompt
	//sometimes phone launches quicker, so it gives us focus on top instead of stopping us
	//that case fixes itself when user hits back to close the dummy	
	
	private boolean done = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.dummy);
		
		Log.v("dummy prompt","on create");		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		Log.v("dummy prompt","start");
	}
	
	public void iAmDone() {
		if (!done) {
			done = true;
			CallPrompt.launch(getApplicationContext());
			moveTaskToBack(true);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Log.v("dummy prompt","pause");
	}
	
	//Expected to start, resume, pause, then stop
	//sometimes we get focus then pause and lose it before stop
	//other times pause is immediate after start/resume
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		//Log.v("dummy prompt","Focus is now " + hasFocus);
		
		if (hasFocus) Log.v("dummy prompt","gained focus");
	}
	
	
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Log.v("dummy prompt","resume");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		Log.v("dummy prompt","stop - launching real Prompt");
		
		iAmDone();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.v("dummy prompt","on destroy");
	}
}