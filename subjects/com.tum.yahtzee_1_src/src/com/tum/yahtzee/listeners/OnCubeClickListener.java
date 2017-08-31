package com.tum.yahtzee.listeners;

import com.tum.yahtzee.GameController;
import com.tum.yahtzee.units.Cube;

import android.view.View;
import android.view.View.OnClickListener;

public class OnCubeClickListener implements OnClickListener {
	
	public OnCubeClickListener(int cubeId)
	{
		this.cubeId = cubeId;
	}
	
	private int cubeId;
	
	public int getCubeId()
	{
		return cubeId;
	}
	
	public void onClick(View v) {
		Cube cube = GameController.get().getCurrentPlayer().getCubes().get(cubeId);
		cube.setUsed(!cube.getUsed());
	}

}
