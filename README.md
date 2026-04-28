# AcademicShowcaseDonnellyMajtyka
============================================
 
FILES
-----
src/Vertex.java          - Road intersection node (label, lat, lon, haversine distance)
src/Edge.java            - Road segment (endpoints, road name, weight in miles)
src/Graph.java           - Graph with TMG parser and edge removal support
src/PathResult.java      - Result object returned by routing algorithms
src/Dijkstra.java        - Dijkstra's algorithm (baseline shortest path)
src/AStar.java           - A* with geographic (haversine) heuristic
src/FailureSimulation.java - Progressive edge removal simulation
src/CriticalityAnalysis.java - Ranks all edges by structural importance
src/Main.java            - Runs all four analyses, prints formatted report
 
siena2mi.tmg             - 2-mile radius METAL graph (8 vertices, 6 edges)
siena10mi.tmg            - 10-mile radius METAL graph (196 vertices, 240 edges)
 
COMPILE & RUN
-------------
  mkdir out
  javac -d out src/*.java
  java -cp out Main siena2mi.tmg siena10mi.tmg
 
Or just one graph:
  java -cp out Main siena10mi.tmg
 
KEY RESULTS (10-mile graph)
---------------------------
  Dijkstra: 18.074 mi, 19 hops, 172 nodes visited
  A*:       18.074 mi, 19 hops,  57 nodes visited  (66.9% efficiency gain)
 
  Most critical road: NY40 corridor (disconnects 231+ vertex pairs if removed)
  14 bridge edges identified — all cause network disconnection