package com.tum.yahtzee.moves;

import java.util.List;

import com.tum.yahtzee.units.Cube;

public class ChanceMove implements IBaseMove
{
	private int points;

	public static boolean validate(List<Cube> cubes)
	{
		return true;
	}
	
	public static int calculatePoints(List<Cube> cubes)
	{
		int points = 0;
		for (Cube cube : cubes)
		{
			points += (cube.getNumber()+1);
		}
		return points;
	}
	
	public ChanceMove(List<Cube> cubes)
	{
		this.points = ChanceMove.calculatePoints(cubes);
	}
	
	public int getPoints() {
		return points;
	}

	public void print() {
		System.out.println("Chance, Points: "+points);
	}

}
