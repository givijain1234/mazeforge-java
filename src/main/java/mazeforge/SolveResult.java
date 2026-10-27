package mazeforge;

import java.util.List;

/** Everything a solver reports. Metrics are counts and costs, never wall-clock time. */
public final class SolveResult {
    public final String solver;
    public final boolean found;
    /** Cells in the order they were taken off the frontier (expanded). */
    public final List<int[]> expandedOrder;
    public final List<int[]> path;
    public final int pathLength; // number of steps
    public final int pathCost;   // sum of terrain cost of every cell entered

    public SolveResult(String solver, boolean found, List<int[]> expandedOrder, List<int[]> path, int pathCost) {
        this.solver = solver;
        this.found = found;
        this.expandedOrder = expandedOrder;
        this.path = path;
        this.pathLength = found ? path.size() - 1 : -1;
        this.pathCost = found ? pathCost : -1;
    }

    public int expanded() { return expandedOrder.size(); }
}
