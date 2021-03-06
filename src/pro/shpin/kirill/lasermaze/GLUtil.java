package pro.shpin.kirill.lasermaze;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("SameParameterValue")
public class GLUtil {

	private final static int[] NUMS = {
			loadTexture("/textures/numbers/0.png"),
			loadTexture("/textures/numbers/1.png"),
			loadTexture("/textures/numbers/2.png"),
			loadTexture("/textures/numbers/3.png"),
			loadTexture("/textures/numbers/4.png"),
			loadTexture("/textures/numbers/5.png"),
			loadTexture("/textures/numbers/6.png"),
			loadTexture("/textures/numbers/7.png"),
			loadTexture("/textures/numbers/8.png"),
			loadTexture("/textures/numbers/9.png"),
	};

	public static void texRectCenter(float x, float y, float w, float h, float rot, int tex, float alpha, float repeatNumX, float repeatNumY) {
		glPushMatrix();

		glTranslatef(x, y, 0);
		glRotatef(rot, 0, 0, 1);

		glColor4f(1, 1, 1, alpha);

		glBindTexture(GL_TEXTURE_2D, tex);

		glBegin(GL_QUADS);
		glTexCoord2f(0, repeatNumY);
		glVertex2f(-w/2, -h/2);
		glTexCoord2f(repeatNumX, repeatNumY);
		glVertex2f( w/2, -h/2);
		glTexCoord2f(repeatNumX, 0);
		glVertex2f( w/2,  h/2);
		glTexCoord2f(0, 0);
		glVertex2f(-w/2,  h/2);
		glEnd();

		glBindTexture(GL_TEXTURE_2D, 0);

		glPopMatrix();
	}

	public static void texRectCorner(float x, float y, float w, float h, float rot, int tex, float alpha, float repeatNumX, float repeatNumY) {
		glPushMatrix();

		glTranslatef(x, y, 0);
		glRotatef(rot, 0, 0, 1);

		glColor4f(1, 1, 1, alpha);

		glBindTexture(GL_TEXTURE_2D, tex);

		glBegin(GL_QUADS);
		glTexCoord2f(0, repeatNumY);
		glVertex2f(0, 0);
		glTexCoord2f(repeatNumX, repeatNumY);
		glVertex2f( w, 0);
		glTexCoord2f(repeatNumX, 0);
		glVertex2f( w,  h);
		glTexCoord2f(0, 0);
		glVertex2f(0,  h);
		glEnd();

		glBindTexture(GL_TEXTURE_2D, 0);

		glPopMatrix();
	}

	public static void tileTexRectCenter(float x, float y, float w, float h, float rot, int tex, int texWidth, int texHeight) {
		texRectCenter(x, y, w, h, rot, tex, 1f, w/texWidth, h/texHeight);
	}

	public static void tileTexRectCorner(float x, float y, float w, float h, float rot, int tex, int texWidth, int texHeight) {
		texRectCorner(x, y, w, h, rot, tex, 1f, w/texWidth, h/texHeight);
	}

	public static void fillRectCenter(float x, float y, float w, float h, float rot, float red, float green, float blue) {
		glPushMatrix();

		glTranslatef(x, y, 0);
		glRotatef(rot, 0, 0, 1);

		glColor3f(red, green, blue);

		glBegin(GL_QUADS);
		glVertex2f(-w/2, -h/2);
		glVertex2f( w/2, -h/2);
		glVertex2f( w/2,  h/2);
		glVertex2f(-w/2,  h/2);
		glEnd();

		glPopMatrix();
	}

	public static void fillRectCorner(float x, float y, float w, float h, float rot, float red, float green, float blue) {
		glPushMatrix();

		glTranslatef(x, y, 0);
		glRotatef(rot, 0, 0, 1);

		glColor3f(red, green, blue);

		glBegin(GL_QUADS);
		glVertex2f(0, 0);
		glVertex2f( w, 0);
		glVertex2f( w,  h);
		glVertex2f(0,  h);
		glEnd();

		glPopMatrix();
	}

	public static void drawNumber(List<Integer> digits, float x, float y, float w, float h) {
		for (int i = 0; i < digits.size(); i++) {
			Integer num = digits.get(i);
			float offsetX = (digits.size() - i-1) * w; // The digits must be in reverse order (easier to obtain them anyway)
			texRectCorner(x + offsetX, y, w, h, 0, NUMS[num], 1f, 1f, 1f);
		}
	}

	public static void fillTri(float x1, float y1, float x2, float y2, float x3, float y3, float r, float g, float b) {
		glPushMatrix();

		glColor3f(r, g, b);

		glBegin(GL_TRIANGLES);
		glVertex2f(x1, y1);
		glVertex2f(x2, y2);
		glVertex2f(x3, y3);
		glEnd();

		glPopMatrix();
	}

	public static void drawLine(float x1, float y1, float x2, float y2, float red, float green, float blue) {
		glPushMatrix();

		glColor3f(red, green, blue);

		glBegin(GL_LINES);
		glVertex2f(x1, y1);
		glVertex2f(x2, y2);
		glEnd();

		glPopMatrix();
	}

	public static int loadTexture(String path) {
		int width = 0;
		int height = 0;
		int[] pixels = null;
		try {
			BufferedImage image = ImageIO.read(Logger.class.getResourceAsStream(path));
			width = image.getWidth();
			height = image.getHeight();
			pixels = new int[width*height];
			image.getRGB(0,0,width,height,pixels,0,width);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int[] data = new int[width*height];
		for(int i = 0;i < width*height;i++) {
			int a = (pixels[i] & 0xff000000) >> 24;
			int r = (pixels[i] & 0xff0000) >> 16;
			int g = (pixels[i] & 0xff00) >> 8;
			int b = (pixels[i] & 0xff);

			data[i] = a << 24 | b << 16 | g << 8 | r;
		}

		int tex = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, tex);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,width,height,0,GL_RGBA,GL_UNSIGNED_BYTE, data);
		glBindTexture(GL_TEXTURE_2D, 0);
		return tex;
	}

}
