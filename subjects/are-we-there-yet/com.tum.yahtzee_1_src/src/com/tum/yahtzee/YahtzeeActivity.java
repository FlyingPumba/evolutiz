package com.tum.yahtzee;

import com.tum.yahtzee.R;
import com.tum.yahtzee.services.MessageService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class YahtzeeActivity extends Activity {
	
	private EditText playerAmountText;
	private EditText roundsText;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TextView titleView = (TextView)findViewById(R.id.game_title);
        titleView.setText("Yahtzee");
        
        TextView playerTitleView = (TextView)findViewById(R.id.player_title);
        playerTitleView.setText("Amount of Players:");
        
        TextView roundTitle = (TextView)findViewById(R.id.game_rounds_title);
        roundTitle.setText("Amount of Rounds:");
        
        playerAmountText = (EditText)findViewById(R.id.player_amount);
        roundsText = (EditText)findViewById(R.id.game_rounds);
        
        Button playButton = (Button)findViewById(R.id.play_button);
        playButton.setText("Play");
        playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				play();
			}
		});
    }
    
    public void play()
    {
    	String sPlayers = playerAmountText.getText().toString();
    	String sRounds = roundsText.getText().toString();
    	
    	if (sPlayers == null || sPlayers.isEmpty() || new Integer(sPlayers) == 0 || sRounds == null || sRounds.isEmpty() || new Integer(sRounds) == 0) {
    		MessageService.showMessage(YahtzeeActivity.this, "Amount of Rounds and Players must not be zero.");
    	} else {
	    	GameController.get().newGame(new Integer(sPlayers), new Integer(sRounds));
	    	
	    	Intent intent = new Intent(YahtzeeActivity.this, GameActivity.class);
	    	startActivity(intent);
    	}
	}
}