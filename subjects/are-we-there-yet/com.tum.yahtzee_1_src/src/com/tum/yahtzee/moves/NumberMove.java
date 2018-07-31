package com.tum.yahtzee.moves;

import java.util.List;

import com.tum.yahtzee.units.Cube;

public class NumberMove implements IBaseMove {
	
	private int cubeCount;
	private int number;
	
	public static boolean validate(List<Cube> cubes, int number)
	{
		for(Cube cube : cubes)
		{
			if (cube.getNumber() == number)
			{
				return true;
			}
		}
		return false;
	}
	
	public static int calculatePoints(List<Cube> cubes, int number)
	{
		NumberMove move = new NumberMove(cubes, number);
		return move.getPoints();
	}
	
	public NumberMove(List<Cube> cubes, int number)
	{
		cubeCount = 0;
		for(Cube cube : cubes)
		{
			if (cube.getNumber() == number)
			{
				cubeCount++;
			}
		}
		this.number = number;
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public int getPoints()
	{
		return cubeCount * (number+1);
	}
	
	public void print()
	{
		System.out.println(cubeCount+"x "+(number+1)+"s, Points: "+getPoints());
	}
}
