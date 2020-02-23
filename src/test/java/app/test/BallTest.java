package app.test;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import app.entities.Ball;
import app.fileio.GameFileIO;
import app.game.Game;
import javafx.scene.paint.Color;
import junit.framework.TestCase;

public class BallTest extends TestCase {
	
	Game game;
	Ball ball;
	double centerX;
	double centerY;
	
	@Before
	public void setUp() {
		game = new Game(null);
		centerX = game.getWidth() / 2;
		centerY = game.getHeight() / 2;
		ball = new Ball(20, centerX, centerY, 0, 0, Color.rgb(255, 0, 0), game, Ball.NONE);
	}
	
	
	@Test
	public void testConstructor() {
		// Forventer ikke at spillet har ballen i listen uten å kalle addBall() fra game
		// men ballen skal ha referanse til spillet
		assertEquals(false, game.containsBall(ball));
		assertEquals(true, ball.getGame() == game);
		
		// Når game kaller addBall forventes det at ballen er i spillets liste
		game.addBall(ball);
		assertEquals(true, game.containsBall(ball));
		assertEquals(true, ball.getGame() == game);
		
		System.out.println(ball.getPosX());
		
		// Sjekker at dataene er det man forventer fra konstruktøren
		assertEquals(20, ball.getRadius(), 0);
		assertEquals(centerX, ball.getPosX(), 0);
		assertEquals(centerY, ball.getPosY(), 0);
		assertEquals(0, ball.getVelX(), 0);
		assertEquals(0, ball.getVelY(), 0);
		assertEquals(0, ball.getAccX(), 0);
		assertEquals(0, ball.getAccX(), 0);
		assertEquals(1, ball.getColor().getRed(), 0);
		assertEquals(0, ball.getColor().getGreen(), 0);
		assertEquals(0, ball.getColor().getBlue(), 0);
		assertEquals(Ball.NONE, ball.getType());
	}
	
	@Test
	public void testSimpleGettersAndSetters() {
		ball.setRadius(5);
		ball.setPosition(100, 300);
		ball.setVelocity(20, -10);
		ball.setAcceleration(0.01, -0.01);
		ball.setColor(Color.color(0.3, 0.2, 0.8));
		ball.setType(Ball.BLACK);
		
		// Tester at det man forventer
		assertEquals(5, ball.getRadius(), 0);
		assertEquals(100, ball.getPosX(), 0);
		assertEquals(300, ball.getPosY(), 0);
		assertEquals(20, ball.getVelX(), 0);
		assertEquals(-10, ball.getVelY(), 0);
		assertEquals(0.01, ball.getAccX(), 0);
		assertEquals(-0.01, ball.getAccY(), 0);
		assertEquals(0.3, ball.getColor().getRed(), 0.01);
		assertEquals(0.2, ball.getColor().getGreen(), 0.01);
		assertEquals(0.8, ball.getColor().getBlue(), 0.01);
		assertEquals(Ball.BLACK, ball.getType());
	}
	
	/**
	 * Regler for kobling:
	 * 		Game kan ha ball i seg dersom ball referer til game
	 * 		Ball kan refere til en game, men må ikke nødvendigvis være i game
	 */
	@Test
	public void testBallGameRelation() {
		// Setter null: forventer null
		ball.setGame(null);
		assertEquals(true, ball.getGame() == null);
		assertEquals(false, game.containsBall(ball));
		
		// addGame: forventer at begge har ball
		game.addBall(ball);
		assertEquals(true, ball.getGame() == game);
		assertEquals(true, game.containsBall(ball));
		
		// setter null igjen: forventer game ikke har ball og ball ikke har game
		ball.setGame(null);
		assertEquals(false, ball.getGame() == game);
	}
	
	@Test
	public void testPositionOutsideBorder() {
		// Forventer å komme til venstre top hjørne
		ball.setPosition(-100, -300);
		ball.update();
		assertEquals(0 + ball.getRadius(), ball.getPosX(), 0.01);
		assertEquals(0 + ball.getRadius(), ball.getPosY(), 0.01);
		
		// høyre top hjørne
		ball.setPosition(game.getWidth() + 100, -300);
		ball.update();
		assertEquals(game.getWidth() - ball.getRadius() - 1, ball.getPosX(), 0.01);
		assertEquals(0 + ball.getRadius(), ball.getPosY(), 0.01);
		
		// venstre nedre hjørne
		ball.setPosition(-100, game.getHeight() + 300);
		ball.update();
		assertEquals(0 + ball.getRadius(), ball.getPosX(), 0.01);
		assertEquals(game.getHeight() - ball.getRadius() - 1, ball.getPosY(), 0.01);
		
		// høyre nedre hjørne
		ball.setPosition(game.getWidth() + 100, game.getHeight() + 300);
		ball.update();
		assertEquals(game.getWidth() - ball.getRadius() - 1, ball.getPosX(), 0.01);
		assertEquals(game.getHeight() - ball.getRadius() - 1, ball.getPosY(), 0.01);
	}

	@Test
	public void testBorderCollision() {
		ball.setVelocity(10, 0);
		assertEquals(true, ball.getVelX() > 0);
		// Forventer at den har truffet veggen etter 4 sekunder
		for (int i = 0; i < 60 * 4; i++) {
			ball.update();
		}
		
		System.out.println(ball.getVelX());
		
		assertEquals(true, ball.getVelX() <= 0);
		assertEquals(true, ball.getPosX() < game.getWidth());
		
	}
	
	@Test
	public void testSimpleBallMovement() {
		ball.setVelocity(10, 10);
		
		double xVel = ball.getVelX();
		double yVel = ball.getVelY();
		double yPos = ball.getPosY();
		double xPos = ball.getPosX();
		
		for(int i = 0; i < 30; i++) {
			ball.update();
			xVel -= ball.getVelX() * ball.getVelX() * Ball.VEL_SQUARED_COEFFICIENT + ball.getVelX() * Ball.VEL_LINEAR_COEFFICIENT;
			yVel -= ball.getVelY() * ball.getVelY() * Ball.VEL_SQUARED_COEFFICIENT + ball.getVelY() * Ball.VEL_LINEAR_COEFFICIENT;
			xPos += ball.getVelX();
			yPos += ball.getVelY();
			
			assertEquals(xVel, ball.getVelX(), 1);
			assertEquals(yVel, ball.getVelY(), 1);
			assertEquals(xPos, ball.getPosX(), 1);
			assertEquals(yPos, ball.getPosY(), 1);
		}
	}
}
