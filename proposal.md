# Resilience of Transportation Networks Under Failure Conditions

**Course:** CSIS 385 – Design and Analysis of Algorithms, Spring 2026

**Team Members:** Oliwia Majtyka, Sean Donnelly

**Submission Date:** April 1, 2026


---

## Project Overview

Modern transportation infrastructure is inherently vulnerable. Road closures, accidents,
and construction disruptions routinely alter the navigability of road networks in ways
that standard routing algorithms are not designed to handle — most shortest-path
algorithms, including Dijkstra's and A*, operate under the assumption of a fully
functional graph. This project investigates what happens when that assumption breaks
down.

Using the METAL road graph dataset, we will simulate progressive edge failures and
measure their cascading effects on routing efficiency, path length, and network
connectivity. Our central questions are: which roads are most critical to network
function, how gracefully do routing algorithms degrade under disruption, and what
does this reveal about the structural vulnerabilities of real transportation systems?

---

## Algorithms and Methods

### 1. Dijkstra's Algorithm (Baseline)
We will implement Dijkstra's algorithm as our baseline shortest-path router on the
intact METAL graph. This establishes reference path lengths and travel costs against
which all post-failure results will be compared.

### 2. A* Search
We will implement A* with a geographic (straight-line distance) heuristic as a
more efficient alternative to Dijkstra's. A key aspect of our analysis will be
evaluating whether A*'s heuristic guidance remains effective — or becomes
misleading — as edges are removed and the graph structure degrades.

### 3. Edge Criticality Analysis
Beyond routing, we will develop a systematic method for identifying the most
critical edges in the network. By iteratively removing edges and measuring the
resulting increase in shortest path lengths (or loss of connectivity between
node pairs), we will rank roads by their structural importance. This analysis
draws on concepts from betweenness centrality and network flow theory.

---

## Dataset

Our primary dataset is the **METAL road graph**, which models real road networks
as weighted graphs suitable for shortest-path analysis. Where relevant, we will
supplement this with **Travel Mapping** data to reflect realistic highway structures
and incorporate information about high-traffic corridors — allowing us to weight our
failure simulations toward roads with the greatest real-world significance.

---

## Milestones and Schedule

| Milestone | Description | Target |
|---|---|---|
| 1 | Parse METAL graph; implement and validate Dijkstra baseline | April 10 |
| 2 | Implement A* with geographic heuristic; benchmark against Dijkstra | April 17 |
| 3 | Build edge-removal simulation framework; measure routing changes | April 24 |
| 4 | Perform full criticality analysis; identify highest-impact roads | April 28 |
| 5 | Finalize presentation materials and written report | May 1–4 |

**Critical milestones:** Milestones 3 and 4 — the failure simulation and criticality
analysis — constitute the scientific core of this project. Milestones 1 and 2 are
essential prerequisites, while Milestone 5 is required for a complete submission.

---
## Progress Update April 10, 2026

We started by parsing two different METAL road graphs, Siena 2 mile radius, and Siena 10 mile radius. 
We implemented Dijkstra's algorithm to see how it works with different sized graphs with a fully working network.

**2 Mile Radius:**

This dataset had a total of 8 vertices and 6 edges. We found that from vertex 1 to vertex 7, it found the shortest
path to have a distance of 3.730 using 3 hops. The shortest was from vertexes, (1,3,4,7). 

**10 Mile Radius**

This dataset had a total of 196 vertices and 240 edges. We found that from vertex 1 to vertex 195, it found 
the shortest path to have a distance of 16.595 using 15 hops. The shortest was from vertexes,
(1,41,43,70,76,77,119,118,130,153,137,151,150,152,163,195).

**Road Failure Simulations**

For our next progress update, we plan to start using other algorithms to see what happens when a road closes.
We plan to see the paths get longer with more edges and vertices visited. We also want to see how big the effect is depending on which
edge is removed. We also may change from a 2 mile radius to something bigger such as 3 miles to get a better idea of how the algorithm looks
at the vertexes and edges.

## Feasibility

Both Dijkstra's and A* are well-understood algorithms with clear implementation
paths. The METAL dataset is publicly available and structured for graph traversal.
The primary engineering challenge will be the computational cost of re-running
routing queries after each simulated edge removal. We plan to address this by
initially restricting our analysis to a representative regional subgraph before
scaling to the full dataset, and by batching removal experiments efficiently.

We are confident this project is achievable within the available timeframe and
that it will yield meaningful, demonstrable results suitable for presentation at
the Academic Showcase.
