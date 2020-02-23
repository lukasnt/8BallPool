package app.game;

import app.entities.Ball;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;

public class Drawer {
	
	private GraphicsContext gc;
	
	public Drawer(GraphicsContext gc) {
		this.gc = gc;
	}
	
	public void drawBackground() {
		gc.setFill(Color.BLUE);
		gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
	}
	
	public void drawBall(Ball ball) {
		gc.setFill(ball.getColor());
		gc.fillArc(ball.getPosX() - ball.getRadius(), ball.getPosY() - ball.getRadius(), ball.getRadius() * 2, ball.getRadius() * 2, 0, 360, ArcType.ROUND);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(2);
		gc.strokeArc(ball.getPosX() - ball.getRadius(), ball.getPosY() - ball.getRadius(), ball.getRadius() * 2, ball.getRadius() * 2, 0, 360, ArcType.OPEN);
		if (ball.getType() == Ball.STRIPE) {
			gc.setFill(Color.WHITE);
			gc.fillArc(ball.getPosX() - ball.getRadius(), ball.getPosY() - ball.getRadius() / 2, ball.getRadius() * 2, ball.getRadius(), 0, 360, ArcType.ROUND);
		}
		//gc.setFill(Color.BLACK);
		//gc.fillText("1" ,ball.getPosX(), ball.getPosY());
	}
	
	public void drawCircle(double x, double y, double r, Color color) {
		gc.setStroke(color);
		gc.strokeArc(x, y, r * 2, r * 2, 0, 360, ArcType.ROUND);
	}
	
	public void fillCircle(Circle circle) {
		gc.setStroke(circle.getStroke());
		gc.setFill(circle.getFill());
		gc.fillArc(circle.getCenterX() - circle.getRadius(), circle.getCenterY() - circle.getRadius(), circle.getRadius() * 2, circle.getRadius() * 2, 0, 360, ArcType.ROUND);
	}
	
	public void drawLine(double x0, double y0, double x1, double y1, Color color) {
		gc.setLineCap(StrokeLineCap.ROUND);
		gc.setLineWidth(4);
		gc.setStroke(color);
		gc.strokeLine(x0, y0, x1, y1);
	}
	
	public void drawReplayR(double x, double y) {
		gc.setFill(Color.RED);
		gc.setStroke(Color.BLACK);
		gc.setFont(new Font(32));
		gc.fillText("R", x, y);
	}
}
