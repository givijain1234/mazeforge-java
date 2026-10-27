package mazeforge;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/** Depth-first carving with an explicit stack: long winding corridors. */
public final class RecursiveBacktracker implements MazeGenerator {
    public String name() { return "backtracker"; }

    public Maze generate(int w, int h, Random rnd) {
        Maze m = new Maze(w, h);
        boolean[] seen = new boolean[w * h];
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{rnd.nextInt(w), rnd.nextInt(h)});
        seen[m.index(stack.peek()[0], stack.peek()[1])] = true;
        while (!stack.isEmpty()) {
            int[] c = stack.peek();
            int[] order = shuffledDirs(rnd);
            boolean moved = false;
            for (int d : order) {
                int nx = c[0] + Maze.DX[d], ny = c[1] + Maze.DY[d];
                if (m.inBounds(nx, ny) && !seen[m.index(nx, ny)]) {
                    m.carve(c[0], c[1], d);
                    seen[m.index(nx, ny)] = true;
                    stack.push(new int[]{nx, ny});
                    moved = true;
                    break;
                }
            }
            if (!moved) stack.pop();
        }
        return m;
    }

    static int[] shuffledDirs(Random rnd) {
        int[] d = {0, 1, 2, 3};
        for (int i = 3; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = d[i]; d[i] = d[j]; d[j] = t;
        }
        return d;
    }
}
