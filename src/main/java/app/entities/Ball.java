package app.entities;

import java.util.Random;

import app.game.BallEngine;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class Ball {
	
	public static final int SOLID = 0;
	public static final int STRIPE = 1;
	public static final int MAIN = 2;
	public static final int BLACK = 3;
	public static final int NONE = 4;
	
	public static final double VEL_LINEAR_COEFFICIENT = 0.01;
	public static final double VEL_SQUARED_COEFFICIENT = 0.001;
	
	private BallEngine engine;
	
	private double posX;
	private double posY;
	private double velX;
	private double velY;
	private double accX;
	private double accY;
	private double radius;
	
	private int r, g, b;
	private int type;
	
	/**
	 * 
	 * @param r
	 * @param posX
	 * @param posY
	 * @param velX
	 * @param velY
	 * @param color
	 * @param engine (engine vil ikke ha ballen i seg, men ballen vil refere til engine)
	 * @param type
	 */
	public Ball(double r, double posX, double posY, double velX, double velY, Color color, BallEngine engine, int type) {
		this.engine = engine;
		setRadius(r);
		setPosition(posX, posY);
		setVelocity(velX, velY);
		setAcceleration(0, 0);
		setColor(color);
		setType(type);
	}
	
	public Ball(double r, double x, double y, BallEngine engine, int type) {
		Random rand = new Random();
		this.engine = engine;
		setGame(engine);
		setRadius(r);
		setPosition(x, y);
		setVelocity(30 * (rand.nextDouble() - 1), 30 * (rand.nextDouble() - 1));
		setType(type);
		
		Color col = Color.rgb((int) (rand.nextDouble() * 255), (int) (rand.nextDouble() * 255),
				(int) (rand.nextDouble() * 255));
		if (type == BLACK) 
			setColor(Color.BLACK);
		else if (type == MAIN) 
			setColor(Color.WHITE);
		else setColor(col);
	}
	
	public Ball(Ball ball) {
		this(ball.getRadius(), ball.getPosX(), ball.getPosY(), ball.getVelX(), ball.getVelY(), ball.getColor(), ball.getGame(), ball.getType());
		this.setAcceleration(ball.getAccX(), ball.getAccY());
		this.setColor(Color.color(ball.getColor().getRed(), ball.getColor().getGreen(), ball.getColor().getBlue()));
	}

	public void update() {
		// Oppdaterer akselerasjon ut ifra fartsretning og farten
		Point2D vel = new Point2D(velX, velY).normalize();
		double value = VEL_SQUARED_COEFFICIENT * getVel() * getVel() + VEL_LINEAR_COEFFICIENT * getVel();
		setAcceleration(-vel.getX() * value, -vel.getY() * value);
		
		// Oppdaterer fart og posisjon
		velX += accX;
		velY += accY;
		posX += velX;
		posY += velY;
		
		borderCollision();
	}
	
	private void borderCollision() {
		if (posX >= engine.getWidth() - radius) {
			posX = engine.getWidth() - 1 - radius;
			velX = -velX;
		}
		if (posX < getRadius()) {
			posX = getRadius();
			velX = -velX;
		}
		if (posY >= engine.getHeight() - radius) {
			posY = engine.getHeight() - radius - 1;
			velY = -velY;
		}
		if (posY < getRadius()) {
			posY = getRadius();
			velY = -velY;
		}
	}
	
	public static void ballCollision(Ball ball1, Ball ball2) {
		Point2D pos1 = new Point2D(ball1.getPosX(), ball1.getPosY());
		Point2D pos2 = new Point2D(ball2.getPosX(), ball2.getPosY());
		
		double dist = pos1.distance(pos2);
		if (dist <= (ball1.getRadius() + ball2.getRadius())) {
			removeOverlap(ball1, ball2);
			calculateCollisionSpeeds(ball1, ball2);
			
		}
	}
	
	private static void removeOverlap(Ball ball1, Ball ball2) {
		Point2D pos1 = new Point2D(ball1.getPosX(), ball1.getPosY());
		Point2D pos2 = new Point2D(ball2.getPosX(), ball2.getPosY());
		
		double angle1 = Ball.getAngle(pos1, pos2);
		Point2D angleVec = new Point2D(Math.cos(angle1), Math.sin(angle1));

		double dist = pos1.distance(pos2);
		double overlap = (ball1.getRadius() + ball2.getRadius() - dist);
		
		// Den med mest fart skal gå mest vekk
		double velSum = ball1.getVel() + ball2.getVel();
		if (velSum != 0) {				
			pos1 = pos1.add(angleVec.multiply(-overlap * ball1.getVel() / velSum));
			pos2 = pos2.add(angleVec.multiply(overlap  * ball2.getVel() / velSum));
		}else {
			pos1 = pos1.add(angleVec.multiply(-overlap/2));
			pos2 = pos2.add(angleVec.multiply(overlap/2));
		}
		ball1.setPosition(pos1.getX(), pos1.getY());
		ball2.setPosition(pos2.getX(), pos2.getY());
	}
	
	private static void calculateCollisionSpeeds(Ball ball1, Ball ball2) {
		Point2D pos1 = new Point2D(ball1.getPosX(), ball1.getPosY());
		Point2D pos2 = new Point2D(ball2.getPosX(), ball2.getPosY());
		
		double angle1 = Ball.getAngle(pos1, pos2);
		double normal = angle1 + Math.PI / 2;
		Point2D angleVec = new Point2D(Math.cos(angle1), Math.sin(angle1));
		Point2D normalVec = new Point2D(Math.cos(normal), Math.sin(normal));
		
		// Regner bevaring av fartsvektoren
		Point2D totVel = new Point2D(ball1.getVelX(), ball1.getVelY()).add(ball2.getVelX(), ball2.getVelY());
		// koffesientene ved å løse vektorlikningen: an + bv = t (n: normalvektoren, v: vektorene mellom sentrene, t: total fartsvektor) Løsning: https://www.emathhelp.net/calculators/linear-algebra/reduced-row-echelon-form-rref-caclulator/?i=%5B%5Ba%2Cc%2Ce%5D%2C%5Bb%2Cd%2Cf%5D%5D&reduced=on&steps=on
		double a = (-angleVec.getX() * totVel.getY() + totVel.getX() * angleVec.getY())  / (normalVec.getX() * angleVec.getY() - normalVec.getY() * angleVec.getX());
		double b = (normalVec.getX() * totVel.getY() - totVel.getX() * normalVec.getY()) / (normalVec.getX() * angleVec.getY() - normalVec.getY() * angleVec.getX());
		
		// Den med mest fart skal gå langs normalen etter støtet
		if (ball1.getVel() > ball2.getVel()) {
			ball1.setVelocity(normalVec.multiply(a).getX(), normalVec.multiply(a).getY());
			ball2.setVelocity(angleVec.multiply(b).getX(), angleVec.multiply(b).getY());				
		} else {
			ball1.setVelocity(angleVec.multiply(b).getX(), angleVec.multiply(b).getY());
			ball2.setVelocity(normalVec.multiply(a).getX(), normalVec.multiply(a).getY());
		}
	}
	
	public static double getAngle(Point2D pos1, Point2D pos2) {
		Point2D line = pos2.subtract(pos1);
		double angle1 = line.angle(1, 0);

		if (pos1.getY() > pos2.getY() && pos1.getX() <= pos2.getX()) {
			angle1 = -angle1;
		}
		if (pos1.getY() < pos2.getY() && pos1.getX() < pos2.getX()) {
			angle1 = angle1;
		}
		if (pos1.getY() > pos2.getY() && pos1.getX() > pos2.getX()) {
			angle1 = angle1 + 2*(180 - angle1);
		}
		if (pos1.getY() < pos2.getY() && pos1.getX() > pos2.getX()) {
			angle1 = angle1;
		}
		
		return angle1 * Math.PI / 180;

	}
	
	/*
	public static void main(String[] args) {
		Point2D pos1 = new Point2D(1.6, 1.5);
		Point2D pos2 = new Point2D(1.6, 3);
		
		System.out.println(pos2);
		Point2D line = pos2.subtract(pos1);
		System.out.println(pos2);
		double angle = line.angle(1, 0);
		System.out.println(angle);
		System.out.println(getAngle(pos1, pos2));
		System.out.println(Math.cos(getAngle(pos1, pos2) * Math.PI / 180));
		System.out.println(Math.sin(getAngle(pos1, pos2) * Math.PI / 180));
	}
	*/
	
	public boolean contains(double x, double y) {
		return (new Point2D(posX, posY).distance(new Point2D(x, y)) <= radius);
	}
	
	/**
	 * * Regler for kobling:
	 * 		Game kan ha ball i seg dersom ball referer til game
	 * 		Ball kan refere til en game, men må ikke nødvendigvis være i game
	 * @return game som den referer til
	 */
	public BallEngine getGame() {
		return this.engine;
	}
	
	/**
	 * * Regler for kobling:
	 * 		Game kan ha ball i seg dersom ball referer til game
	 * 		Ball kan refere til en game, men må ikke nødvendigvis være i game
	 * @param engine game som ball skal ha referanse til til
	 */
	public void setGame(BallEngine engine) {
		if (this.engine != engine && this.engine != null) this.engine.removeBall(this);
		this.engine = engine;
	}
	
	public double getPosX() {
		return posX;
	}


	public double getPosY() {
		return posY;
	}


	public double getVelX() {
		return velX;
	}


	public double getVelY() {
		return velY;
	}

	public double getVel() {
		return new Point2D(velX, velY).magnitude();
	}
	
	public double getAccX() {
		return accX;
	}

	public double getAccY() {
		return accY;
	}
	
	public double getAcc() {
		return new Point2D(accX, accY).magnitude();
	}

	public double getRadius() {
		return radius;
	}
	
	public Color getColor() {
		return Color.rgb(r, g, b);
	}

	public void setPosition(double x, double y) {
		this.posX = x;
		this.posY = y;
	}
	
	public void setVelocity(double x, double y) {
		this.velX = x;
		this.velY = y;
	}
	
	public void setAcceleration(double x, double y) {
		this.accX = x;
		this.accY = y;
	}
	
	public void setRadius(double r) {
		this.radius = r;
	}

	public void setColor(Color color) {
		r = (int) (color.getRed() * 255);
		g = (int) (color.getGreen() * 255);
		b = (int) (color.getBlue() * 255);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
