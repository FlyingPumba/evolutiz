package hiof.enigma.android.soundboard;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * An example showcasing the creation of a very simple soundboard, using only
 * the default button layouts.
 * 
 * This is the main class, as specified in AndroidManifest.xml. This class is
 * what is run first when the application is started.
 * 
 * @author William Killerud
 * 
 */
public class Soundboard extends Activity
{
	private static MediaPlayer mediaplayer;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		/*
		 * Determines what layout file the activity will use. In our case,
		 * soundboard.
		 */
		setContentView(R.layout.soundboard);

		/*
		 * Creates instances of the buttons from our layout file in code
		 */
		Button coin = (Button) findViewById(R.id.Coin);
		Button tube = (Button) findViewById(R.id.Tube);

		/*
		 * Create the listeners for each of our buttons, and determine what to
		 * do on click
		 */
		coin.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				/*
				 * Creates the media player with the wanted file, then starts it
				 * right away
				 */
				mediaplayer = MediaPlayer.create(getApplicationContext(),
						R.raw.coin);
				mediaplayer.start();
			}
		});

		tube.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				mediaplayer = MediaPlayer.create(getApplicationContext(),
						R.raw.tube);
				mediaplayer.start();
			}
		});

	}
}