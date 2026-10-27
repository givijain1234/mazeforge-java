package mazeforge;

import java.util.Random;

/** Strategy interface for maze generation algorithms. */
public interface MazeGenerator {
    String name();
    /** Fills a fresh maze with a perfect maze (a spanning tree of the grid). */
    Maze generate(int width, int height, Random rnd);
}
