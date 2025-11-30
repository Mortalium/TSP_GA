import java.util.*;

public class TSP_GA {

    // --- Configuration ---
    static int V = 5;
    static int POP_SIZE = 10;
    static int MAX_GEN = 50;
    static double MUTATION_RATE = 0.1; // 10% chance to mutate

    // The Map (Graph)
    static int INT_MAX = Integer.MAX_VALUE;
    static int[][] mp = {
            {0, 2, INT_MAX, 12, 5},
            {2, 0, 4, 8, INT_MAX},
            {INT_MAX, 4, 0, 3, 3},
            {12, 8, 3, 0, 10},
            {5, INT_MAX, 3, 10, 0},
    };

    // --- Structure of an Individual ---
    static class Individual implements Comparable<Individual> {
        int[] gnome; // Changed from String to int[] for logic handling
        int fitness;

        public Individual(int[] gnome, int fitness) {
            this.gnome = gnome;
            this.fitness = fitness;
        }

        @Override
        public int compareTo(Individual other) {
            return this.fitness - other.fitness;
        }
    }

    // --- 1. Initialization Function ---
    // Creates a random valid path: 0 -> {random perm} -> 0
    static int[] create_gnome() {
        List<Integer> cities = new ArrayList<>();
        for (int i = 1; i < V; i++) cities.add(i); // Add cities 1, 2, 3, 4...
        Collections.shuffle(cities);

        int[] gnome = new int[V + 1];
        gnome[0] = 0; // Start
        for (int i = 0; i < cities.size(); i++) {
            gnome[i + 1] = cities.get(i);
        }
        gnome[V] = 0; // End
        return gnome;
    }

    // --- 2. Fitness Function ---
    static int cal_fitness(int[] gnome) {
        int f = 0;
        for (int i = 0; i < gnome.length - 1; i++) {
            int u = gnome[i];
            int v = gnome[i + 1];
            if (mp[u][v] == INT_MAX) return INT_MAX;
            f += mp[u][v];
        }
        return f;
    }

    // --- 3. Selection Function (Tournament Selection) ---
    // Picks a small random group and selects the best one to be a parent
    static Individual selection(List<Individual> pop) {
        int tournamentSize = 3;
        Individual best = null;
        for (int i = 0; i < tournamentSize; i++) {
            int randIdx = (int) (Math.random() * pop.size());
            Individual ind = pop.get(randIdx);
            if (best == null || ind.fitness < best.fitness) {
                best = ind;
            }
        }
        return best;
    }

    // --- 4. Crossover Function (Ordered Crossover) ---
    // Essential for TSP to avoid duplicate cities in a path
    static int[] crossover(int[] parent1, int[] parent2) {
        int[] child = new int[V + 1];
        Arrays.fill(child, -1);
        child[0] = 0;
        child[V] = 0;

        // 1. Take a subset from Parent 1
        int startPos = 1;
        int endPos = 1 + (int) (Math.random() * (V - 1));

        for (int i = startPos; i < endPos; i++) {
            child[i] = parent1[i];
        }

        // 2. Fill the rest from Parent 2 (if not already in child)
        int currentPos = 1;
        for (int i = 1; i < V; i++) {
            int city = parent2[i];

            // Check if city exists in the subset we took from P1
            boolean alreadyInChild = false;
            for (int k = startPos; k < endPos; k++) {
                if (child[k] == city) {
                    alreadyInChild = true;
                    break;
                }
            }

            // If not in child, find the next empty spot
            if (!alreadyInChild) {
                while (child[currentPos] != -1) {
                    currentPos++;
                }
                child[currentPos] = city;
            }
        }
        return child;
    }

    // --- 5. Mutation Function ---
    // Swaps two cities randomly
    static int[] mutation(int[] gnome) {
        int[] mutated = gnome.clone();
        if (Math.random() < MUTATION_RATE) {
            int r1 = 1 + (int) (Math.random() * (V - 1));
            int r2 = 1 + (int) (Math.random() * (V - 1));
            // Swap
            int temp = mutated[r1];
            mutated[r1] = mutated[r2];
            mutated[r2] = temp;
        }
        return mutated;
    }

    public static void main(String[] args) {
        // Initial population
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < POP_SIZE; i++) {
            int[] gnome = create_gnome();
            population.add(new Individual(gnome, cal_fitness(gnome)));
        }

        int gen = 1;
        while (gen <= MAX_GEN) {
            // Sort to always keep the best (Elitism)
            Collections.sort(population);

            List<Individual> new_population = new ArrayList<>();

            // ELITISM: Carry over the single best individual directly
            new_population.add(population.get(0));

            // Generate the rest of the new population
            while (new_population.size() < POP_SIZE) {
                // A. Selection
                Individual p1 = selection(population);
                Individual p2 = selection(population);

                // B. Crossover
                int[] childGnome = crossover(p1.gnome, p2.gnome);

                // C. Mutation
                childGnome = mutation(childGnome);

                // Add to new population
                new_population.add(new Individual(childGnome, cal_fitness(childGnome)));
            }

            population = new_population;

            // Print Best of Generation
            System.out.println("Generation " + gen + " Best Fitness: " + population.get(0).fitness +
                    " Path: " + Arrays.toString(population.get(0).gnome));
            gen++;
        }
    }
}