package app.game;

import java.util.ArrayList;
import java.util.List;

import app.fileio.GameFile;

public class GameReplay extends Game {
	
	private List<Double[]> replayShots = new ArrayList<Double[]>();
	private Game originalGame;
	
	public GameReplay(Drawer drawer, List<Double[]> replayShots) {
		super(drawer);
		this.replayShots = replayShots;
	}
	
	public GameReplay(Drawer drawer, Game game) {
		super(drawer);
		this.replayShots = game.getShots();
		this.originalGame = game;
	}

	public GameReplay(Drawer drawer, GameFile file) {
		super(drawer, file);
		this.replayShots = file.getShots();
	}
	
	@Override
	public void update() {
		super.update();
		
		if (replayShots.isEmpty() && isAllIdle()) {
			stop();
			if (originalGame != null) originalGame.start();
		}
		else if (isTurn() && isAllIdle()) {
			Double[] shot = replayShots.remove(0);
			addLostBall();
			mainBall.setPosition(shot[0], shot[1]);
			loadShot(shot[2], shot[3]);
			releaseShot(shot[4], shot[5]);
		}
	}
	
	@Override
	public void draw() {
		super.draw();
		
		drawer.drawReplayR(getWidth() - 40, 40);
	}
}
