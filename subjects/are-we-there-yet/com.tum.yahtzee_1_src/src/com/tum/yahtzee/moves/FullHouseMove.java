package com.tum.yahtzee.moves;

import java.util.List;

import com.tum.yahtzee.units.Cube;

public class FullHouseMove implements IBaseMove {
	public static boolean validate(List<Cube> cubes)
	{
		int[] values = new int[6];
		for(int i=0;i<6;i++) { values[i] = 0; }
		for (Cube cube : cubes)
		{
			values[cube.getNumber()]++;
		}
		
		boolean foundTwo = false;
		boolean foundThree = false;
		
		for(int i=0;i<6;i++) {
			if (values[i] == 2) foundTwo = true;
			if (values[i] == 3) foundThree = true;
		}
		return foundTwo && foundThree;
	}
	
	public static int calculatePoints(List<Cube> cubes)
	{
		return 25;
	}
	
	public FullHouseMove(List<Cube> cubes)
	{
		
	}
	
	public int getPoints()
	{
		return FullHouseMove.calculatePoints(null);
	}
	
	public void print()
	{
		System.out.println("FullHouse, Points: "+getPoints());
	}
}
