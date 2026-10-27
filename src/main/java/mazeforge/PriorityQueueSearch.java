package mazeforge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/** Shared best-first search used by Dijkstra (no heuristic) and A* (Manhattan heuristic). */
final class PriorityQueueSearch {
    private PriorityQueueSearch() {}

    static SolveResult run(Maze m, String name, boolean useHeuristic) {
        int n = m.width * m.height;
        int goal = m.index(m.width - 1, m.height - 1);
        int[] dist = new int[n];
        int[] parent = new int[n];
        boolean[] done = new boolean[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[0] = 0;
        // entries: {priority, tieBreaker(cell), cell}
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0]) : Integer.compare(a[1], b[1]));
        pq.add(new int[]{h(m, 0, useHeuristic), 0, 0});
        List<int[]> order = new ArrayList<>();
        while (!pq.isEmpty()) {
            int c = pq.poll()[2];
            if (done[c]) continue;
            done[c] = true;
            int x = c % m.width, y = c / m.width;
            order.add(new int[]{x, y});
            if (c == goal) {
                List<int[]> path = MazeSolver.rebuild(m, parent, goal);
                return new SolveResult(name, true, order, path, dist[goal]);
            }
            for (int d = 0; d < 4; d++) {
                if (!m.isOpen(x, y, d)) continue;
                int nx = x + Maze.DX[d], ny = y + Maze.DY[d];
                int nb = m.index(nx, ny);
                int nd = dist[c] + m.terrainCost(nx, ny);
                if (nd < dist[nb]) {
                    dist[nb] = nd;
                    parent[nb] = c;
                    pq.add(new int[]{nd + h(m, nb, useHeuristic), nb, nb});
                }
            }
        }
        return new SolveResult(name, false, order, new ArrayList<>(), 0);
    }

    private static int h(Maze m, int cell, boolean use) {
        if (!use) return 0;
        int x = cell % m.width, y = cell / m.width;
        return (m.width - 1 - x) + (m.height - 1 - y);
    }
}
