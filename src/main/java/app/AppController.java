package app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;

import app.fileio.GameFileIO;
import app.game.Drawer;
import app.game.Game;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class AppController {	
	
	Game game;
	Drawer drawer;
	@FXML Canvas canvas;
	
	@FXML Label p1Text;
	@FXML Label p2Text;
	@FXML Label message;
	@FXML Button restartButton;
	@FXML Button replay;
	@FXML Button save;
	@FXML Button load;
	
	@FXML
	public void initialize() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		drawer = new Drawer(gc);
		game = new Game(drawer);
		game.start();
		canvas.setWidth(game.getWidth());
		canvas.setHeight(game.getHeight());
		
	}
	
	public void handleClick(MouseEvent event) {
		game.addLostBall();
		//game.addBall(new Ball(10, game.getMouseX(), game.getMouseY(), game, Ball.NONE));
	}
	
	public void handlePressed(MouseEvent event) {
		game.loadShot(event.getX(), event.getY());
	}
	
	public void handlePressRelease(MouseEvent event) {
		game.releaseShot(event.getX(), event.getY());
	}
	
	public void handleMoved(MouseEvent event) {
		game.setMouseX(event.getX());
		game.setMouseY(event.getY());
		
		updateInfo();
	}
	
	public void updateInfo() {
		p1Text.setText(game.getP1().toString());
		p2Text.setText(game.getP2().toString());
	}
	
	public void restart() {
		game.stop();
		initialize();
		message.setText("Succsessfully restarted the game");
	}
	
	public void replay() {
		game.replay();
	}
	
	public void saveGame() {
		if (!game.isTurn()) {
			message.setText("You cannot save game when the round is not over.");
			return;
		}
		
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File(new File("").getAbsolutePath() + "/src/main/resources/app/savefiles"));
		chooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));
		File file = chooser.showSaveDialog(new Stage());
		GameFileIO fileIO = new GameFileIO();
		try {
			if (file == null) return;
			fileIO.save(file.toString(), game);
			message.setText("Has succsesfully saved the game to a gamefile");
		} catch (IOException e) {
			e.printStackTrace();
			message.setText(e.getMessage());
		}
	}
	
	public void loadGame() {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File(new File("").getAbsolutePath() + "/src/main/resources/app/savefiles"));
		chooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));
		File file = chooser.showOpenDialog(new Stage());
		GameFileIO fileIO = new GameFileIO();
		try {
			if (file == null) return;
			game.stop();
			game = new Game(drawer, fileIO.load(file.toString()));
			game.start();
			message.setText("Has succsesfully loaded a gamefile");
		}
		catch (IOException e) {
			message.setText("Kan ikke laste inn filen");
			game.start();
		}
		catch (Exception e) {
			message.setText("Kan ikke laste inn filen");
			game.start();
		}
	}
	
}
