package mazeforge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Randomized Prim: grows from one cell via a random frontier, giving many short dead ends. */
public final class PrimGenerator implements MazeGenerator {
    public String name() { return "prim"; }

    public Maze generate(int w, int h, Random rnd) {
        Maze m = new Maze(w, h);
        boolean[] in = new boolean[w * h];
        List<int[]> frontier = new ArrayList<>(); // {x, y, dirBackToTree}
        int sx = rnd.nextInt(w), sy = rnd.nextInt(h);
        in[m.index(sx, sy)] = true;
        addFrontier(m, in, sx, sy, frontier);
        while (!frontier.isEmpty()) {
            int pick = rnd.nextInt(frontier.size());
            int[] f = frontier.get(pick);
            frontier.set(pick, frontier.get(frontier.size() - 1));
            frontier.remove(frontier.size() - 1);
            if (in[m.index(f[0], f[1])]) continue;
            m.carve(f[0], f[1], f[2]);
            in[m.index(f[0], f[1])] = true;
            addFrontier(m, in, f[0], f[1], frontier);
        }
        return m;
    }

    private static void addFrontier(Maze m, boolean[] in, int x, int y, List<int[]> frontier) {
        for (int d = 0; d < 4; d++) {
            int nx = x + Maze.DX[d], ny = y + Maze.DY[d];
            if (m.inBounds(nx, ny) && !in[m.index(nx, ny)]) frontier.add(new int[]{nx, ny, (d + 2) % 4});
        }
    }
}
