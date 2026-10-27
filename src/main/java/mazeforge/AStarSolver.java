package mazeforge;

/**
 * A*: same optimal cost as Dijkstra, guided by a heuristic. The heuristic is Manhattan distance
 * times the minimum terrain cost (1), which never overestimates, so the result stays optimal.
 */
public final class AStarSolver implements MazeSolver {
    public String name() { return "astar"; }
    public boolean optimalForCost() { return true; }
    public boolean optimalForSteps() { return false; }

    public SolveResult solve(Maze m) {
        return PriorityQueueSearch.run(m, name(), true);
    }
}
