package mazeforge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/** Breadth-first search: fewest steps, ignores terrain cost. */
public final class BfsSolver implements MazeSolver {
    public String name() { return "bfs"; }
    public boolean optimalForCost() { return false; }
    public boolean optimalForSteps() { return true; }

    public SolveResult solve(Maze m) {
        int goal = m.index(m.width - 1, m.height - 1);
        int[] parent = new int[m.width * m.height];
        Arrays.fill(parent, -2);
        parent[0] = -1;
        Deque<Integer> q = new ArrayDeque<>();
        q.add(0);
        List<int[]> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int c = q.poll();
            int x = c % m.width, y = c / m.width;
            order.add(new int[]{x, y});
            if (c == goal) {
                List<int[]> path = MazeSolver.rebuild(m, parent, goal);
                return new SolveResult(name(), true, order, path, MazeSolver.costOf(m, path));
            }
            for (int d = 0; d < 4; d++) {
                if (!m.isOpen(x, y, d)) continue;
                int n = m.index(x + Maze.DX[d], y + Maze.DY[d]);
                if (parent[n] == -2) { parent[n] = c; q.add(n); }
            }
        }
        return new SolveResult(name(), false, order, new ArrayList<>(), 0);
    }
}
