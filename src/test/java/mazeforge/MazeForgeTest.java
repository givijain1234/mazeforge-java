package mazeforge;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MazeForgeTest {
    private static int reachableCells(Maze m) {
        boolean[] seen = new boolean[m.width * m.height];
        Deque<Integer> q = new ArrayDeque<>();
        q.add(0);
        seen[0] = true;
        int count = 0;
        while (!q.isEmpty()) {
            int c = q.poll();
            count++;
            int x = c % m.width, y = c / m.width;
            for (int d = 0; d < 4; d++)
                if (m.isOpen(x, y, d)) {
                    int n = m.index(x + Maze.DX[d], y + Maze.DY[d]);
                    if (!seen[n]) { seen[n] = true; q.add(n); }
                }
        }
        return count;
    }

    @ParameterizedTest
    @ValueSource(strings = {"backtracker", "prim", "kruskal"})
    void generatorsProducePerfectMazes(String name) {
        for (int seed = 1; seed <= 20; seed++) {
            Maze m = MazeForgeApi.GENERATORS.get(name).generate(17, 11, new Random(seed));
            assertEquals(17 * 11, reachableCells(m), name + " must connect every cell");
            assertEquals(17 * 11 - 1, m.passageCount(), name + " must be a spanning tree (no loops)");
        }
    }

    @Test
    void sameSeedGivesSameMaze() {
        assertEquals(MazeForgeApi.generate("prim", 15, 10, 7, 0.1), MazeForgeApi.generate("prim", 15, 10, 7, 0.1));
        assertNotEquals(MazeForgeApi.generate("prim", 15, 10, 7, 0.1), MazeForgeApi.generate("prim", 15, 10, 8, 0.1));
    }

    @Test
    void braidingAddsLoops() {
        Maze m = MazeForgeApi.build("kruskal", 20, 20, 3, 0.2);
        assertTrue(m.passageCount() > 20 * 20 - 1);
    }

    @Test
    void everySolverFindsAValidPath() {
        for (int seed = 1; seed <= 15; seed++) {
            Maze m = MazeForgeApi.build("backtracker", 18, 12, seed, 0.12);
            for (MazeSolver s : MazeForgeApi.SOLVERS.values()) {
                SolveResult r = s.solve(m);
                assertTrue(r.found, s.name());
                int[] first = r.path.get(0), last = r.path.get(r.path.size() - 1);
                assertArrayEquals(new int[]{0, 0}, first);
                assertArrayEquals(new int[]{m.width - 1, m.height - 1}, last);
                for (int i = 1; i < r.path.size(); i++) {
                    int[] a = r.path.get(i - 1), b = r.path.get(i);
                    int dir = -1;
                    for (int d = 0; d < 4; d++) if (a[0] + Maze.DX[d] == b[0] && a[1] + Maze.DY[d] == b[1]) dir = d;
                    assertTrue(dir >= 0 && m.isOpen(a[0], a[1], dir), "path must walk through open passages");
                }
                assertEquals(MazeSolver.costOf(m, r.path), r.pathCost);
            }
        }
    }

    @Test
    void optimalityGuaranteesHold() {
        for (int seed = 1; seed <= 40; seed++) {
            Maze m = MazeForgeApi.build("kruskal", 20, 14, seed, 0.15);
            SolveResult bfs = new BfsSolver().solve(m), dij = new DijkstraSolver().solve(m),
                        astar = new AStarSolver().solve(m), dfs = new DfsSolver().solve(m);
            assertEquals(dij.pathCost, astar.pathCost, "A* must match Dijkstra's cost (admissible heuristic)");
            assertTrue(dij.pathCost <= bfs.pathCost, "Dijkstra is never costlier than BFS");
            assertTrue(bfs.pathLength <= dij.pathLength, "BFS is never longer than Dijkstra's path");
            assertTrue(bfs.pathLength <= dfs.pathLength);
            assertTrue(astar.expanded() <= dij.expanded() + 0, "A* should not expand more than Dijkstra here");
        }
    }

    @Test
    void costAwareSolversBeatBfsOnSomeMaze() {
        boolean differs = false;
        for (int seed = 1; seed <= 40 && !differs; seed++) {
            Maze m = MazeForgeApi.build("prim", 25, 16, seed, 0.2);
            differs = new DijkstraSolver().solve(m).pathCost < new BfsSolver().solve(m).pathCost;
        }
        assertTrue(differs, "with loops and terrain, cheapest and shortest paths should sometimes differ");
    }

    @Test
    void unionFindMerges() {
        UnionFind uf = new UnionFind(5);
        assertTrue(uf.union(0, 1));
        assertTrue(uf.union(1, 2));
        assertFalse(uf.union(0, 2));
        assertEquals(uf.find(0), uf.find(2));
        assertNotEquals(uf.find(0), uf.find(4));
    }

    @Test
    void rejectsBadInput() {
        assertThrows(IllegalArgumentException.class, () -> new Maze(1, 5));
        assertThrows(IllegalArgumentException.class, () -> MazeForgeApi.build("nope", 5, 5, 1, 0));
    }
}
