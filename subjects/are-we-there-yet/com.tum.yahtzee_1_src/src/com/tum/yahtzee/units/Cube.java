package com.tum.yahtzee.units;

public class Cube {
	
	private int number;
	private boolean used;
	
	public Cube()
	{
		unflag();
		shake();
	}
	
	public void shake()
	{
		if (!used) {
			number = (int)Math.round(Math.random() * 5.0); // 0-5 = 6 possibilities
		}
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public void setNumber(int number)
	{
		this.number = number;
	}
	
	public void setUsed(boolean used)
	{
		this.used = used;
	}
	
	public boolean getUsed()
	{
		return used;
	}
	
	public void unflag()
	{
		this.used = false;
	}
}
