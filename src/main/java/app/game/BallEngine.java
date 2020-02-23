package app.game;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

import app.entities.Ball;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BallEngine extends AnimationTimer {
	
	protected final int width = 750;
	protected final int height = 500;

	protected Drawer drawer;
	protected List<Ball> balls = new ArrayList<Ball>();
	protected Ball loadedBall;

	protected double mouseX, mouseY;
	protected double loadX, loadY;
	protected boolean loading = false;
	
	public BallEngine(Drawer drawer) {
		this.drawer = drawer;
		
	}
	
	@Override
	public void handle(long arg0) {
		if (drawer != null) draw();
		update();
	}

	public void update() {
		// Oppdaterer alle ballene
		for (int i = 0; i < balls.size(); i++) {
			Ball ball = balls.get(i);
			ball.update();
		}

		// Sjekker kollisjon mellom alle ballene
		for (int i = 0; i < balls.size(); i++) {
			for (int j = i + 1; j < balls.size(); j++) {
				Ball.ballCollision(balls.get(i), balls.get(j));
			}
		}
	}
	
	public void draw() {
		// Tegner bakgrunnen og så alle ballene
		drawer.drawBackground();
		for (int i = 0; i < balls.size(); i++) {
			drawer.drawBall(balls.get(i));
		}
		
		// Dersom man lader et skudd skal det tegnes en linje mellom musen og punktet
		if (loading) {
			drawer.drawLine(loadX, loadY, mouseX, mouseY, Color.LIMEGREEN);
			double angle = Ball.getAngle(new Point2D(loadX, loadY), new Point2D(mouseX, mouseY));
			drawer.drawLine(loadedBall.getPosX(), loadedBall.getPosY(), loadedBall.getPosX() - Math.cos(angle) * 100,
					loadedBall.getPosY() - Math.sin(angle) * 100, Color.WHITE);
		}
	}
	/**
	 * 
	 * Regler for kobling:
	 * 		Game kan ha ball i seg dersom ball referer til game
	 * 		Ball kan refere til en game, men må ikke nødvendigvis være i game
	 *
	 * @param ball som skal legges til game
	 */
	public void addBall(Ball ball) {
		if (ball == null) return;
		ball.setGame(this);
		if (!balls.contains(ball))
			balls.add(ball);
	}
	
	/**
	 * * Regler for kobling:
	 * 		Game kan ha ball i seg dersom ball referer til game
	 * 		Ball kan refere til en game, men må ikke nødvendigvis være i game
	 * @param index den indexen ballen har i listen
	 */
	public void removeBall(int index) {
		balls.remove(balls.get(index));
	}
	
	/**
	 * * Regler for kobling:
	 * 		Game kan ha ball i seg dersom ball referer til game
	 * 		Ball kan refere til en game, men må ikke nødvendigvis være i game
	 * @param ball som skal fjernes
	 */
	public void removeBall(Ball ball) {
		if (ball == null) return;
		
		balls.remove(ball);
		
		ball.setGame(null);
	}
	
	/**
	 * 
	 * @return en listen med kopier av ballene
	 */
	public List<Ball> getBalls() {
		List<Ball> nBalls = new ArrayList<Ball>();
		for (Ball ball : balls) {
			nBalls.add(new Ball(ball));
		}
		return nBalls;
		
		/* Med streams
		return balls.stream()
				.map(b -> new Ball(b))
				.collect(Collectors.toList());
		*/
	}
	
	public boolean containsBall(Ball ball) {
		return this.balls.contains(ball);
	}
	
	public boolean isAllIdle() {
		for (int i = 0; i < balls.size(); i++) {
			Ball ball = balls.get(i);
			if (!(ball.getVel() < 0.01)) {
				return false;
			}
		}
		return true;

		// Med streams
		// return balls.stream().allMatch(b -> b.getVel() < 0.001);
	}
	
	public List<Integer[]> getBallsInfo() {
		// 0 er r
		// 1 er x
		// 2 er y
		// 3 er type
		// farger:
		// 4 er r
		// 5 er g
		// 6 er b
		List<Integer[]> infoList = new ArrayList<Integer[]>(balls.size());
		for (Ball ball : balls) {
			infoList.add(new Integer[] { (int) ball.getRadius(), (int) ball.getPosX(), (int) ball.getPosY(),
					ball.getType() });
		}
		return infoList;
	}
	
	public void loadShot(double mouseX, double mouseY) {
		try {			
			loadedBall = balls.stream().filter(b -> b.contains(mouseX, mouseY)).findFirst().get();
		} catch (NoSuchElementException e){
			return;
		}
		this.loadX = mouseX;
		this.loadY = mouseY;

		loading = true;
	}

	public void releaseShot(double mouseX, double mouseY) {
		if (!loading)
			return;
		Point2D load = new Point2D((int) loadX, (int) loadY);
		Point2D release = new Point2D((int) mouseX, (int) mouseY);

		double angle = Ball.getAngle(release, load);
		if (Double.isNaN(angle))
			return;
		double power = load.distance(release) * 0.15;
		if (power >= 30)
			power = 30;
		loadedBall.setVelocity(Math.cos(angle) * power, Math.sin(angle) * power);
		
		loading = false;
	}
	
	protected void generateTriangleSetup(int len, Point2D pos) {
		Random rand = new Random();
		double radius = 15;
		pos = pos.subtract(radius * len, radius * len);
		int count = 0;
		for (int y = 0; y < len; y++) {
			for (int x = y; x < len; x++) {
				int type = Ball.SOLID;
				Color col = Color.rgb((int) (rand.nextDouble() * 255), (int) (rand.nextDouble() * 255),
						(int) (rand.nextDouble() * 255));
				if (count % 2 == 0)
					type = Ball.STRIPE;
				if (y == (int) (len / 2) && (x - y) == (len - 1 - x)) {
					type = Ball.BLACK;
					col = Color.BLACK;
				}
				addBall(new Ball(radius, pos.getX() + (radius * y) + (x - y) * 2 * radius, y * radius * 2 + pos.getY(),
						0, 0, col, this, type));
				count++;
			}
		}
	}
	
	public double getWidth() {
		return this.width;
	}

	public double getHeight() {
		return this.height;
	}

	public double getMouseX() {
		return mouseX;
	}

	public void setMouseX(double mouseX) {
		this.mouseX = mouseX;
	}

	public double getMouseY() {
		return mouseY;
	}
	
	public void setMouseY(double mouseY) {
		this.mouseY = mouseY;
	}
}
