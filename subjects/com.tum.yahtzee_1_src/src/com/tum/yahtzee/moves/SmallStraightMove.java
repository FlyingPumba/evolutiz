package com.tum.yahtzee.moves;

import java.util.List;

import com.tum.yahtzee.units.Cube;

public class SmallStraightMove implements IBaseMove {

	public static boolean validate(List<Cube> cubes)
	{
		int values = 0;
		for(Cube cube : cubes)
		{
			switch(cube.getNumber())
			{
			case 0: if((values & 1) == 0) values += 1; break;
			case 1: if((values & 2) == 0) values += 2; break;
			case 2: if((values & 4) == 0) values += 4; break;
			case 3: if((values & 8) == 0) values += 8; break;
			case 4: if((values & 16) == 0) values += 16; break;
			case 5: if((values & 32) == 0) values += 32; break;
			default: return false;
			}
		}
		return values == 15 || values == 30 || values == 60 || values == 31 || values == 62;
	}
	
	public static int calculatePoints(List<Cube> cubes)
	{
		return 30;
	}
	
	public SmallStraightMove(List<Cube> cubes)
	{
		
	}
	
	public int getPoints() {
		return SmallStraightMove.calculatePoints(null);
	}
	
	public void print() {
		System.out.println("Small Straight, Points: "+getPoints());
	}

}
