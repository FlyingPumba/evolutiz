package hiof.enigma.android.gestures;

import java.util.ArrayList;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;
import demo.killerud.gestures.R;

/**
 * Quick demo of the gestures API for Android. Consult
 * http://developer.android.com/resources/articles/gestures.html for detailed,
 * step-by-step instructions.
 * 
 * Gestures to be used in the application must be built with the emulator's
 * Gestures Builder or the Gesture Tool app:
 * https://market.android.com/details?id=com.davemac327.gesture.tool
 * 
 * The file /sdcard/gestures created by either tool must be moved to the project
 * /res/raw folder. This can be done by opening the DDMS perspective
 * (Window->Open perspective->DDMS) and using the file explorer. Save the
 * gesture file to disk by pressing the floppy disk in the top-right.
 * 
 * The gestures area, usually brought forth by a button or other command, opens
 * for simple, one-finger shapes to be drawn and recognized by the app in order
 * to do stuff. In the case of this demo, the only gesture built is a square,
 * and upon recognition, the demo changes the screen orientation.
 * 
 * The API opens up for recognition of specific gestures by the name given in
 * the Gesture Tool, running different blocks of code for the different
 * gestures.
 * 
 * @author William Killerud
 * 
 */
public class GesturesDemoActivity extends Activity implements
		OnGesturePerformedListener
{

	private GestureLibrary mLibrary;
	private Display display;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/*
		 * Used for getting the current orientation of the screen, so that we
		 * can change it later with toggleOrientation()
		 */
		display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();

		/* Get the gestures we created using the Gesture Tool */
		mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!mLibrary.load())
		{
			finish(); // If we can't load the library we quit the application
		}

		/*
		 * Create an object instance of a screen overlay that accepts gestures.
		 * We defined this layout in /res/layout/main.xml
		 */
		GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
		gestures.addOnGesturePerformedListener(this);
	}

	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture)
	{

		/* Creates an arraylist of different suggestions for recognitions */
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

		/*
		 * Sometimes the API doesn't find a thing. In that case, the app skips
		 * this block.
		 */
		if (predictions.size() > 0)
		{
			/*
			 * Gets the first prediction and checks its score. If it's low,
			 * skips the next block.
			 */
			Prediction prediction = predictions.get(0);
			if (prediction.score > 1.0)
			{

				/*
				 * Uses the build-in Toast class to create a small message to
				 * the user that we recognize the gesture! :D
				 */
				Toast.makeText(
						getApplicationContext(),
						"Hey, I recognize that gesture! It's "
								+ prediction.name + "!", Toast.LENGTH_SHORT)
						.show();

				if (prediction.name.equals("square"))
				{
					// Do stuff for this gesture
				} else if (prediction.name.equals("circle"))
				{
					// Do other stuff for this other gesture
				}
				toggleOrientation();
			}
		}
	}

	/**
	 * Here we use the Display object we created to check what orientation the
	 * screen is in. If it is Landscape, we put it in Portrait and vice versa.
	 */
	private void toggleOrientation()
	{
		/*
		 * For those of you who have not seen a switch before, this basically
		 * replaces a whole bunch of if-tests when what we're testing against is
		 * a number range with specific values.
		 * 
		 * It's a very neat tool!
		 * 
		 * If you want to learn more, research Switch, and Enumerator (Enum for
		 * short).
		 */
		switch (display.getRotation())
		{
		case Surface.ROTATION_0:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		case Surface.ROTATION_180:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		case Surface.ROTATION_270:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Surface.ROTATION_90:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		}
		/*
		 * Note the break-statement for each case above. This is very important!
		 * Without it the switch goes through every case until it either breaks
		 * or finishes!
		 * 
		 * Note that return is a valid replacement for break, should you need a
		 * value returned.
		 */
	}
}