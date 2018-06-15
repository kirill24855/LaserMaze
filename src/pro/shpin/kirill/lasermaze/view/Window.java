package pro.shpin.kirill.lasermaze.view;

import org.lwjgl.opengl.GL;
import pro.shpin.kirill.lasermaze.GLUtil;
import pro.shpin.kirill.lasermaze.model.Game;
import pro.shpin.kirill.lasermaze.model.Line;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Window {

	public int width;
	public int height;
	private String title;

	private long windowHandle;

	// Textures

	private int prevScore = -1;
	private List<Integer> scoreDigits; // Global scope so that there isn't a need to re-calculate this list every time when it stays the same

	private boolean firstRender;
	private int FIELD_WIDTH;
	private int FIELD_HEIGHT;

	public Window(String title, int width, int height) {
		this.width = width;
		this.height = height;
		this.title = title;
	}

	public void init() {
		glfwInit();

		windowHandle = glfwCreateWindow(width, height, title, 0, 0);
		glfwMakeContextCurrent(windowHandle);

		GL.createCapabilities();
		glClearColor(0.2f, 0.2f, 0.2f, 1f); // Background color

		// Texture init
		glEnable(GL_TEXTURE_2D);
		//tex = GLUtil.loadTexture("/textures/bonusYellowPurple.png");

		firstRender = true;

		// Below -- DO NOT TOUCH (will break)
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, 0, height, -1, 1);
		glMatrixMode(GL_MODELVIEW);
	}

	public long getHandle() {
		return windowHandle;
	}

	public void updateInput() {
		glfwPollEvents();
	}

	public void render(Game game) {
		if (firstRender) {
			firstRender = false;
			FIELD_WIDTH = Game.FIELD_WIDTH;
			FIELD_HEIGHT = Game.FIELD_HEIGHT;
		}

		glClear(GL_COLOR_BUFFER_BIT);

		drawLines(game);
		drawWalls(game.getWalls());

		glfwSwapBuffers(windowHandle);
	}

	private void drawWalls(List<Line> walls) {
		walls.forEach(wall -> GLUtil.drawLine(
				transformX(wall.x1),
				transformY(wall.y1),
				transformX(wall.x2),
				transformY(wall.y2),
				1f ,1f, 1f
		));
	}

	private void drawLines(Game game) {
		if (game.isAiming()) {
			float deltaX = game.mouseX - transformX(game.startX);
			float deltaY = game.mouseY - transformY(game.startY);
			double theta = Math.atan2(deltaY, deltaX);
			GLUtil.drawLine(
					transformX(game.startX),
					transformY(game.startY),
					(float) Math.cos(theta)*(width+height) + transformX(game.startX),
					(float) Math.sin(theta)*(width+height) + transformY(game.startY),
					0f,1f, 1f
			);
		} else {
			List<Line> shots = game.getShots();
			for (int i = 0, l = shots.size(); i < l; i++) {
				Line shot = shots.get(i);
				GLUtil.drawLine(
						transformX(shot.x1),
						transformY(shot.y1),
						transformX(shot.x2),
						transformY(shot.y2),
						1f, 0f, (float) i/l
				);
			}
		}
	}

	private float transformX(double val) {
		float t = (float) (val*width/FIELD_WIDTH);
		if (t == 0) t = 1;
		return t;
	}

	private float transformY(double val) {
		float t = (float) (val*height/FIELD_HEIGHT);
		if (t == 0) t = 1;
		return t;
	}

	public boolean isKeyPressed(int keyCode) {
		return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
	}

	public boolean shouldClose() {
		return glfwWindowShouldClose(windowHandle);
	}
}
