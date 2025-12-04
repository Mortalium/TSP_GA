import java.util.*;

public class TSP_Comparison_System {

    // ==========================================
    // 1. CHALLENGE DEFINITION (The Fixed Maps)
    // ==========================================
    enum DifficultyLevel {
        TRIVIAL(5),     // 5 Cities (Instant)
        EASY(10),       // 10 Cities (Fast)
        MEDIUM(12),     // 12 Cities (Noticeable delay for BF)
        HARD(14),       // 14 Cities (BF will take minutes/hours, GA is instant)
        HARDER(15);
                

        final int cities;
        DifficultyLevel(int cities) {
            this.cities = cities;
        }
    }

    static class TSPMap {
        int[][] matrix;
        int numCities;
        String name;

        public TSPMap(DifficultyLevel level) {
            this.numCities = level.cities;
            this.name = level.name();
            this.matrix = new int[numCities][numCities];

            // We use a FIXED seed so the "Random" map is the same every time we run the program.
            Random fixedRand = new Random(12345);

            for (int i = 0; i < numCities; i++) {
                for (int j = 0; j < numCities; j++) {
                    if (i == j) {
                        this.matrix[i][j] = 0;
                    } else {
                        // Symmetric map for simplicity, but works for asymmetric too
                        int dist = fixedRand.nextInt(90) + 10; // Distances 10-100
                        this.matrix[i][j] = dist;
                        this.matrix[j][i] = dist;
                    }
                }
            }
        }
    }

    // ==========================================
    // 2. BRUTE FORCE SOLVER ( The "True Optimum")
    // ==========================================
    static class BruteForceSolver {
        private int minDistance = Integer.MAX_VALUE;
        private int[] bestPath;
        private int[][] matrix;
        private int n;

        public int solve(TSPMap map) {
            this.matrix = map.matrix;
            this.n = map.numCities;
            this.minDistance = Integer.MAX_VALUE;
            this.bestPath = new int[n + 1];

            // visited array
            boolean[] visited = new boolean[n];
            visited[0] = true; // Start at 0

            // Current path tracker
            ArrayList<Integer> currentPath = new ArrayList<>();
            currentPath.add(0);

            search(0, 1, 0, visited, currentPath);

            return minDistance;
        }

        // Recursive backtracking
        private void search(int currPos, int count, int cost, boolean[] visited, ArrayList<Integer> path) {
            // Pruning: If current cost is already worse than best found, stop (Branch & Bound lite)
            // Remove this 'if' to make it a "Dumb" Brute Force if you want it even slower.
            if (cost >= minDistance && minDistance != Integer.MAX_VALUE) {
                return;
            }

            // Base case: All cities visited, return to start
            if (count == n) {
                int returnCost = matrix[currPos][0];
                int totalCost = cost + returnCost;

                if (totalCost < minDistance) {
                    minDistance = totalCost;
                    // Save path if needed (omitted here for speed)
                }
                return;
            }

            // Iterate neighbors
            for (int i = 0; i < n; i++) {
                if (!visited[i] && matrix[currPos][i] > 0) {
                    visited[i] = true;
                    path.add(i);

                    search(i, count + 1, cost + matrix[currPos][i], visited, path);

                    // Backtrack
                    visited[i] = false;
                    path.removeLast();
                }
            }
        }
    }

    // ==========================================
    // 3. GA SOLVER (The Heuristic)
    // ==========================================
    static class GASolver {
        private final int POP_SIZE = 100;
        private final double MUTATION_RATE = 0.1;
        private final int MAX_GEN = 1500; // Fixed generations
        private int V;
        private int[][] matrix;

        class Individual implements Comparable<Individual> {
            int[] gnome;
            int fitness;

            Individual(int[] gnome, int fitness) {
                this.gnome = gnome;
                this.fitness = fitness;
            }
            @Override
            public int compareTo(Individual o) { return this.fitness - o.fitness; }
        }

        public int solve(TSPMap map) {
            this.V = map.numCities;
            this.matrix = map.matrix;

            // 1. Initialize
            List<Individual> pop = new ArrayList<>();
            for(int i=0; i<POP_SIZE; i++) {
                int[] g = createGnome();
                pop.add(new Individual(g, calFitness(g)));
            }

            // 2. Evolution Loop
            for(int gen=0; gen<MAX_GEN; gen++) {
                Collections.sort(pop);
                List<Individual> nextGen = new ArrayList<>();

                // Elitism: Keep best 10%
                int elitismOffset = POP_SIZE / 10;
                for(int i=0; i<elitismOffset; i++) nextGen.add(pop.get(i));

                // Breed rest
                while(nextGen.size() < POP_SIZE) {
                    Individual p1 = tournament(pop);
                    Individual p2 = tournament(pop);
                    int[] childGnome = crossover(p1.gnome, p2.gnome);
                    childGnome = mutate(childGnome);
                    nextGen.add(new Individual(childGnome, calFitness(childGnome)));
                }
                pop = nextGen;
            }

            Collections.sort(pop);
            return pop.get(0).fitness;
        }

        // --- Helpers ---
        int[] createGnome() {
            List<Integer> c = new ArrayList<>();
            for(int i=1; i<V; i++) c.add(i);
            Collections.shuffle(c);
            int[] g = new int[V+1];
            g[0]=0; g[V]=0;
            for(int i=0; i<c.size(); i++) g[i+1] = c.get(i);
            return g;
        }

        int calFitness(int[] g) {
            int f=0;
            for(int i=0; i<V; i++) f += matrix[g[i]][g[i+1]];
            return f;
        }

        Individual tournament(List<Individual> pop) {
            int idx = (int)(Math.random()*pop.size());
            Individual best = pop.get(idx);
            for(int i=0; i<4; i++) { // Tournament size 5
                Individual ind = pop.get((int)(Math.random()*pop.size()));
                if(ind.fitness < best.fitness) best = ind;
            }
            return best;
        }

        int[] crossover(int[] p1, int[] p2) {
            // Ordered Crossover (OX1)
            int[] child = new int[V+1];
            Arrays.fill(child, -1);
            child[0]=0; child[V]=0;

            int s = 1 + (int)(Math.random()*(V-1));
            int e = 1 + (int)(Math.random()*(V-1));
            if(s>e) { int t=s; s=e; e=t; }

            for(int i=s; i<=e; i++) child[i] = p1[i];

            int cur = 1;
            for(int i=1; i<V; i++) {
                int gene = p2[i];
                boolean in = false;
                for(int k=s; k<=e; k++) if(child[k]==gene) in=true;
                if(!in) {
                    while(child[cur] != -1) cur++;
                    child[cur] = gene;
                }
            }
            return child;
        }

        int[] mutate(int[] g) {
            if(Math.random() < MUTATION_RATE) {
                int i = 1 + (int)(Math.random()*(V-1));
                int j = 1 + (int)(Math.random()*(V-1));
                int t = g[i]; g[i] = g[j]; g[j] = t;
            }
            return g;
        }
    }

    // ==========================================
    // 4. MAIN ORCHESTRATOR
    // ==========================================
    public static void main(String[] args) {
        System.out.println("--- TSP: Brute Force vs Genetic Algorithm ---");
        System.out.printf("%-10s | %-12s | %-12s | %-10s | %-10s | %-10s%n",
                "Level", "Cities", "True Opt", "GA Best", "Accuracy", "Speedup");
        System.out.println("---------------------------------------------------------------------------------");

        BruteForceSolver bf = new BruteForceSolver();
        GASolver ga = new GASolver();
        
        for (DifficultyLevel level : DifficultyLevel.values()) {
            TSPMap map = new TSPMap(level);

            // 1. Run Brute Force
            long startBF = System.nanoTime();
            int trueOptimum = bf.solve(map);
            long endBF = System.nanoTime();
            double timeBF = (endBF - startBF) / 1_000_000.0; // ms

            // 2. Run Genetic Algorithm
            long startGA = System.nanoTime();
            int gaBest = ga.solve(map);
            long endGA = System.nanoTime();
            double timeGA = (endGA - startGA) / 1_000_000.0; // ms

            // 3. Compare
            double accuracy = ((double) trueOptimum / gaBest) * 100.0;
            double speedup = timeBF / timeGA;

            // Output Row
            System.out.printf("%-10s | %-12d | %-12d | %-10d | %-9.2f%% | %.1fx%n",
                    level.name(),
                    level.cities,
                    trueOptimum,
                    gaBest,
                    accuracy,
                    speedup);

            // Force output flush
            System.out.flush();
        }
    }
}