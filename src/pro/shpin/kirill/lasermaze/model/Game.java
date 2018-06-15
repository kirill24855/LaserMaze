package pro.shpin.kirill.lasermaze.model;

import pro.shpin.kirill.lasermaze.Sound;
import pro.shpin.kirill.lasermaze.view.Window;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Game {

	private static final boolean DEBUG_MODE = true;

	public static final int FIELD_WIDTH = 8;
	public static final int FIELD_HEIGHT = 8;
	private static final int MAX_SHOTS = 50;

	private Sound sound;

	private int width;
	private int height;

	public int mouseX;
	public int mouseY;

	private boolean leftButtonPressed = false;
	private boolean lastUpdateLeftButtonState = false;

	private boolean spacePressed = false;
	private boolean lastUpdateSpaceState = false;

	private Random rng;
	private static final long RNG_SEED = 0;

	private FieldGenerator fieldGen;
	private List<Line> walls;

	public float startX;
	public float startY;
	public float endX;
	public float endY;

	private boolean aiming;
	private boolean lastUpdateAimState;
	private boolean calcShot;
	private boolean hit;

	private double curTheta;
	private double posX;
	private double posY;
	private List<Line> shots;

	public Game() {}

	public void init(Window window) {
		width = window.width;
		height = window.height;

		sound = new Sound("/sounds/8BitTeleport.wav");

		glfwSetCursorPosCallback(window.getHandle(), (windowHandle, posX, posY) -> {
				mouseX = (int) posX;
				mouseY = height - (int) posY;
		});
		glfwSetMouseButtonCallback(window.getHandle(), (windowHandle, button, action, mode) ->
				leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS
		);

		fieldGen = new FieldGenerator(FIELD_WIDTH, FIELD_HEIGHT, this::debugPrint);

		rng = new Random(RNG_SEED);

		reinit();
	}

	private void reinit() {
		walls = fieldGen.generate();

		startX = 0.5f;
		startY = 0.5f;
		endX = FIELD_WIDTH - 0.5f;
		endY = FIELD_HEIGHT - 0.5f;

		aiming = false;
		calcShot = false;
		shots = new ArrayList<>();
		hit = false;
	}

	private void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

	public void updateInput(Window window) {
		spacePressed = window.isKeyPressed(GLFW_KEY_SPACE);
	}

	public void update(float interval) {
		processInput();

		if (calcShot) {
			if (curTheta < 0) curTheta += 2*Math.PI;

			Line reflectiveWall = getReflectiveWall(curTheta, posX, posY);

			double m = Math.tan(curTheta);
			double x;
			double y;

			// wall is vertical
			if (reflectiveWall.x1 == reflectiveWall.x2) {
				x = reflectiveWall.x1;
				y = (x - posX)*m + posY;

				curTheta = Math.PI-curTheta;
				if (curTheta < 0) curTheta += 2*Math.PI;
			} else {
				y = reflectiveWall.y1;
				x = (y - posY)/m + posX;

				curTheta = 2*Math.PI - curTheta;
			}

			addShot(new Line(posX, posY, x, y));

			posX = x;
			posY = y;
		}

		lastUpdateLeftButtonState = leftButtonPressed;
		lastUpdateSpaceState = spacePressed;
		lastUpdateAimState = aiming;
	}

	private Line getReflectiveWall(double theta, double posX, double posY) {
		int xIncrement;
		int initX;
		if (theta > Math.PI/2 && theta < Math.PI*3/2) {
			xIncrement = -1;
			initX = (int) Math.ceil(posX) - 1;
		} else {
			xIncrement = 1;
			initX = (int) Math.floor(posX) + 1;
		}

		int yIncrement = theta < Math.PI ? 1 : -1;

		int closestYInDir = roundInDir(posY, yIncrement);
		Line potentialWall;
		// for all integer verticals that the line crosses within the field
		for (int curX = initX; curX >= 0 && curX <= FIELD_WIDTH; curX += xIncrement) {
			double yForIntegerX = (curX-posX)*Math.tan(theta) + posY;

			// for all integer horizontals that the line crosses between each vertical
			for (int curY = closestYInDir; curY >= 0 && curY <= FIELD_HEIGHT && curY != roundInDir(yForIntegerX, yIncrement); curY += yIncrement) {
				double xForIntegerY = (curY-posY)/Math.tan(theta) + posX;
				potentialWall = new Line((int) Math.floor(xForIntegerY), curY, (int) Math.floor(xForIntegerY)+1, curY);
				if (this.walls.contains(potentialWall)) return potentialWall;
			}

			closestYInDir = roundInDir(yForIntegerX, yIncrement);

			potentialWall = new Line(curX, (int) Math.floor(yForIntegerX), curX, (int) Math.floor(yForIntegerX)+1);
			if (this.walls.contains(potentialWall)) return potentialWall;
		}

		potentialWall = new Line(0, 0, 0, 1);
		debugPrint("NO OBSTRUCTING WALL IDENTIFIED, USING " + potentialWall + "\n]");
		return potentialWall;
	}

	private int roundInDir(double val, int incrementDir) {
		return (int) (incrementDir > 0 ? Math.floor(val)+1 : Math.ceil(val)-1);
	}

	private void addShot(Line shot) {
		shots.add(shot);
		if (shots.size() > MAX_SHOTS) shots.remove(0);
	}

	private void processInput() {
		if (spacePressed && !lastUpdateSpaceState) reinit();
		aiming = leftButtonPressed && !hit;
		if (!aiming && lastUpdateAimState) {
			shots = new ArrayList<>();
			curTheta = Math.atan2(mouseY - startY*height/FIELD_HEIGHT, mouseX - startX*width/FIELD_WIDTH);
			posX = startX;
			posY = startY;

			calcShot = true;
		}
	}

	private void debugPrint(String message) {
		if (DEBUG_MODE) System.out.println(message);
	}

	public List<Line> getWalls() {
		return walls;
	}

	public boolean isAiming() {
		return aiming;
	}

	public List<Line> getShots() {
		return shots;
	}

	public void cleanup() {}
}
