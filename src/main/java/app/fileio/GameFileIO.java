package app.fileio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import app.entities.Ball;
import app.entities.Player;
import app.game.Game;
import javafx.scene.paint.Color;

public class GameFileIO implements GameFileIOInterface {

	@Override
	public void save(String filename, Game game) throws IOException {
		if (filename.endsWith(".txt")) {
			saveAsTxt(filename, game);
		} else if (filename.endsWith(".dat")) {
			saveAsObject(filename, game);
		}
	}
	
	private void saveAsObject(String filename, Game game) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
		out.writeObject(new GameFile(game));
	}

	private void saveAsTxt(String filename, Game game) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(filename);

		writer.println("Balls:");
		//List<Integer[]> infoList = game.getBallsInfo();
		for (Ball ball: game.getBalls()) {
			writer.println(String.format("%d,%d,%d,%d,%d,%d,%d", 
							(int) ball.getRadius(),
							(int) ball.getPosX(),
							(int) ball.getPosY(),
							(int) ball.getType(),
							(int) (ball.getColor().getRed() * 255),
							(int) (ball.getColor().getGreen() * 255),
							(int) (ball.getColor().getBlue() * 255))
					);
		}
		writer.println();

		writer.println("Players:");
		Player p1 = game.getP1();
		Player p2 = game.getP2();
		writer.println(String.format("%s,%d,%b", p1.getName(), p1.getType(), p1.isTurn()));
		writer.println(String.format("%s,%d,%b", p2.getName(), p2.getType(), p2.isTurn()));
		writer.println();
		
		writer.println("State:");
		writer.println(game.isFoul());
		writer.println(game.isFirstPot());
		writer.println(game.isPoint());
		writer.println(game.isTurn());
		writer.println();
		
		writer.println("Shots:");
		for (Double[] shot : game.getShots()) {
			writer.println(shot[0] + "," + shot[1] + "," + shot[2] + "," + shot[3] + "," + shot[4] + "," + shot[5]);
		}
		writer.println();
		
		writer.flush();
		writer.close();
	}
	
	/*@Override
	public GameFile load(String filename) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(filename));
		
		boolean isBallsData = false;
		boolean isPlayersData = false;
		boolean isStateData = false;
		boolean isShotsData = false;
		
		List<Ball> balls = new ArrayList<Ball>();
		Player p1 = new Player(null,  null), p2 = new Player(null, null);
		boolean[] stateData = new boolean[4];
		List<Double[]> shots = new ArrayList<Double[]>();
		
		
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			
			// Sjekker om det er blank linje
			if (line.isBlank()) 
			{
				isBallsData = false;
				isPlayersData = false;
				isStateData = false;
				isShotsData = false;
				continue;
			}
			
			if (isBallsData) 
			{
				String[] split = line.split(",");
				
				double r = Double.parseDouble(split[0]);
				double x = Double.parseDouble(split[1]);
				double y = Double.parseDouble(split[2]);
				int type = Integer.parseInt(split[3]);
				
				// Generer en tilfeldig farge for nå (gidder ikke lagre fargeverdiene)
				Random rand = new Random();
				Color col = Color.rgb((int) (rand.nextDouble() * 255), (int) (rand.nextDouble() * 255), (int) (rand.nextDouble() * 255));
				if (type == Ball.BLACK) 
					col = Color.BLACK;
				else if (type == Ball.MAIN) 
					col = Color.WHITE;
				
				balls.add(new Ball(r, x, y, 0, 0, col, null, type));
			
			} 
			else if (isPlayersData) 
			{
				// Spiller 1
				String[] split = line.split(",");
				String name = split[0];
				int type = Integer.parseInt(split[1]);
				boolean turn = Boolean.parseBoolean(split[2]);
				p1 = new Player(name, null);
				p1.setTurn(turn);
				p1.setType(type);
				
				// Spiller 2
				split = scanner.nextLine().split(",");
				name= split[0];
				type = Integer.parseInt(split[1]);
				turn = Boolean.parseBoolean(split[2]);
				p2 = new Player(name, null);
				p2.setTurn(turn);
				p2.setType(type);
			} 
			else if (isStateData) 
			{
				// Rekkefølge på tilstandsdata
				// 0: foul
				// 1: firstPot
				// 2: point
				// 3: turn
				stateData[0] = Boolean.parseBoolean(line.trim());
				stateData[1] = Boolean.parseBoolean(scanner.nextLine().trim());
				stateData[2] = Boolean.parseBoolean(scanner.nextLine().trim());
				stateData[3] = Boolean.parseBoolean(scanner.nextLine().trim());
			}
			else if (isShotsData)
			{
				String[] split = line.split(",");
				shots.add(new Double[] {Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3])});
			}
			else 
			{
				if (line.contains("Balls")) isBallsData = true;
				if (line.contains("Players")) isPlayersData = true;
				if (line.contains("State")) isStateData = true;
				if (line.contains("Shots")) isShotsData = true;
			}
		}
		
		scanner.close();
		return new GameFile(balls, p1, p2, stateData, shots);
	}*/
	
	@Override
	public GameFile load(String filename) throws IOException {
		if (filename.endsWith(".txt")) {
			return loadAsTxt(filename);
		}
		else if (filename.endsWith(".dat")) {
			return loadAsObject(filename);
		} else {
			throw new IOException("need to be correct fileformat (.txt or .dat)");
		}
	}
	
	private GameFile loadAsObject(String filename) throws IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
		try {
			return (GameFile) in.readObject();
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public GameFile loadAsTxt(String filename) throws IOException {
		List<Ball> balls = (List<Ball>) getCategoryList(filename, "Balls", s -> toBall(s));
		List<Player> players = (List<Player>) getCategoryList(filename, "Players", s -> toPlayer(s));
		List<Boolean> states = (List<Boolean>) getCategoryList(filename, "State", s -> toState(s));
		List<Double[]> shots = (List<Double[]>) getCategoryList(filename, "Shots", s -> toShotData(s));
		
		return new GameFile(balls, players.get(0), players.get(1), states, shots);
	}
	
	private List<?> getCategoryList(String filename, String categoryName, Function<String, ?> map) throws IOException{
		return Files.lines(Paths.get(filename))
		.dropWhile(s -> !s.contains(categoryName))
		.skip(1)
		.takeWhile(s -> !s.isBlank())
		.map(map)
		.collect(Collectors.toList());	
	}
	
	private Ball toBall(String ballLine) {
		String[] split = ballLine.split(",");
		
		double r = Double.parseDouble(split[0]);
		double x = Double.parseDouble(split[1]);
		double y = Double.parseDouble(split[2]);
		int type = Integer.parseInt(split[3]);
		Color col = Color.rgb(Integer.parseInt(split[4]), Integer.parseInt(split[5]), Integer.parseInt(split[6]));
		return new Ball(r, x, y, 0, 0, col, null, type);
	}
	
	private Player toPlayer(String playerLine) {
		String[] split = playerLine.split(",");
		String name = split[0];
		int type = Integer.parseInt(split[1]);
		boolean turn = Boolean.parseBoolean(split[2]);
		
		Player p = new Player(name, null);
		p.setTurn(turn);
		p.setType(type);
		return p;
	}
	
	private boolean toState(String stateLine) {
		// Rekkefølge på tilstandsdata
		// 0: foul
		// 1: firstPot
		// 2: point
		// 3: turn
		return Boolean.parseBoolean(stateLine);
	}
	
	private Double[] toShotData(String shotDataLine) {
		String[] split = shotDataLine.split(",");
		return new Double[] {Double.parseDouble(split[0]), 
				Double.parseDouble(split[1]), 
				Double.parseDouble(split[2]), 
				Double.parseDouble(split[3]),
				Double.parseDouble(split[4]),
				Double.parseDouble(split[5])};
	}
}
