// Drives the compiled Java engine (mazeforge.jar) through CheerpJ library mode.
const $ = (id) => document.getElementById(id);
const NAMES = { bfs: "BFS", dfs: "DFS", dijkstra: "Dijkstra", astar: "A*" };
const GUARANTEE = {
  bfs: "fewest steps",
  dfs: "none (finds a path)",
  dijkstra: "lowest cost",
  astar: "lowest cost (admissible heuristic)",
};
let Api = null;
let maze = null;
let animToken = 0;

function params() {
  const [w, h] = $("size").value.split("x").map(Number);
  return { gen: $("gen").value, w, h, seed: Number($("seed").value) || 1, loops: Number($("loops").value) / 100 };
}

function writeHash() {
  const p = params();
  location.hash = `g=${p.gen}&s=${$("size").value}&seed=${p.seed}&l=${$("loops").value}&a=${$("solverA").value}&b=${$("solverB").value}`;
}
function readHash() {
  const q = new URLSearchParams(location.hash.slice(1));
  const set = (id, k) => { if (q.has(k)) { const el = $(id); const v = q.get(k); if ([...(el.options || [])].length === 0 || [...el.options].some(o => o.value === v)) el.value = v; } };
  set("gen", "g"); set("size", "s"); set("solverA", "a"); set("solverB", "b");
  if (q.has("seed")) $("seed").value = Math.min(999999, Math.max(1, Number(q.get("seed")) || 42));
  if (q.has("l")) $("loops").value = Math.min(30, Math.max(0, Number(q.get("l")) || 0));
  $("loopsOut").textContent = $("loops").value + "%";
}

function terrainColor(c) {
  if (c <= 1) return "#1c2638";
  const t = (c - 1) / 8; // 0..1
  const r = Math.round(60 + 80 * t), g = Math.round(50 + 25 * (1 - t)), b = Math.round(40 - 14 * t);
  return `rgb(${r},${g},${b})`;
}

function setup(canvas) {
  const cell = Math.max(10, Math.floor(560 / Math.max(maze.width, maze.height)));
  canvas.width = maze.width * cell + 2;
  canvas.height = maze.height * cell + 2;
  return cell;
}

function drawBase(canvas, cell) {
  const ctx = canvas.getContext("2d");
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  for (let y = 0; y < maze.height; y++)
    for (let x = 0; x < maze.width; x++) {
      ctx.fillStyle = terrainColor(maze.terrain[y * maze.width + x]);
      ctx.fillRect(1 + x * cell, 1 + y * cell, cell, cell);
    }
  ctx.fillStyle = "#34d399"; ctx.fillRect(1 + 2, 1 + 2, cell - 4, cell - 4);
  ctx.fillStyle = "#f43f5e"; ctx.fillRect(1 + (maze.width - 1) * cell + 2, 1 + (maze.height - 1) * cell + 2, cell - 4, cell - 4);
  drawWalls(ctx, cell);
}

function drawWalls(ctx, cell) {
  ctx.strokeStyle = "#e8ecf4"; ctx.lineWidth = 2; ctx.lineCap = "square";
  ctx.beginPath();
  for (let y = 0; y < maze.height; y++)
    for (let x = 0; x < maze.width; x++) {
      const m = maze.cells[y * maze.width + x];
      const px = 1 + x * cell, py = 1 + y * cell;
      if (!(m & 1)) { ctx.moveTo(px, py); ctx.lineTo(px + cell, py); }
      if (!(m & 8)) { ctx.moveTo(px, py); ctx.lineTo(px, py + cell); }
      if (y === maze.height - 1 && !(m & 4)) { ctx.moveTo(px, py + cell); ctx.lineTo(px + cell, py + cell); }
      if (x === maze.width - 1 && !(m & 2)) { ctx.moveTo(px + cell, py); ctx.lineTo(px + cell, py + cell); }
    }
  ctx.stroke();
}

function paintCell(canvas, cell, x, y, color) {
  const ctx = canvas.getContext("2d");
  ctx.fillStyle = color;
  ctx.globalAlpha = 0.38;
  ctx.fillRect(1 + x * cell + 2, 1 + y * cell + 2, cell - 4, cell - 4);
  ctx.globalAlpha = 1;
}

function drawPath(canvas, cell, flat) {
  const ctx = canvas.getContext("2d");
  ctx.strokeStyle = "#fde047"; ctx.lineWidth = Math.max(3, cell / 4); ctx.lineJoin = "round"; ctx.lineCap = "round";
  ctx.beginPath();
  for (let i = 0; i < flat.length; i += 2) {
    const px = 1 + flat[i] * cell + cell / 2, py = 1 + flat[i + 1] * cell + cell / 2;
    i === 0 ? ctx.moveTo(px, py) : ctx.lineTo(px, py);
  }
  ctx.stroke();
  ctx.fillStyle = "#34d399"; ctx.fillRect(1 + 2, 1 + 2, cell - 4, cell - 4);
  ctx.fillStyle = "#f43f5e"; ctx.fillRect(1 + (maze.width - 1) * cell + 2, 1 + (maze.height - 1) * cell + 2, cell - 4, cell - 4);
}

async function callJava(fn) {
  return JSON.parse(await fn());
}

async function generate() {
  const p = params();
  writeHash();
  animToken++;
  maze = await callJava(() => Api.generate(p.gen, p.w, p.h, p.seed, p.loops));
  for (const [id, cap] of [["boardA", "capA"], ["boardB", "capB"]]) {
    const c = $(id); const cell = setup(c); c.dataset.cell = cell; drawBase(c, cell);
  }
  $("capA").textContent = "Racer A: " + NAMES[$("solverA").value];
  $("capB").textContent = "Racer B: " + NAMES[$("solverB").value];
  $("stats").querySelector("tbody").innerHTML = "";
  $("insight").textContent = `${maze.width} x ${maze.height} maze, ${maze.passages} passages (${maze.width * maze.height - 1} would be loop-free).`;
  $("status").textContent = "Maze ready. Press Race.";
}

async function race() {
  const p = params();
  writeHash();
  const token = ++animToken;
  $("race").disabled = true;
  const ids = [["boardA", "solverA"], ["boardB", "solverB"]];
  const results = [];
  for (const [, sel] of ids) results.push(await callJava(() => Api.solve(p.gen, p.w, p.h, p.seed, p.loops, $(sel).value)));
  for (const [id] of ids) { const c = $(id); drawBase(c, Number(c.dataset.cell)); }
  $("capA").textContent = "Racer A: " + NAMES[results[0].solver];
  $("capB").textContent = "Racer B: " + NAMES[results[1].solver];
  $("status").textContent = "Racing: one expansion per tick for each racer...";
  const maxLen = Math.max(results[0].expandedOrder.length, results[1].expandedOrder.length) / 2;
  const perFrame = Math.max(1, Math.ceil(maxLen / 240));
  let step = 0;
  await new Promise((resolve) => {
    const frame = () => {
      if (token !== animToken) return resolve();
      for (let k = 0; k < perFrame; k++, step++) {
        results.forEach((r, i) => {
          const o = r.expandedOrder;
          if (step * 2 < o.length) { const c = $(ids[i][0]); paintCell(c, Number(c.dataset.cell), o[step * 2], o[step * 2 + 1], "#38bdf8"); }
        });
      }
      if (step < maxLen) requestAnimationFrame(frame); else resolve();
    };
    requestAnimationFrame(frame);
  });
  if (token !== animToken) return;
  results.forEach((r, i) => { const c = $(ids[i][0]); drawBase(c, Number(c.dataset.cell)); r.expandedOrder.forEach((v, j) => { if (j % 2 === 0) paintCell(c, Number(c.dataset.cell), v, r.expandedOrder[j + 1], "#38bdf8"); }); drawPath(c, Number(c.dataset.cell), r.path); });
  showStats(results);
  $("race").disabled = false;
  $("status").textContent = "Done. Change the seed, loops or racers and race again.";
}

function showStats(results) {
  const tbody = $("stats").querySelector("tbody");
  const minExp = Math.min(...results.map(r => r.expanded));
  const minLen = Math.min(...results.map(r => r.pathLength));
  const minCost = Math.min(...results.map(r => r.pathCost));
  tbody.innerHTML = "";
  for (const r of results) {
    const tr = document.createElement("tr");
    const cells = [
      [NAMES[r.solver], ""],
      [r.expanded / 1, r.expanded === minExp ? "best" : ""],
      [r.pathLength, r.pathLength === minLen ? "best" : ""],
      [r.pathCost, r.pathCost === minCost ? "best" : ""],
      [GUARANTEE[r.solver], ""],
    ];
    for (const [text, cls] of cells) { const td = document.createElement("td"); td.textContent = text; if (cls) td.className = cls; tr.appendChild(td); }
    tbody.appendChild(tr);
  }
  const [a, b] = results;
  let msg;
  if (a.pathCost === b.pathCost && a.pathLength === b.pathLength) msg = `Both found equally good routes. ${a.expanded < b.expanded ? NAMES[a.solver] : b.expanded < a.expanded ? NAMES[b.solver] : "Neither"} needed fewer expansions.`;
  else if (a.pathCost !== b.pathCost) { const w = a.pathCost < b.pathCost ? a : b, l = w === a ? b : a; msg = `${NAMES[w.solver]} found a route costing ${w.pathCost}, against ${l.pathCost} for ${NAMES[l.solver]}.`; }
  else msg = `Same cost, different route length (${a.pathLength} vs ${b.pathLength} steps).`;
  $("insight").textContent = msg + " Bold green numbers mark the best value in each column.";
}

async function boot() {
  readHash();
  $("loops").addEventListener("input", () => { $("loopsOut").textContent = $("loops").value + "%"; });
  try {
    await cheerpjInit({ version: 11 });
    const lib = await cheerpjRunLibrary("/app/mazeforge.jar");
    Api = await lib.mazeforge.MazeForgeApi;
  } catch (e) {
    $("status").textContent = "Could not start the Java runtime: " + e;
    window.__state = "error: " + e;
    return;
  }
  $("generate").onclick = generate;
  $("race").onclick = race;
  $("rand").onclick = () => { $("seed").value = 1 + Math.floor(Math.random() * 999999); generate(); };
  for (const id of ["gen", "size", "loops"]) $(id).addEventListener("change", generate);
  $("generate").disabled = false; $("race").disabled = false;
  await generate();
  window.__state = "ready";
  window.__race = race;
}
boot();
