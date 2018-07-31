package com.tum.yahtzee;

import java.util.List;

import com.tum.yahtzee.R;
import com.tum.yahtzee.listeners.OnCubeClickListener;
import com.tum.yahtzee.services.MessageService;
import com.tum.yahtzee.services.MethodPointer;
import com.tum.yahtzee.units.Cube;
import com.tum.yahtzee.units.Player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class GameActivity extends Activity {
	
	private TextView playerText;
	private TextView possibilityText;
	private TextView pointsText;
	
	private ImageButton[] cubeButtons = new ImageButton[5];
	
	private Button shakeButton;
	private Button saveButton;
	
	private Spinner moveSpinner;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.game);
    	
    	playerText = (TextView)findViewById(R.id.player_nr_header);
    	possibilityText = (TextView)findViewById(R.id.possibilityText);
    	pointsText = (TextView)findViewById(R.id.pointsText);
    	
    	cubeButtons[0] = (ImageButton)findViewById(R.id.cubeButton1);
		cubeButtons[1] = (ImageButton)findViewById(R.id.cubeButton2);
		cubeButtons[2] = (ImageButton)findViewById(R.id.cubeButton3);
		cubeButtons[3] = (ImageButton)findViewById(R.id.cubeButton4);
		cubeButtons[4] = (ImageButton)findViewById(R.id.cubeButton5);
    	
    	for(int i=0;i<5;i++)
    	{
    		cubeButtons[i].setOnClickListener(new OnCubeClickListener(i) {
    			@Override
    			public void onClick(View v) {
    				super.onClick(v);
    				updateCubes();
    			}
    		});
    	}
    	
    	shakeButton = (Button)findViewById(R.id.shakeButton);
    	saveButton = (Button)findViewById(R.id.continueButton);
    	
    	moveSpinner = (Spinner)findViewById(R.id.spinner1);
    	
    	shakeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				if(!GameController.get().getCurrentPlayer().shake())
				{
					MessageService.showMessage(GameActivity.this, "You can only shake 2 times. (2 because first time is used to generate the random cubes.)");
				}
				updateCubes();
			}
		});
    	
    	saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveAndContinue();
			}
		});
    	
    	update();
    }
    
    private void update()
    {
    	Player player = GameController.get().getCurrentPlayer();
    	playerText.setText(player.getName());
    	
    	
    	String[] moves = player.getMoves().toArray(new String[]{});
    	
    	moveSpinner.setAdapter(new ArrayAdapter<String>(GameActivity.this, android.R.layout.simple_spinner_item, moves));
    	moveSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				GameController.get().getCurrentPlayer().setSelectedMove((String)arg0.getItemAtPosition(arg2));
				updateMoveDetails();
			}

			public void onNothingSelected(AdapterView<?> arg0) { }
    		
		});
    	
    	updateCubes();
    }
    
    private void updateCubes()
    {
    	Player player = GameController.get().getCurrentPlayer();
    	List<Cube> cubes = player.getCubes();
    	
    	setImage(cubes.get(0).getUsed(), 0, cubes.get(0).getNumber());
    	setImage(cubes.get(1).getUsed(), 1, cubes.get(1).getNumber());
    	setImage(cubes.get(2).getUsed(), 2, cubes.get(2).getNumber());
    	setImage(cubes.get(3).getUsed(), 3, cubes.get(3).getNumber());
    	setImage(cubes.get(4).getUsed(), 4, cubes.get(4).getNumber());
    }
    
    private void updateMoveDetails()
    {
    	Player player = GameController.get().getCurrentPlayer();
    	possibilityText.setText((player.isSelectedMovePossible() ? "yes" : "no"));
    	int points = player.getPointsForSelectedMove();
    	pointsText.setText(""+(points != -1 ? points : 0));
    }
    
    private void saveAndContinue()
    {
    	//only for debugging:
    	Player player = GameController.get().getCurrentPlayer();
    	
    	//validate input
    	if (!player.isSelectedMovePossible()) {
    		MessageService.showMessage(GameActivity.this, "The selected move isn't possible!");
    		return;
    	}
    	
    	//do move
    	String msg = player.specialMoveRequired();
    	if (!msg.equals("") && msg != null) {
    		//show dialog with moves
    		final List<String> emptyMoves = player.getUnusedMoves();
    		CharSequence[] moveArray = emptyMoves.toArray(new CharSequence[]{});
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(msg);
    		builder.setItems(moveArray, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface arg0, int arg1) {
					GameController.get().getCurrentPlayer().doSpecialMove(emptyMoves.get(arg1));
					arg0.dismiss();
				}
			});
    		builder.show();
    	} else {
    		if (!player.anyMovePossible()) {
    			MessageService.showMessage(GameActivity.this, "No move possible. Continuing with next user or finishing game now if this was the last round.");
    		} else {
    			player.doSelectedMove();
    		}
    	}
    	//move to next player/round
    	if (GameController.get().next()) {
    		//update
    		update();
    	} else {
    		MessageService.showMessage(GameActivity.this, "Game finished! "+GameController.get().winner().getName()+" won the game.", new MethodPointer() {
    			@Override
    			public void execute(){
    				closeActivity();
    			}
    		});
    	}
    	
    }
    
    private void closeActivity()
    {
    	this.finish();
    }
    
    private void setImage(boolean used, int id, int number)
    {
    	if (!used) {
    		switch(number)
    		{
    		case 0: cubeButtons[id].setImageResource(R.drawable.cube1); break;
    		case 1: cubeButtons[id].setImageResource(R.drawable.cube2); break;
    		case 2: cubeButtons[id].setImageResource(R.drawable.cube3); break;
    		case 3: cubeButtons[id].setImageResource(R.drawable.cube4); break;
    		case 4: cubeButtons[id].setImageResource(R.drawable.cube5); break;
    		case 5: cubeButtons[id].setImageResource(R.drawable.cube6); break;
    		}
    	} else {
    		switch(number)
    		{
    		case 0: cubeButtons[id].setImageResource(R.drawable.selectedcube1); break;
    		case 1: cubeButtons[id].setImageResource(R.drawable.selectedcube2); break;
    		case 2: cubeButtons[id].setImageResource(R.drawable.selectedcube3); break;
    		case 3: cubeButtons[id].setImageResource(R.drawable.selectedcube4); break;
    		case 4: cubeButtons[id].setImageResource(R.drawable.selectedcube5); break;
    		case 5: cubeButtons[id].setImageResource(R.drawable.selectedcube6); break;
    		}
    	}
    }
}
