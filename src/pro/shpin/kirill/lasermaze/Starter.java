package pro.shpin.kirill.lasermaze;

import pro.shpin.kirill.lasermaze.control.Engine;
import pro.shpin.kirill.lasermaze.model.Game;

public class Starter {

	public static void main(String[] args) {
		Game game = new Game();
		Engine engine;
		try {
			engine = new Engine("LD40", 800, 800, game);
			engine.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
