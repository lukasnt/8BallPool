package app.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import app.entities.Ball;
import app.fileio.GameFile;
import app.fileio.GameFileIO;
import app.game.Game;
import junit.framework.TestCase;

public class GameTest extends TestCase {
	
	Map<String, Boolean> expectedStates;
	Game test;
	double centerX;
	double centerY;
	final String testFilesDir = new File("").getAbsolutePath() + "/src/test/resources/app/test/";
	GameFileIO io;
	
	@Before
	public void setUp() {
		test = new Game(null);
		
		expectedStates = new HashMap<>();
		expectedStates.put("isAllIdle", true);
		expectedStates.put("isTurn", true);
		expectedStates.put("isFirstPot", false);
		expectedStates.put("isFoul", true);
		expectedStates.put("isPoint", false);
		expectedStates.put("P1.isTurn", true);
		expectedStates.put("P2.isTurn", false);
		
		centerX = test.getWidth() / 2;
		centerY = test.getHeight() / 2;
		
		io = new GameFileIO();
	}
	
	@Test
	public void testStates() {
		assertEquals(expectedStates.get("isAllIdle").booleanValue(),	test.isAllIdle());
		assertEquals(expectedStates.get("isTurn").booleanValue(), 		test.isTurn());
		assertEquals(expectedStates.get("isFirstPot").booleanValue(), 	test.isFirstPot());
		assertEquals(expectedStates.get("isFoul").booleanValue(), 		test.isFoul());
		assertEquals(expectedStates.get("isPoint").booleanValue(), 		test.isPoint());
		assertEquals(expectedStates.get("P1.isTurn").booleanValue(), 	test.getP1().isTurn());
		assertEquals(expectedStates.get("P2.isTurn").booleanValue(), 	test.getP2().isTurn());
	}
	
	@Test
	public void testConstructor() {
		Game test = new Game(null);
		testStates();
		assertEquals(test.getSolidsLeft(), 7);
		assertEquals(test.getStripesLeft(), 7);
		
	}
	
	
	@Test
	public void testLoadFile() {
		GameFileIO io = new GameFileIO();
		try {
			GameFile file = io.load(testFilesDir.toString() + "startHorizontalCenter.txt");
			test = new Game(null, file);
			
			// Tester like antall baller
			List<Ball> fileBalls = file.getBalls();
			List<Ball> gameBalls = test.getBalls();
			assertEquals(fileBalls.size(), gameBalls.size());
			
			// Tester at alle ballene er på samme posisjon i fil og spillet
			assertEquals(true, gameBalls.stream()
				.allMatch(gb -> fileBalls.stream()
							.anyMatch(fb -> gb.getPosX() == fb.getPosX() && gb.getPosY() == fb.getPosY())
				)
			);
			
			// tester samme tilstander
			expectedStates.put("isFoul", false);
			testStates();
		} catch (IOException e) {
			fail("Got IOException");
		}
	}
	
	@Test
	public void testPlaceBallAtStart() {
		test.setMouseX(centerX);
		test.setMouseY(centerY);
		test.update();
		test.addLostBall();
		test.update();
		
		// Tester om ballen er i midten
		Ball mainBall = test.getMainBall();
		assertEquals(mainBall.getPosX(), centerX, 0.001);
		assertEquals(mainBall.getPosY(), centerY, 0.001);
		
		expectedStates.put("isFoul", false);
		testStates();
	}
	
	@Test
	public void testSimpleShot() {
		try {
			GameFile file = io.load(testFilesDir.toString() + "startHorizontalCenter.txt");
			test = new Game(null, file);
		} catch (IOException e) {
			fail("Couldn't load testfile: startHorizontalCenter.txt");
		}
		
		Ball mainBall = test.getMainBall();
		test.loadShot(mainBall.getPosX(), mainBall.getPosY());
		test.releaseShot(mainBall.getPosX(), mainBall.getPosY() + 30);
		test.update();
		
		// Tester at den er på vei i y-retning
		assertEquals(true, test.getMainBall().getPosY() < mainBall.getPosY() - 1);
		assertEquals(true, test.getMainBall().getVelY() < 0);
		
		// Tester alle tilstandene
		expectedStates.put("isAllIdle", false);
		expectedStates.put("isTurn", false);
		expectedStates.put("isFoul", false);
		testStates();
	}

	@Test
	public void testSaveFile() {
		test.setMouseX(centerX);
		test.setMouseY(centerY);
		test.update();
		test.addLostBall();
		test.update();
		
		double oldX = test.getMouseX();
		double oldY = test.getMouseY();
		
		try {
			io.save(testFilesDir.toString() + "testSaveFile.txt", test);
		} catch (IOException e) {
			fail("IOException in saving to: testSaveFile.txt");
		}
		
		try {
			GameFile file = io.load(testFilesDir.toString() + "testSaveFile.txt");
			test = new Game(null, file);
		} catch (IOException e) {
			fail("Couldn't load the saved file: testSaveFile.txt");
		}
		
		assertEquals(oldX, test.getMainBall().getPosX(), 0);
		assertEquals(oldY, test.getMainBall().getPosY(), 0);
		
		expectedStates.put("isFoul", false);
		testStates();
	}
	
	@Test
	public void testIsAllIdle() {
		assertEquals(true, test.isAllIdle());
		
		try {
			GameFile file = io.load(testFilesDir.toString() + "startHorizontalCenter.txt");
			test = new Game(null, file);
		} catch (IOException e) {
			fail("Couldn't load testfile: startHorizontalCenter.txt");
		}
		
		// Alle ballene skal ikke være i ro etter å ha skutt den til venstre i ett sekund
		Ball mainBall = test.getMainBall();
		test.loadShot(mainBall.getPosX(), mainBall.getPosY());
		test.releaseShot(mainBall.getPosX() + 30, mainBall.getPosY());
		for (int i = 0; i < 60; i++) {
			test.update();
			assertEquals(false, test.isAllIdle());
		}
		// Etter 15 sekunder forventer jeg at alle ballene er i ro
		for (int i = 0; i < 15 * 60; i++) {
			test.update();
		}
		assertEquals(true, test.isAllIdle());
	}

	@Test
	public void testIsTurn() {
		try {
			GameFile file = io.load(testFilesDir.toString() + "startHorizontalCenter.txt");
			test = new Game(null, file);
		} catch (IOException e) {
			fail("Couldn't load testfile: startHorizontalCenter.txt");
		}
		
		// Skal være tur før man har skutt
		assertEquals(true, test.isTurn());
		assertEquals(true, test.getP1().isTurn());
		assertEquals(false, test.getP2().isTurn());
		
		Ball mainBall = test.getMainBall();
		test.loadShot(mainBall.getPosX(), mainBall.getPosY());
		test.releaseShot(mainBall.getPosX() + 30, mainBall.getPosY());
		test.update();
		assertEquals(false, test.isTurn());
		
		// Forventer at det skal være ny tur etter 15 sekunder
		for (int i = 0; i < 15 * 60; i++) {
			test.update();
		}
		assertEquals(true, test.isTurn());
		assertEquals(false, test.getP1().isTurn());
		assertEquals(true, test.getP2().isTurn());
	}

	@Test
	public void testIsFoul() {
		try {
			GameFile file = io.load(testFilesDir.toString() + "startHorizontalCenter.txt");
			test = new Game(null, file);
		} catch (IOException e) {
			fail("Couldn't load testfile: startHorizontalCenter.txt");
		}
		
		//Tester at det ikke er foul i starten
		assertEquals(false, test.isFoul());
		
		// Skyter den i y-retning slik at den går i hullet
		Ball mainBall = test.getMainBall();
		test.loadShot(mainBall.getPosX(), mainBall.getPosY());
		test.releaseShot(mainBall.getPosX(), mainBall.getPosY() + 30);
		// Etter to sekunder forventer jeg at det er foul
		for(int i = 0; i < 2 * 60; i++) {
			test.update();			
		}
		assertEquals(true, test.isFoul());
	}

	@Test
	public void testIsPoint() {
		try {
			GameFile file = io.load(testFilesDir.toString() + "ballshotPointVertical.txt");
			test = new Game(null, file);
		} catch (IOException e) {
			fail("Couldn't load testfile: ballshotPointVertical.txt");
		}
		
		assertEquals(false, test.isPoint());
		assertEquals(false, test.isFirstPot());
		
		// Skyter den i y-retning slik at den treffer en kule som går i hullet
		Ball mainBall = test.getMainBall();
		test.loadShot(mainBall.getPosX(), mainBall.getPosY());
		test.releaseShot(mainBall.getPosX(), mainBall.getPosY() + 50);
		// Etter 3 sekunder forventer jeg at det er point
		for(int i = 0; i < 3 * 60; i++) {
			test.update();
		}
		assertEquals(false, test.isPoint());
		assertEquals(true, test.isFirstPot());
		assertEquals(true, test.getP1().isTurn());
		assertEquals(false, test.getP2().isTurn());
	}
}
