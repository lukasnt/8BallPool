package app.fileio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.entities.Ball;
import app.entities.Player;
import app.game.Game;

public class GameFile implements Serializable {
	
	private Player p1, p2;
	private List<Ball> balls;

	private boolean foul = true;
	private boolean firstPot = false;
	private boolean point = false;
	private boolean turn = true;
	
	private List<Double[]> shots = new ArrayList<Double[]>();
	
	public GameFile(final List<Ball> balls, final Player p1, final Player p2, final List<Boolean> states, final List<Double[]> shots) {
		this.balls = balls;
		
		this.p1 = p1;
		this.p2 = p2;
		
		this.foul = states.get(0);
		this.firstPot = states.get(1);
		this.point = states.get(2);
		this.turn = states.get(3);
		
		this.shots = shots;
	}
	
	public GameFile(Game game) {
		this(game.getBalls(), game.getP1(), game.getP2(), 
				new ArrayList<Boolean>(Arrays.asList(game.isFoul(), game.isFirstPot(), game.isPoint(), game.isTurn())),
				game.getShots());
	}
	
	public Player getP1() {
		Player nP1 = new Player(p1.getName(), null);
		nP1.setTurn(p1.isTurn());
		nP1.setType(p1.getType());
		
		return nP1;
	}
	
	public Player getP2() {
		Player nP2 = new Player(p2.getName(), null);
		nP2.setTurn(p2.isTurn());
		nP2.setType(p2.getType());
		
		return nP2;
	}
	
	public List<Ball> getBalls() {
		return new ArrayList<Ball>(balls);
	}
	
	public List<Double[]> getShots(){
		return new ArrayList<Double[]>(shots);
	}

	public boolean isFoul() {
		return foul;
	}

	public boolean isFirstPot() {
		return firstPot;
	}

	public boolean isPoint() {
		return point;
	}

	public boolean isTurn() {
		return turn;
	}
}
