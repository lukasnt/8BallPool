package app.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import app.entities.Ball;
import app.entities.Player;
import app.fileio.GameFile;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Game extends BallEngine  {
	
	private Player p1;
	private Player p2;
	protected Ball mainBall = new Ball(15, width / 2, height / 2, 0, 0, Color.WHITE, this, Ball.MAIN);
	private final ArrayList<Circle> holes = new ArrayList<Circle>();
	
	private boolean foul = true;
	private boolean firstPot = false;
	private boolean point = false;
	private boolean turn = true;
	private boolean replay = false;
	
	private List<Double[]> shots = new ArrayList<Double[]>();
	private Game replayGame;
	
	/**
	 * 
	 * @param drawer En tegner som tegner spillet på en GraphicsContext. 
	 * Dersom null vil ikke spillet tegnes (vises), men kan fortsatt kjøres logisk
	 */
	public Game(Drawer drawer) {
		super(drawer);
		generateTriangleSetup(5, new Point2D(width / 4, height / 2));
		setUpHoles();
		
		setP1(new Player("Player 1", this));
		setP2(new Player("Player 2", this));
		p1.setTurn(true);
	}
	
	public Game(Drawer drawer, GameFile file) {
		super(drawer);
		
		// Setter baller fra fil
		for (Ball ball : file.getBalls()) {
			addBall(ball);
			
			if (ball.getType() == Ball.MAIN) mainBall = ball;
		}
		
		// Setter spillere fra fil
		setP1(file.getP1());
		setP2(file.getP2());
		
		// Setter spilltilstandene fra fil
		this.foul = file.isFoul();
		this.firstPot = file.isFirstPot();
		this.point = file.isPoint();
		this.turn = file.isTurn();
		
		// Setter skuddene som har blitt spilt fra fil
		this.shots = file.getShots();
		
		setUpHoles();
	}

	private void setUpHoles() {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				holes.add(new Circle(15 + j * (width / 2 - 15), 15 + i * (height - 30), 20));
			}
		}
	}
	
	public void update() {
		super.update();
		
		// Sjekker om de er i hullene
		/*balls.stream()
			.filter(b -> holes.stream().anyMatch(h -> h.contains(b.getPosX(), b.getPosY())))
			.peek(b -> System.out.println(b))
			.forEach(b -> this.removeBall(b));
		*/
		for (int i = 0; i < balls.size(); i++) {
			Ball ball = balls.get(i);
			for (Circle hole: holes) {
				if (hole.contains(ball.getPosX(), ball.getPosY())) {
					removeBall(i);
					i--;
				}
			}
		}

		// Sjekker om det er neste runde eller den neste spiller sin tur
		if (!turn && isAllIdle()) {
			if (foul || !point)
				nextTurn();
			nextRound();
		}

		// Dersom det har vært foul skal neste spiller bestemme hvor den skal sette
		// ballen
		if (foul && isAllIdle()) {
			balls.remove(mainBall);
			mainBall.setPosition(mouseX, mouseY);
		}
	}

	public void draw() {
		super.draw();

		// Tegner alle hullene
		for (int i = 0; i < holes.size(); i++) {
			drawer.fillCircle(holes.get(i));
		}

		// Dersom det er foul skal man tegne ballen på musposisjonen
		if (foul && isAllIdle()) {
			drawer.drawBall(mainBall);
		}
	}

	public void removeBall(int index) {
		Ball ball = balls.get(index);

		// Dersom det er første pot skal sidene settes (Stripes/Solids)
		if (!firstPot && (ball.getType() == Ball.SOLID || ball.getType() == Ball.STRIPE)) {
			if (p1.isTurn()) {
				p1.setType(ball.getType());
				p2.setType((ball.getType() + 1) % 2);
			}
			if (p2.isTurn()) {
				p2.setType(ball.getType());
				p1.setType((ball.getType() + 1) % 2);
			}
			firstPot = true;
			point = true;
		}

		// Hvis det var den hvite ballen så blir det foul
		if (ball == mainBall)
			foul = true;

		// Dersom spilleren tar riktig type ball blir det point, ellers foul
		if (getCurrentTurnPlayer().getType() == ball.getType()) {
			point = true;
		} else
			foul = true;

		// Dersom det er den svarte ballen blir det foul
		if (ball.getType() == Ball.BLACK)
			foul = true;

		super.removeBall(index);
	}
	
	public void removeBall(Ball ball) {
		int index = balls.indexOf(ball);
		if (index != -1)
			this.removeBall(index);
	}

	private void nextRound() {
		// Kan ikke sette neste runde dersom ikke alle ballene er i ro
		if (!isAllIdle()) return;
		
		// Setter den lille farten de kan ha til å være 0
		balls.forEach(b -> b.setVelocity(0, 0));
		
		turn = true;
		point = false;
	}

	private void nextTurn() {
		// Kan ikke sette neste tur dersom ikke alle ballene er i ro
		if (!isAllIdle()) return;
		
		if (p1.isTurn()) {
			p1.setTurn(false);
			p2.setTurn(true);
		} else if (p2.isTurn()) {
			p2.setTurn(false);
			p1.setTurn(true);
		}
	}

	public Player getCurrentTurnPlayer() {
		if (p1.isTurn())
			return new Player(p1);
		if (p2.isTurn())
			return new Player(p2);
		return new Player("", this);
	}

	public void addLostBall() {
		if (foul && isAllIdle()) {
			mainBall.setVelocity(0, 0);
			mainBall.setAcceleration(0, 0);
			addBall(mainBall);
			foul = false;
			loading = false;
		}
	}

	public void loadShot(double mouseX, double mouseY) {
		if (!mainBall.contains(mouseX, mouseY))
			return;
		if (foul || loading)
			return;
		if (!isAllIdle())
			return;
		
		super.loadShot(mouseX, mouseY);
	}
	
	@Override
	public void releaseShot(double mouseX, double mouseY) {
		if (!loading)
			return;
		super.releaseShot(mouseX, mouseY);
		turn = false;
		recordShot(mainBall.getPosX(), mainBall.getPosY(), loadX, loadY, mouseX, mouseY);

	}
	
	private void recordShot(double x, double y, double lx, double ly, double rx, double ry) {
		shots.add(new Double[] {x, y, lx, ly, rx, ry});
	}
	
	@Override
	public void stop() {
		super.stop();
		if (replayGame != null) replayGame.stop();
	}

	public void replay() {
		stop();
		replay = true;
		replayGame = new GameReplay(this.drawer, this);
		replayGame.start();
		
		this.p1 = replayGame.p1;
		this.p2 = replayGame.p2;
	}
	
	
	/**
	 * 
	 * @return antallet baller med helfarge igjen
	 */
	public int getSolidsLeft() {
		int left = 0;
		for (Ball ball : balls) {
			if (ball.getType() == Ball.SOLID)
				left++;
		}
		return left;
		
		/* Med streams
		return (int) balls.stream()
				.filter(b -> b.getType() == Ball.SOLID)
				.count();
		*/
	}


	/**
	 * 
	 * @return antallet baller med striper igjen
	 */
	public int getStripesLeft() {
		int left = 0;
		for (Ball ball : balls) {
			if (ball.getType() == Ball.STRIPE)
				left++;
		}
		return left;
		
		/* Med streams
		return (int) balls.stream()
				.filter(b -> b.getType() == Ball.STRIPE)
				.count();
		*/
	}
	
	public Ball getMainBall() {
		/*return this.getBalls().stream()
				.filter(b -> b.getType() == Ball.MAIN)
				.collect(Collectors.toList())
				.get(0);
		*/
		return new Ball(mainBall);
	}
	
	public List<Double[]> getShots() {
		List<Double[]> nShots = new ArrayList<Double[]>();
		for (Double[] shot : shots) {
			nShots.add(new Double[] {shot[0], shot[1], shot[2], shot[3], shot[4], shot[5]});
		}
		return nShots;
	}
	
	/**
	 * 
	 * @param p1 Spiller som man vil sette spiller 1 til. 
	 * Det er kun mulig å sette spiller 1 én gang.
	 */
	public void setP1(Player p1) {
		// null-sjekk
		if (p1 == null) return;
		
		// Kan kun sette spiller en gang
		if (this.p1 == null)
			this.p1 = p1;
		// ordner kobling
		if (p1.getGame() != this) 
			p1.setGame(this, 1);
	}
	
	/**
	 * 
	 * @param p2 Spiller som man vil sette spiller 2 til.
	 * Det er kun mulig å sette spiller 2 én gang.
	 */
	public void setP2(Player p2) {
		// null-sjekk
		if (p2 == null) return;
		
		// kan kun sette spiller en gang
		if (this.p2 == null)
			this.p2 = p2;
		// ordner kobling
		if (p2.getGame() != this) 
			p2.setGame(this, 2);
	}
	
	public Player getP1() {
		return new Player(p1);
	}

	public Player getP2() {
		return new Player(p2);
	}
	
	/**
	 * 
	 * @return true dersom det har vært en foul i spillet eller det er første runde.
	 * Foul er om noen har gjort en ulovelig runde, altså skutt ut den andre sin kule, den hvite eller den svarte.
	 */
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
