package mazeforge;

import java.util.Random;

/** A grid maze. Each cell stores which of its four sides are open (a bitmask) and a terrain cost. */
public final class Maze {
    public static final int N = 1, E = 2, S = 4, W = 8;
    public static final int[] DX = {0, 1, 0, -1};
    public static final int[] DY = {-1, 0, 1, 0};
    public static final int[] BIT = {N, E, S, W};

    public final int width;
    public final int height;
    private final int[] open;
    private final int[] terrain;

    public Maze(int width, int height) {
        if (width < 2 || height < 2) throw new IllegalArgumentException("maze must be at least 2x2");
        this.width = width;
        this.height = height;
        this.open = new int[width * height];
        this.terrain = new int[width * height];
        java.util.Arrays.fill(terrain, 1);
    }

    public int index(int x, int y) { return y * width + x; }
    public boolean inBounds(int x, int y) { return x >= 0 && y >= 0 && x < width && y < height; }
    public int openMask(int x, int y) { return open[index(x, y)]; }
    public boolean isOpen(int x, int y, int dir) { return (open[index(x, y)] & BIT[dir]) != 0; }
    public int terrainCost(int x, int y) { return terrain[index(x, y)]; }
    public void setTerrain(int x, int y, int cost) { terrain[index(x, y)] = cost; }

    /** Removes the wall between (x,y) and its neighbour in direction dir. */
    public void carve(int x, int y, int dir) {
        int nx = x + DX[dir], ny = y + DY[dir];
        if (!inBounds(nx, ny)) throw new IllegalArgumentException("carving outside the maze");
        open[index(x, y)] |= BIT[dir];
        open[index(nx, ny)] |= BIT[(dir + 2) % 4];
    }

    /** Counts passages (each carved wall counted once). A perfect maze has cells-1 passages. */
    public int passageCount() {
        int c = 0;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                if (isOpen(x, y, 1)) c++;
                if (isOpen(x, y, 2)) c++;
            }
        return c;
    }

    /**
     * Knocks down extra walls so the maze has loops. A perfect maze has exactly one route between
     * two cells, so every solver would return the same path. Loops let cost-aware solvers
     * (Dijkstra, A*) find cheaper routes than hop-count solvers (BFS).
     */
    public void braid(double probability, Random rnd) {
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                for (int d = 1; d <= 2; d++) {
                    int nx = x + DX[d], ny = y + DY[d];
                    if (inBounds(nx, ny) && !isOpen(x, y, d) && rnd.nextDouble() < probability) carve(x, y, d);
                }
    }

    /** Scatters terrain costs 1..9 in smooth-ish blobs (mud) so that "shortest" and "cheapest" differ. */
    public void scatterTerrain(Random rnd) {
        int blobs = Math.max(3, (width * height) / 40);
        for (int b = 0; b < blobs; b++) {
            int cx = rnd.nextInt(width), cy = rnd.nextInt(height);
            int radius = 1 + rnd.nextInt(3);
            int cost = 3 + rnd.nextInt(7);
            for (int y = cy - radius; y <= cy + radius; y++)
                for (int x = cx - radius; x <= cx + radius; x++)
                    if (inBounds(x, y) && Math.abs(x - cx) + Math.abs(y - cy) <= radius) setTerrain(x, y, cost);
        }
        setTerrain(0, 0, 1);
        setTerrain(width - 1, height - 1, 1);
    }
}
