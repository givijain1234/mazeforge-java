package mazeforge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Randomized Kruskal: shuffle every wall, knock it down if it joins two separate regions. */
public final class KruskalGenerator implements MazeGenerator {
    public String name() { return "kruskal"; }

    public Maze generate(int w, int h, Random rnd) {
        Maze m = new Maze(w, h);
        List<int[]> walls = new ArrayList<>();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                if (x + 1 < w) walls.add(new int[]{x, y, 1});
                if (y + 1 < h) walls.add(new int[]{x, y, 2});
            }
        Collections.shuffle(walls, rnd);
        UnionFind uf = new UnionFind(w * h);
        for (int[] wall : walls) {
            int nx = wall[0] + Maze.DX[wall[2]], ny = wall[1] + Maze.DY[wall[2]];
            if (uf.union(m.index(wall[0], wall[1]), m.index(nx, ny))) m.carve(wall[0], wall[1], wall[2]);
        }
        return m;
    }
}
