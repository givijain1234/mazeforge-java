package mazeforge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/** Depth-first search: finds some path, with no optimality guarantee. */
public final class DfsSolver implements MazeSolver {
    public String name() { return "dfs"; }
    public boolean optimalForCost() { return false; }
    public boolean optimalForSteps() { return false; }

    public SolveResult solve(Maze m) {
        int goal = m.index(m.width - 1, m.height - 1);
        int[] parent = new int[m.width * m.height];
        Arrays.fill(parent, -2);
        parent[0] = -1;
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        List<int[]> order = new ArrayList<>();
        boolean[] expanded = new boolean[parent.length];
        while (!stack.isEmpty()) {
            int c = stack.pop();
            if (expanded[c]) continue;
            expanded[c] = true;
            int x = c % m.width, y = c / m.width;
            order.add(new int[]{x, y});
            if (c == goal) {
                List<int[]> path = MazeSolver.rebuild(m, parent, goal);
                return new SolveResult(name(), true, order, path, MazeSolver.costOf(m, path));
            }
            for (int d = 3; d >= 0; d--) {
                if (!m.isOpen(x, y, d)) continue;
                int n = m.index(x + Maze.DX[d], y + Maze.DY[d]);
                if (parent[n] == -2) { parent[n] = c; stack.push(n); }
            }
        }
        return new SolveResult(name(), false, order, new ArrayList<>(), 0);
    }
}
