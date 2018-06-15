package pro.shpin.kirill.lasermaze.model;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FieldGenerator {

	private final int FIELD_WIDTH;
	private final int FIELD_HEIGHT;
	private final Consumer<String> debugPrint;

	public FieldGenerator(int width, int height, Consumer<String> debugPrint) {
		this.FIELD_WIDTH = width;
		this.FIELD_HEIGHT = height;
		this.debugPrint = debugPrint;
	}

	public List<Line> generate() {
		return generate(new Random());
	}

	public List<Line> generate(Random rng) {
		List<Connection> superGraph = new ArrayList<>();
		IntStream.range(0, FIELD_WIDTH).forEachOrdered(x -> {
			IntStream.range(0, FIELD_HEIGHT).forEachOrdered(y -> {
				if (y > 0) superGraph.add(new Connection(new Location(x, y), new Location(x, y - 1)));
				if (x > 0) superGraph.add(new Connection(new Location(x, y), new Location(x - 1, y)));
			});
		});

		debugPrint.accept("PREPARATION STEP COMPLETE");

		Map<Location, Location> spanningGraph = new HashMap<>();
		List<Location> openNodes = new ArrayList<>();
		List<Location> closedSet = new ArrayList<>();

		Location root = new Location(rng.nextInt(FIELD_WIDTH), rng.nextInt(FIELD_HEIGHT));
		debugPrint.accept("Root: " + root);

		spanningGraph.put(root, null);
		openNodes.add(root);

		while (openNodes.size() > 0) {
			Location node = openNodes.get(rng.nextInt(openNodes.size()));
			openNodes.remove(node);
			closedSet.add(node);

			for (Location adjacent : getAdjacent(node.x, node.y)) {
				if (!openNodes.contains(adjacent) && !closedSet.contains(adjacent)) {
					spanningGraph.put(adjacent, node);
					openNodes.add(adjacent);
				}
			}
		}

		debugPrint.accept("SPANNING GRAPH GENERATION COMPLETE");

		// invert the final graph in order to get the walls instead of the actual field
		List<Connection> inverseSpanningGraph = new ArrayList<>();
		boolean foundMatch;
		for (Connection superConnection : superGraph) {
			foundMatch = false;
			for (Location child : spanningGraph.keySet()) {
				Location parent = spanningGraph.get(child);
				if (superConnection.equals(new Connection(child, parent)) || superConnection.equals(new Connection(parent, child))) foundMatch = true;
			}
			if (!foundMatch) inverseSpanningGraph.add(superConnection);
		}

		debugPrint.accept("SPANNING GRAPH INVERSION COMPLETE");

		// rotate each connection such that instead of connecting the nodes, they block them like walls
		List<Line> walls = new ArrayList<>();
		for (Connection connection : inverseSpanningGraph) {
			// if vertical connection, rotate to horizontal
			if (connection.nodes[0].x - connection.nodes[1].x == 0) {
				float middleY = (connection.nodes[0].y + connection.nodes[1].y)/2f;
				walls.add(new Line(
						connection.nodes[0].x - 0.5f + 0.5f,
						middleY + 0.5f,
						connection.nodes[1].x + 0.5f + 0.5f,
						middleY + 0.5f
				));
			}

			// if horizontal connection, rotate to vertical
			else if (connection.nodes[0].y - connection.nodes[1].y == 0) {
				float middleX = (connection.nodes[0].x + connection.nodes[1].x)/2f;
				walls.add(new Line(
						middleX + 0.5f,
						connection.nodes[0].y - 0.5f + 0.5f,
						middleX + 0.5f,
						connection.nodes[1].y + 0.5f + 0.5f
				));
			} else {
				debugPrint.accept("WALL ROTATION SCREWED UP");
			}
		}

		for (int x = 0; x < FIELD_WIDTH; x++) {
			walls.add(new Line(x, 0, x+1, 0));
			walls.add(new Line(x, FIELD_HEIGHT, x+1, FIELD_HEIGHT));
		}
		for (int y = 0; y < FIELD_HEIGHT; y++) {
			walls.add(new Line(0, y, 0, y+1));
			walls.add(new Line(FIELD_WIDTH, y, FIELD_WIDTH, y+1));
		}

		debugPrint.accept("WALL GENERATION COMPLETE\n");

		return walls;
	}

	private List<Location> getAdjacent(int x, int y) {
		List<Location> nodes = new ArrayList<>();
		for (int i = -1; i < 2; i+=2) {
			if (x+i >= 0 && x+i < FIELD_WIDTH) nodes.add(new Location(x+i, y));
			if (y+i >= 0 && y+i < FIELD_HEIGHT) nodes.add(new Location(x, y+i));
		}

		return nodes;
	}

	private class Location {
		private int x;
		private int y;

		private Location(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "(" + x + ", " + y + ")";
		}

		@Override
		public boolean equals(Object o) {
			return (o instanceof Location) && (((Location) o).x == x && ((Location) o).y == y);
		}
	}

	private class Connection {

		private Location[] nodes;

		private Connection(Location loc0, Location loc1) {
			nodes = new Location[2];
			nodes[0] = loc0;
			nodes[1] = loc1;
		}

		@Override
		public String toString() {
			return "{" + nodes[0] + ", " + nodes[1] + "}";
		}

		@Override
		public boolean equals(Object o) {
			return (o instanceof Connection) && (
					(nodes[0].equals(((Connection) o).nodes[0]) && nodes[1].equals(((Connection) o).nodes[1])) ||
					(nodes[0].equals(((Connection) o).nodes[1]) && nodes[1].equals(((Connection) o).nodes[0]))
			);
		}
	}
}
