package app.fileio;

import java.io.FileNotFoundException;
import java.io.IOException;

import app.game.Game;

public interface GameFileIOInterface {
	
	void save(String filename, Game game) throws IOException;
	GameFile load(String filename) throws IOException;
	
}
