import java.util.*;

public class TSP {
    // Constants and global variables
    static int V = 5;
    static int INT_MAX = 2147483647;
    static int POP_SIZE = 10;
    static int gen_thres = 5;
    static double temperature = 10000;

    // Structure of a GNOME
    static class Individual {
        String gnome;
        int fitness;

        public Individual(String gnome, int fitness) {
            this.gnome = gnome;
            this.fitness = fitness;
        }
    }

    // Function to return a random number from start and end
    static int rand_num(int start, int end) {
        return (int) (Math.random() * (end - start)) + start;
    }

    // Function to check if the character has already occurred in the string
    static boolean repeat(String s, char ch) {
        return s.indexOf(ch) >= 0;
    }

    // Function to return a mutated GNOME
    static String mutatedGene(String gnome) {
        while (true) {
            int r = rand_num(1, V);
            int r1 = rand_num(1, V);
            if (r1 != r) {
                char[] gnomeArr = gnome.toCharArray();
                char temp = gnomeArr[r];
                gnomeArr[r] = gnomeArr[r1];
                gnomeArr[r1] = temp;
                return new String(gnomeArr);
            }
        }
    }

    // Function to return a valid GNOME string
    static String create_gnome() {
        String gnome = "0";
        while (true) {
            if (gnome.length() == V) {
                gnome += gnome.charAt(0);
                break;
            }
            int temp = rand_num(1, V);
            if (!repeat(gnome, (char) (temp + 48))) {
                gnome += (char) (temp + 48);
            }
        }
        return gnome;
    }

    // Function to return the fitness value of a gnome
    static int cal_fitness(String gnome) {
        int[][] mp = {
            {0, 2, INT_MAX, 12, 5},
            {2, 0, 4, 8, INT_MAX},
            {INT_MAX, 4, 0, 3, 3},
            {12, 8, 3, 0, 10},
            {5, INT_MAX, 3, 10, 0},
        };
        int f = 0;
        for (int i = 0; i < gnome.length() - 1; i++) {
            if (mp[gnome.charAt(i) - 48][gnome.charAt(i + 1) - 48] == INT_MAX) {
                return INT_MAX;
            }
            f += mp[gnome.charAt(i) - 48][gnome.charAt(i + 1) - 48];
        }
        return f;
    }

    // Function to return the updated value of the cooling element
    static double cooldown(double temp) {
        return (90 * temp) / 100;
    }

    public static void main(String[] args) {
        // Initial population
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < POP_SIZE; i++) {
            String gnome = create_gnome();
            int fitness = cal_fitness(gnome);
            population.add(new Individual(gnome, fitness));
        }

        // Iteration to perform population crossing and gene mutation
        int gen = 1;
        while (temperature > 1000 && gen <= gen_thres) {
            Collections.sort(population, Comparator.comparingInt(a -> a.fitness));
            List<Individual> new_population = new ArrayList<>();
            for (Individual p1 : population) {
                while (true) {
                    String new_g = mutatedGene(p1.gnome);
                    int new_fitness = cal_fitness(new_g);
                    Individual new_gnome = new Individual(new_g, new_fitness);
                    if (new_gnome.fitness <= p1.fitness) {
                        new_population.add(new_gnome);
                        break;
                    } else {
                        double prob = Math.pow(2.7, -1 * ((double) (new_gnome.fitness - p1.fitness) / temperature));
                        if (prob > 0.5) {
                            new_population.add(new_gnome);
                            break;
                        }
                    }
                }
            }
            temperature = cooldown(temperature);
            population = new_population;
            System.out.println("Generation " + gen);
            for (Individual ind : population) {
                System.out.println(ind.gnome + " " + ind.fitness);
            }
            gen++;
        }
    }
}