package app.entities;

import app.game.Game;

public class Player {
	
	private Game game;
	private boolean turn = false;
	private int type = Ball.NONE;
	private String name;
	
	public Player(String name, Game game) {
		this.game = game;
		this.setName(name);
	}
	
	public Player(Player player) {
		// For å lage kopi av player
		this.game = player.game;
		this.name = player.name;
		this.turn = player.turn;
		this.type = player.type;
	}
	
	@Override
	public String toString() {
		return getName() + ": " 
			+ getTypeString() + ", "
			+ ("Left: "  + getBallsLeft())
			+ (isTurn()==true ? ", Turn" : "")
			+ (getBallsLeft()==0 ? ", WIN" : "");
	}
	
	public int getBallsLeft() {
		if (game == null) return 0;
		if (type == Ball.SOLID) return game.getSolidsLeft();
		else return game.getStripesLeft();
	}
	
	public int getType() {
		return type;
	}
	
	public String getTypeString() {
		switch (type) {
		case Ball.SOLID:
			return "Solid";
		case Ball.STRIPE:
			return "Stripe";
		case Ball.NONE:
			return "";
		}
		return "";
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public void setGame(Game game, int number) {
		// null-sjekk
		if (game == null) {
			this.game = game;
			return;
		}
		
		if (number == 1) {
			if (game.getP1().equals(this)) this.game = game;
			// Kan kun sette spiller en gang
			if (game.getP1() == null) {
				game.setP1(this);
			}
		}else if (number == 2) {
			if (game.getP2().equals(this)) this.game = game;
			// Kan kun sette spiller en gang
			if (game.getP2() == null) {
				game.setP2(this);
			}
		}
	}

	public boolean isTurn() {
		return turn;
	}

	public void setTurn(boolean turn) {
		this.turn = turn;
	}

	public Game getGame() {
		return game;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	public boolean equals(Player p) {
		return hashCode() == p.hashCode();
	}
}
