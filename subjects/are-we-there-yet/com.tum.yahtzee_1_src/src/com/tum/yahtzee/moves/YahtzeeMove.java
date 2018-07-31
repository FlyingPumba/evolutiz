package com.tum.yahtzee.moves;

import java.util.LinkedList;
import java.util.List;

import com.tum.yahtzee.units.Cube;

public class YahtzeeMove implements IBaseMove {

	public static boolean validate(List<Cube> cubes)
	{
		List<Integer> items = new LinkedList<Integer>();
		for(Cube cube : cubes) 
		{  
			if (!items.contains(cube.getNumber()))
			{
				items.add(cube.getNumber());
			}
		}
		return items.size() == 1;
	}
	
	public static int calculatePoints(List<Cube> cubes)
	{
		return 50;
	}
	
	public YahtzeeMove(List<Cube> cubes)
	{
		
	}
	
	public int getPoints() {
		return YahtzeeMove.calculatePoints(null);
	}

	public void print() {
		System.out.println("Yahtzee Move, Points: "+getPoints());
	}

}
