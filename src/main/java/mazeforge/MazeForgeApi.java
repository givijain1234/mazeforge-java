package mazeforge;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Small string-in, JSON-out facade so the same compiled Java can be driven from a browser
 * (through CheerpJ) or from the command line. Every call is deterministic for a given seed.
 */
public final class MazeForgeApi {
    private MazeForgeApi() {}

    public static final Map<String, MazeGenerator> GENERATORS = new TreeMap<>();
    public static final Map<String, MazeSolver> SOLVERS = new TreeMap<>();
    static {
        for (MazeGenerator g : new MazeGenerator[]{new RecursiveBacktracker(), new PrimGenerator(), new KruskalGenerator()})
            GENERATORS.put(g.name(), g);
        for (MazeSolver s : new MazeSolver[]{new BfsSolver(), new DfsSolver(), new DijkstraSolver(), new AStarSolver()})
            SOLVERS.put(s.name(), s);
    }

    /** Builds the maze for a seed: generate, add loops, scatter terrain. Same inputs, same maze. */
    public static Maze build(String generator, int width, int height, int seed, double loops) {
        MazeGenerator g = GENERATORS.get(generator);
        if (g == null) throw new IllegalArgumentException("unknown generator: " + generator);
        Random rnd = new Random(seed);
        Maze m = g.generate(width, height, rnd);
        m.braid(loops, rnd);
        m.scatterTerrain(rnd);
        return m;
    }

    public static String generate(String generator, int width, int height, int seed, double loops) {
        Maze m = build(generator, width, height, seed, loops);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"width\":").append(m.width).append(",\"height\":").append(m.height)
          .append(",\"passages\":").append(m.passageCount()).append(",\"cells\":[");
        for (int i = 0; i < m.width * m.height; i++) {
            if (i > 0) sb.append(',');
            sb.append(m.openMask(i % m.width, i / m.width));
        }
        sb.append("],\"terrain\":[");
        for (int i = 0; i < m.width * m.height; i++) {
            if (i > 0) sb.append(',');
            sb.append(m.terrainCost(i % m.width, i / m.width));
        }
        return sb.append("]}").toString();
    }

    public static String solve(String generator, int width, int height, int seed, double loops, String solver) {
        MazeSolver s = SOLVERS.get(solver);
        if (s == null) throw new IllegalArgumentException("unknown solver: " + solver);
        SolveResult r = s.solve(build(generator, width, height, seed, loops));
        StringBuilder sb = new StringBuilder();
        sb.append("{\"solver\":\"").append(r.solver).append("\",\"found\":").append(r.found)
          .append(",\"expanded\":").append(r.expanded())
          .append(",\"pathLength\":").append(r.pathLength)
          .append(",\"pathCost\":").append(r.pathCost)
          .append(",\"optimalForSteps\":").append(s.optimalForSteps())
          .append(",\"optimalForCost\":").append(s.optimalForCost())
          .append(",\"expandedOrder\":");
        appendCells(sb, r.expandedOrder);
        sb.append(",\"path\":");
        appendCells(sb, r.path);
        return sb.append('}').toString();
    }

    private static void appendCells(StringBuilder sb, List<int[]> cells) {
        sb.append('[');
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(cells.get(i)[0]).append(',').append(cells.get(i)[1]);
        }
        sb.append(']');
    }

    /** Command-line demo: java -cp mazeforge.jar mazeforge.MazeForgeApi [generator] [seed] */
    public static void main(String[] args) {
        String gen = args.length > 0 ? args[0] : "backtracker";
        int seed = args.length > 1 ? Integer.parseInt(args[1]) : 42;
        Maze m = build(gen, 20, 12, seed, 0.12);
        System.out.printf("%-9s %9s %11s %9s%n", "solver", "expanded", "pathLength", "pathCost");
        for (MazeSolver s : SOLVERS.values()) {
            SolveResult r = s.solve(m);
            System.out.printf("%-9s %9d %11d %9d%n", s.name(), r.expanded(), r.pathLength, r.pathCost);
        }
    }
}
