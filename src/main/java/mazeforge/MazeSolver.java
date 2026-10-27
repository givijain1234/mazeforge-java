package mazeforge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Strategy interface for path-finding algorithms. Start is (0,0), goal is the bottom-right cell. */
public interface MazeSolver {
    String name();
    /** True when the returned path is guaranteed to have the fewest terrain-cost, false when it is only guaranteed to exist. */
    boolean optimalForCost();
    boolean optimalForSteps();
    SolveResult solve(Maze maze);

    /** Shared helper: rebuild a path from a parent array. */
    static List<int[]> rebuild(Maze m, int[] parent, int goal) {
        List<int[]> path = new ArrayList<>();
        for (int c = goal; c != -1; c = parent[c]) path.add(new int[]{c % m.width, c / m.width});
        Collections.reverse(path);
        return path;
    }

    static int costOf(Maze m, List<int[]> path) {
        int cost = 0;
        for (int i = 1; i < path.size(); i++) cost += m.terrainCost(path.get(i)[0], path.get(i)[1]);
        return cost;
    }
}
