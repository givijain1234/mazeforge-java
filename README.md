# MazeForge

A maze race lab. Generate a maze, pick two algorithms and watch them race to the exit. The generators and solvers are written in **Java** and run **in the browser** through [CheerpJ](https://cheerpj.com/), so the live demo executes the compiled Java bytecode, not a JavaScript copy.

**Live demo:** _(link added after deploy)_

## What it shows

| Area | Where |
| --- | --- |
| Maze generation: recursive backtracker (explicit stack), randomized Prim, randomized Kruskal with a union-find | `RecursiveBacktracker`, `PrimGenerator`, `KruskalGenerator`, `UnionFind` |
| Path finding: BFS, DFS, Dijkstra, A* | `BfsSolver`, `DfsSolver`, `DijkstraSolver`, `AStarSolver`, `PriorityQueueSearch` |
| Strategy pattern behind two small interfaces | `MazeGenerator`, `MazeSolver` |
| Collections: `ArrayDeque`, `PriorityQueue`, arrays, `TreeMap` registries | throughout |
| Deterministic output: the same seed always rebuilds the same maze | `MazeForgeApi.build` |
| JUnit 5 tests (10 tests) | `src/test/java/mazeforge/MazeForgeTest.java` |

## How the race works

- Each racer reports **cells expanded**, **path steps** and **path cost**. These are counts produced by the Java code. The page never claims one algorithm is faster from animation time, because browser animation speed says nothing about algorithm speed.
- Both boards replay one expansion per tick, so they advance in lockstep. Only the counts are compared, never the animation time.
- Cells carry a **terrain cost from 1 to 9** (the brown patches). BFS minimises steps and ignores cost. Dijkstra and A* minimise total cost.
- A perfect maze (every cell connected, no loops) has exactly one route between two cells, so every solver would return the same path. The **Loops** slider knocks down extra walls, giving several routes so the algorithms can disagree.
- A* uses Manhattan distance as its heuristic. The cheapest cell costs 1, so the heuristic never overestimates and A* returns the same cost as Dijkstra. The tests check this on 40 seeds.

"Path steps" counts moves between cells (a path through N cells has N-1 steps). Example from the command line for one seed (42, 20 x 12 maze), not a general benchmark:

```
solver     expanded  pathLength  pathCost
astar           122          50        59
bfs             234          42       139
dfs             217          50       139
dijkstra        177          50        59
```

BFS finds the route with the fewest steps (42) but it crosses a lot of mud (cost 139). Dijkstra and A* take 8 more steps and pay 59.

## Repository layout

```
src/main/java/mazeforge/   the Java engine
src/test/java/mazeforge/   JUnit 5 tests
web/                       static site: index.html, style.css, app.js, mazeforge.jar
pom.xml                    Maven project (JUnit 5), used by IntelliJ and `mvn test`
build.sh                   compile, test, rebuild web/mazeforge.jar without Maven
netlify.toml               publishes web/
```

## Run it locally

Requirements: JDK 11 or newer (tested with JDK 17). Run every command from the repository root.

**IntelliJ IDEA:** choose File > Open and select `pom.xml` (or the project folder, then "Open as Project" if asked). IntelliJ imports it as a Maven project and downloads JUnit 5 by itself. If you opened the folder earlier and see "package org.junit.jupiter.api does not exist", close the project, delete the `.idea` folder and any `*.iml` file, then open `pom.xml` again (or right-click `pom.xml` > Add as Maven Project). Run the tests by right-clicking `MazeForgeTest` > Run, or the console demo by right-clicking `MazeForgeApi` > Run.

**Command line with Maven:**

```bash
mvn test                   # compiles and runs the 10 JUnit 5 tests
```

**Command line without Maven** (needs curl, which downloads the JUnit console jar once):

```bash
./build.sh                 # compiles the sources, runs the JUnit tests, then rebuilds web/mazeforge.jar
java -cp build/main mazeforge.MazeForgeApi backtracker 42   # console demo
```

`web/mazeforge.jar` is what the website runs. `./build.sh` is the step that refreshes it after you change the Java code.

**Preview the website:** `npx http-server web -p 8080` (needs Node.js), then open <http://localhost:8080>. `python3 -m http.server` does not support range requests, which CheerpJ needs, so use `http-server` or similar.

## Deploy on Netlify

The `web/` folder is a static site and `netlify.toml` publishes it. No build step is needed because `web/mazeforge.jar` is committed. Run `./build.sh` and commit the new jar after any change to the Java sources, otherwise the live site keeps running the old code. Netlify does not run Java servers, which is why the Java runs in the visitor's browser.

## Limits

- The demo needs the CheerpJ runtime, loaded from `cjrtnc.leaningtech.com`. If that CDN is unreachable the page shows an error and nothing runs. The Java sources and console demo still work.
- First load downloads the runtime, so it takes a few seconds.
- Start is always the top-left cell and the goal the bottom-right cell.
- Only the Java engine is Java. The page around it (HTML, CSS and a small `app.js` for drawing) is plain JavaScript.

## Credits and references

- CheerpJ by Leaning Technologies runs the Java bytecode in the browser: <https://cheerpj.com/> (free under the [CheerpJ Community License](https://cheerpj.com/docs/licensing.html), loaded from their CDN).
- Jamis Buck, maze algorithm write-ups (backtracker, Prim, Kruskal): <https://weblog.jamisbuck.org/2011/2/7/maze-generation-algorithm-recap>
- P. Hart, N. Nilsson, B. Raphael, "A Formal Basis for the Heuristic Determination of Minimum Cost Paths", 1968 (A*).
- E. W. Dijkstra, "A note on two problems in connexion with graphs", 1959.
- T. Cormen et al., *Introduction to Algorithms* (union-find, BFS, DFS, Dijkstra).

## License

MIT. See `LICENSE`.

## Before you publish

- In `web/index.html`, change the footer link `id="repo"` (currently `https://github.com/`) to your repository URL.
- In this README, replace the "Live demo" placeholder with your Netlify URL once deployed.
# mazeforge-java
