package com.tum.yahtzee;

import java.util.LinkedList;
import java.util.List;

import com.tum.yahtzee.units.Player;

public class GameController {
	
	private static GameController instance = new GameController();
	public static GameController get() { return instance; }
	
	private int rounds;
	private int currentRound;
	private int currentPlayer;
	
	private List<Player> players = new LinkedList<Player>();
	
	private GameController()
	{
	
	}
	
	public void newGame(int players, int rounds)
	{
		this.rounds = rounds;
		currentRound = 0;
		currentPlayer = 0;
		
		this.players.clear();
		
		for(int i=0;i<players;i++)
		{
			this.players.add(new Player("Player "+(i+1)));
		}
	}
	
	public Player getCurrentPlayer()
	{
		return players.get(currentPlayer);
	}
	
	/*
	 * returns false if game is finished
	 */
	public boolean next()
	{
		if (currentPlayer+1 >= players.size()) {	
			if (currentRound+1 == rounds)
			{
				return false;
			} else {
				currentPlayer = 0;
				for(Player player : players)
				{
					player.unlockCubes();
				}
				currentRound++;
			}
		} else {
			for(Player player : players)
			{
				player.unlockCubes();
			}
			currentPlayer++;
		}

		return true;
	}
	
	public int getRound()
	{
		return currentRound;
	}
	
	public Player winner()
	{
		List<Player> winners = new LinkedList<Player>(players); //don't want to resort the old one
		for(int i=0;i<winners.size()-1;i++) {
			boolean exchanged = false;
			for(int k=0;k<(winners.size()-1-i);k++) {
				if (winners.get(k).getPoints() < winners.get(k+1).getPoints()) { //lowest at the end will result in winner at position 0
					Player swap = winners.get(k);
					winners.set(k, winners.get(k+1));
					winners.set(k+1, swap);
					exchanged = true;
				}
			}
			if (!exchanged) break; //improve performance, finished sorting if nothing has been exchanged
		}
		
		return winners.get(0);
	}
	
}
