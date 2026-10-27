package mazeforge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/** Dijkstra: lowest total terrain cost, explores outward in every direction. */
public final class DijkstraSolver implements MazeSolver {
    public String name() { return "dijkstra"; }
    public boolean optimalForCost() { return true; }
    public boolean optimalForSteps() { return false; }

    public SolveResult solve(Maze m) {
        return PriorityQueueSearch.run(m, name(), false);
    }
}
