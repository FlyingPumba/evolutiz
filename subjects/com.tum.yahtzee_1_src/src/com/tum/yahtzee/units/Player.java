package com.tum.yahtzee.units;

import java.util.LinkedList;
import java.util.List;

import com.tum.yahtzee.moves.ChanceMove;
import com.tum.yahtzee.moves.DummyMove;
import com.tum.yahtzee.moves.FourOfAKindMove;
import com.tum.yahtzee.moves.FullHouseMove;
import com.tum.yahtzee.moves.IBaseMove;
import com.tum.yahtzee.moves.LargeStraightMove;
import com.tum.yahtzee.moves.NumberMove;
import com.tum.yahtzee.moves.SmallStraightMove;
import com.tum.yahtzee.moves.ThreeOfAKindMove;
import com.tum.yahtzee.moves.YahtzeeMove;

public class Player {
	private List<Cube> cubes = new LinkedList<Cube>();
	private List<IBaseMove> moves = new LinkedList<IBaseMove>();
	private List<String> lockedMoves = new LinkedList<String>(); //moves locked by yahtzee moves or locked because there was no other possibillity
	private int finalPoints = -1;
	private int shaken = 1;
	
	private String selectedMove = "Number 1"; //Number 1 is the default in the spinner view
	
	private String name;
	
	public Player(String name)
	{
		this.name = name;
		for(int i=0;i<5;i++)
		{
			cubes.add(new Cube());
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public void unlockCubes()
	{
		for(Cube cube : cubes)
		{
			cube.unflag();
		}
	}
	
	public List<String> getMoves()
	{
		List<String> possibleMoves = new LinkedList<String>();
		for(int i=0;i<6;i++) {
			possibleMoves.add("Number "+(i+1));
		}
		possibleMoves.add("ThreeOfAKind");
		possibleMoves.add("FourOfAKind");
		possibleMoves.add("FullHouse");
		possibleMoves.add("SmallStraight");
		possibleMoves.add("LargeStraight");
		possibleMoves.add("Yahtzee");
		possibleMoves.add("Chance");
		
		return possibleMoves;
	}
	
	public String getSelectedMove()
	{
		return selectedMove;
	}
	
	public void setSelectedMove(String selectedMove)
	{
		this.selectedMove = selectedMove;
	}
	
	public boolean shake()
	{
		if (shaken == 3) {
			return false;
		} else {
			for(Cube cube : cubes)
			{
				cube.shake();
			}
			shaken++;
			return true;
		}
	}
	
	public List<Cube> getCubes()
	{
		return cubes;
	}
	
	public int getPointsForSelectedMove()
	{
		return getPointsForMove(selectedMove);
	}
	
	public int getPointsForMove(String move)
	{
		if (move.startsWith("Number")) {
			int nr = new Integer(move.substring("Number ".length()));
			if (NumberMove.validate(cubes, nr-1) && !numberMoveAlreadyDone(nr-1)) {
				return NumberMove.calculatePoints(cubes, nr-1);
			}
		} else if (move.equals("ThreeOfAKind")) {
			if (ThreeOfAKindMove.validate(cubes) && !moveAlreadyDone(ThreeOfAKindMove.class)) {
				return ThreeOfAKindMove.calculatePoints(cubes);
			}
		} else if (move.equals("FourOfAKind")) {
			if (FourOfAKindMove.validate(cubes) && !moveAlreadyDone(FourOfAKindMove.class)) {
				return FourOfAKindMove.calculatePoints(cubes);
			}
		} else if (move.equals("FullHouse")) {
			if (FullHouseMove.validate(cubes) && !moveAlreadyDone(FullHouseMove.class)) {
				return FullHouseMove.calculatePoints(cubes);
			}
		} else if (move.equals("SmallStraight")) {
			if (SmallStraightMove.validate(cubes) && !moveAlreadyDone(SmallStraightMove.class)) {
				return SmallStraightMove.calculatePoints(cubes);
			}
		} else if (move.equals("LargeStraight")) {
			if (LargeStraightMove.validate(cubes) && !moveAlreadyDone(LargeStraightMove.class)) {
				return LargeStraightMove.calculatePoints(cubes);
			}
		} else if (move.equals("Yahtzee")) {
			if (YahtzeeMove.validate(cubes)) { // && moveAlreadyDone(YahtzeeMove.class)) { yahtzee can be made more than once
				return YahtzeeMove.calculatePoints(cubes);
			}
		} else if (move.equals("Chance")) {
			if (ChanceMove.validate(cubes) && !moveAlreadyDone(ChanceMove.class)) {
				return ChanceMove.calculatePoints(cubes);
			}
		}
		return -1;
	}
	
	public boolean isSelectedMovePossible()
	{
		return getPointsForSelectedMove() != -1;
	}
	
	public void doSelectedMove()
	{
		//app should check isSelectedMovePossible before executing this
		
		//to be implemented
		if (selectedMove.startsWith("Number")) {
			int nr = new Integer(selectedMove.substring("Number ".length()));
			if (NumberMove.validate(cubes, nr-1) && !numberMoveAlreadyDone(nr-1)) {
				moves.add(new NumberMove(cubes, nr-1));
			}
		} else if (selectedMove.equals("ThreeOfAKind")) {
			if (ThreeOfAKindMove.validate(cubes) && !moveAlreadyDone(ThreeOfAKindMove.class)) {
				moves.add(new ThreeOfAKindMove(cubes));
			}
		} else if (selectedMove.equals("FourOfAKind")) {
			if (FourOfAKindMove.validate(cubes) && !moveAlreadyDone(FourOfAKindMove.class)) {
				moves.add(new FourOfAKindMove(cubes));
			}
		} else if (selectedMove.equals("FullHouse")) {
			if (FullHouseMove.validate(cubes) && !moveAlreadyDone(FullHouseMove.class)) {
				moves.add(new FullHouseMove(cubes));
			}
		} else if (selectedMove.equals("SmallStraight")) {
			if (SmallStraightMove.validate(cubes) && !moveAlreadyDone(SmallStraightMove.class)) {
				moves.add(new SmallStraightMove(cubes));
			}
		} else if (selectedMove.equals("LargeStraight")) {
			if (LargeStraightMove.validate(cubes) && !moveAlreadyDone(LargeStraightMove.class)) {
				moves.add(new LargeStraightMove(cubes));
			}
		} else if (selectedMove.equals("Yahtzee")) {
			if (YahtzeeMove.validate(cubes)) { // && moveAlreadyDone(YahtzeeMove.class)) { yahtzee can be made more than once
				moves.add(new YahtzeeMove(cubes));
			}
		} else if (selectedMove.equals("Chance")) {
			if (ChanceMove.validate(cubes) && !moveAlreadyDone(ChanceMove.class)) {
				moves.add(new ChanceMove(cubes));
			}
		}
		
		//cleanup for next round:
		unlockCubes();
		shaken = 0;
		shake(); //generate new random numbers for next round
		selectedMove = "Number 1"; //Number 1 is the default in the spinner view
	}
	
	public String specialMoveRequired()
	{
		String message = "";
		
		if (this.selectedMove.equals("Yahtzee") && isSelectedMovePossible() && moveAlreadyDone(YahtzeeMove.class) && anyMoveUnused())
		{
			return "Select a Field where you want to save another YahtzeeMove.";
		} else if(!anyMovePossible() && anyMoveUnused()) {
			return "Select a Field you want to lock. (Required because there aren't any other options you have.)";
		}
		
		return message;
	}
	
	public void doSpecialMove(String move)
	{
		if (this.selectedMove == "Yahtzee" && isSelectedMovePossible() && moveAlreadyDone(YahtzeeMove.class) && anyMoveUnused())
		{
			lockedMoves.add(move+"Move"); //+"Move" to fit to class name
			moves.add(new YahtzeeMove(cubes));
		} else if(anyMovePossible() && anyMoveUnused()) {
			lockedMoves.add(move+"Move");
			moves.add(new DummyMove());
		}		
		unlockCubes();
		shaken = 0;
		shake(); //generate new random numbers for next round
		selectedMove = "Number 1"; //Number 1 is the default in the spinner view
	}
	
	public List<String> getUnusedMoves()
	{
		List<String> unusedMoves = new LinkedList<String>();
		
		for(int i=0;i<6;i++) {
			if (!numberMoveAlreadyDone(i)) {
				unusedMoves.add("Number "+(i+1));
			}
		}
		
		if (!moveAlreadyDone(SmallStraightMove.class)) unusedMoves.add("SmallStraight");
		
		if (!moveAlreadyDone(LargeStraightMove.class)) unusedMoves.add("LargeStraight");
		
		if (!moveAlreadyDone(FullHouseMove.class)) unusedMoves.add("FullHouse");
		
		if (!moveAlreadyDone(YahtzeeMove.class)) unusedMoves.add("Yahtzee");
		
		if (!moveAlreadyDone(ThreeOfAKindMove.class)) unusedMoves.add("ThreeOfAKind");
		
		if (!moveAlreadyDone(FourOfAKindMove.class)) unusedMoves.add("FourOfAKind");
		
		if (!moveAlreadyDone(ChanceMove.class)) unusedMoves.add("Chance");
		
		
		return unusedMoves;
	}
	
	public int getPoints()
	{
		if (finalPoints == -1) {
			int numbermoves = 0;
			int specialmoves = 0;
			
			for(IBaseMove move : moves)
			{
				if (move != null) {
					if (move.getClass() == NumberMove.class)
					{
						numbermoves += move.getPoints();
					} else {
						specialmoves += move.getPoints();
					}
				}
			}
			
			if (numbermoves >= 63) {
				numbermoves += 35;
			}
			finalPoints = numbermoves + specialmoves;
		}
		return finalPoints;
	}
	
	private boolean moveAlreadyDone(Class<?> cl) {
		for (int i=0;i<moves.size();i++)
		{
			if (moves.get(i).getClass() == cl && !lockedMoves.contains(cl.getName()) && anyMoveUnused()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean numberMoveAlreadyDone(int nr) {
		for (int i=0;i<moves.size();i++) {
			if (moves.get(i).getClass() == NumberMove.class) {
				NumberMove numberMove = (NumberMove)moves.get(i);
				String name = "Number "+(nr+1);
				if (numberMove.getNumber() == nr && !lockedMoves.contains(name) && anyMoveUnused()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean anyMoveUnused()
	{
		return moves.size() <= 13;  //13 moves are the maximum possible moves
	}
	
	public boolean anyMovePossible()
	{
		if (anyMoveUnused()) 
		{
			for (int i=0;i<6;i++) {
				if (NumberMove.validate(cubes, i) && !numberMoveAlreadyDone(i)) {
					return true;
				}
			}
			if (ThreeOfAKindMove.validate(cubes) && !moveAlreadyDone(ThreeOfAKindMove.class)) {
				moves.add(new ThreeOfAKindMove(cubes));
			}
			if (FourOfAKindMove.validate(cubes) && !moveAlreadyDone(FourOfAKindMove.class)) {
				moves.add(new FourOfAKindMove(cubes));
			}
			if (FullHouseMove.validate(cubes) && !moveAlreadyDone(FullHouseMove.class)) {
				moves.add(new FullHouseMove(cubes));
			}
			if (SmallStraightMove.validate(cubes) && !moveAlreadyDone(SmallStraightMove.class)) {
				moves.add(new SmallStraightMove(cubes));
			}
			if (LargeStraightMove.validate(cubes) && !moveAlreadyDone(LargeStraightMove.class)) {
				moves.add(new LargeStraightMove(cubes));
			}
			if (YahtzeeMove.validate(cubes)) { // && moveAlreadyDone(YahtzeeMove.class)) { yahtzee can be made more than once
				moves.add(new YahtzeeMove(cubes));
			}
			if (ChanceMove.validate(cubes) && !moveAlreadyDone(ChanceMove.class)) {
				moves.add(new ChanceMove(cubes));
			}
		}
		return false;
	}
}
