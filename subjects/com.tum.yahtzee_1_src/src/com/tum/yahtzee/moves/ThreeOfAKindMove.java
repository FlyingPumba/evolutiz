package com.tum.yahtzee.moves;

import java.util.List;

import com.tum.yahtzee.units.Cube;

public class ThreeOfAKindMove implements IBaseMove {
	private int points;
	
	public static boolean validate(List<Cube> cubes)
	{
		int[] values = new int[6];
		for(int i=0;i<6;i++) { values[i] = 0; }
		for(Cube cube : cubes) { values[cube.getNumber()]++; }
		
		for(int i=0;i<6;i++)
		{
			if (values[i] >= 3) return true;
		}
		return false;
	}
	
	public static int calculatePoints(List<Cube> cubes)
	{
		int points = 0;
		for(Cube cube : cubes)
		{
			points += cube.getNumber();
		}
		return points;
	}
	
	public ThreeOfAKindMove(List<Cube> cubes)
	{
		points = ThreeOfAKindMove.calculatePoints(cubes);
	}
	
	public int getPoints()
	{
		return points;
	}
	
	public void print()
	{
		System.out.println("ThreeOfAKind, Points: "+getPoints());
	}
}
