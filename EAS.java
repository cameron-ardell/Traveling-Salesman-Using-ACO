/*
 * Creates an instance of the EAS algorithm. Calls on AntEAS to generate ants
 * that follow the rules of EAS. Takes into the constructor all parameters 
 * needed for this problem type.
 */
package aco;

import java.io.*;

/**
 *
 * @author PryhuberA
 */
public class EAS {

    private final int numAnts;
    private final int numIter;
    private final double pheroWeight;
    private final double heuristicWeight;
    private final double evapFactor;
    private final double elitismFactor;
    private final double percentOfOpt;
    private static final double INIT_PHERO = 1.0;
    private final double optLen;

    private Edge[] bestTour;
    private double bestTourLength = Double.MAX_VALUE;
    private int numCities;
    private AntEAS[] ants;
    private City[] cities;
    private static Edge[][] edgeMatrix;
    

    EAS(int numAnts, int numIter, double alpha, double beta, double rho,
            double elitism, String file, double optLength, double percent) {

        this.numAnts = numAnts;
        this.numIter = numIter;
        //the degree of influence of the pheromone component
        this.pheroWeight = alpha;
        //the degree of influence of the heurisitic component
        this.heuristicWeight = beta;
        //the pheromone evaporation factor
        this.evapFactor = rho;
        this.elitismFactor = elitism;
        this.percentOfOpt = percent;
        this.optLen = optLength;

        //housekeeping to read in file being tested and create the edge matrix
        readFile(file);
    
        createEdges();
        

        //timer
        long startTime = System.nanoTime();
        //for as many iterations as specified in the constructor
        for (int iter = 0; iter < numIter; iter++) {
            //generate new ants every iteration
            generateAnts();

            //walks ants to judge them for their tours (not their personality)
            for (int i = 0; i < numAnts; i++) {

                //updates best tour 
                if (ants[i].getTourLength() < bestTourLength) {
                    bestTourLength = ants[i].getTourLength();
                    bestTour = ants[i].getTour();
                }
            }

            //the odors are dead.
            evapPheromone();

            //long live the new odors!
            //**applause**
            layDownPhero();

            //put down the extra potent ones
            layElitePhero(elitismFactor);

            //if by some miracle we get the perfect solution (or any percentage of the optimal
            //set in ACO) we will return early
            if(bestTourLength/optLen <= percentOfOpt){
                System.out.println("Found optimal solution early on the " + iter + "th iteration");
                return;
            }
            long timeElapsed = System.nanoTime() - startTime;
        //if the time elapsed is longer than five minutes, quit out
        if(((double)timeElapsed/1000000000)> 300){
            return;
        }
        }

 

    }
    


    /**
     * Read in data about all cities from the file.
     */
    public void readFile(String fileName) {
        int numCities = 0;
        try {
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            String buff;

            do {
                //read next line and tokenize it
                buff = file.readLine();
                String stringTemp = buff.trim();
                String[] result = stringTemp.split("\\s+");
                //for the third file String[] result = buff.split("    ");

                //set the number of cities
                if (result[0].equals("DIMENSION")) {
//                    System.out.println("setting numCities");
                    numCities = Integer.parseInt(result[2]);
                }

            } while (!buff.equals("NODE_COORD_SECTION"));


            //set length of City array
            this.numCities = numCities;
            cities = new City[numCities];

            //put all info into in an array of "City" of length numCities
            //City class will contain x and y coordinates of each city
            //set coordinates of all cities
            for (int i = 0; i < cities.length; i++) {
                buff = file.readLine();
                String stringTemp = buff.trim();
                String[] tokens = stringTemp.split("\\s+");
                cities[i] = new City(Integer.parseInt(tokens[0]), Double.parseDouble(tokens[1]),
                        Double.parseDouble(tokens[2]));
            }

        } catch (Exception e) {
            System.out.println("Error while reading file: " + e.getMessage());
        }

    }

    /**
     * Initialize matrix of edges
     */
    public void createEdges() {
        edgeMatrix = new Edge[numCities][numCities];

        //initialize edgeMatrix
        for (int source = 0; source < numCities; source++) {
            for (int dest = 0; dest < numCities; dest++) {
                edgeMatrix[source][dest] = new Edge(cities[source], cities[dest]);

                // arbitrarily set initial pheromone level on all edges to be the same
                edgeMatrix[source][dest].addPheromone(INIT_PHERO);

            }
        }
    }

    /**
     * creates numAnts and runs tours for each
     */
    public void generateAnts() {

        ants = new AntEAS[numAnts];

        int startCity = 0;

        for (int index = 0; index < numAnts; index++) {

            //if there are more ants than cities then start looping through the 
            //cities again until we exhaust the numAnts
            if (startCity >= numCities) {
                startCity = startCity % numCities;
            }

            ants[index] = new AntEAS(startCity, numCities, pheroWeight, heuristicWeight);

            startCity++;

        }

    }

    //makes the pheromone on all the edges evaporate by a predetermined factor
    public void evapPheromone() {
        //walks through edge matrix
        for (int source = 0; source < numCities; source++) {
            for (int dest = 0; dest < numCities; dest++) {
                edgeMatrix[source][dest].evapPheromone(evapFactor);
            }
        }
    }

    /**
     * goes through by tour, then by edge for each specific tour, modifying
     * their pheromone levels
     */
    public void layDownPhero() {
        for (int ant = 0; ant < numAnts; ant++) {
            Edge[] localTour = ants[ant].getTour();
            for (int edge = 0; edge < numCities; edge++) {
                int cityA = localTour[edge].getSource();
                int cityB = localTour[edge].getDest();

                edgeMatrix[cityA][cityB].addPheromone(1 / ants[ant].getTourLength());
                edgeMatrix[cityB][cityA].addPheromone(1 / ants[ant].getTourLength());

            }

        }

    }

    /**
     * goes through each edge of the best tour so far and updates those edges
     * with more pheromone
     */
    public void layElitePhero(double eliteFact) {
        for (int edge = 0; edge < numCities; edge++) {
            int cityA = bestTour[edge].getSource();
            int cityB = bestTour[edge].getDest();

            edgeMatrix[cityA][cityB].addPheromone(eliteFact / bestTourLength);
            edgeMatrix[cityB][cityA].addPheromone(eliteFact / bestTourLength);

        }
    }

    // //returns edge object that represents the source to the destination
    public static Edge getEdge(int source, int dest) {
        return edgeMatrix[source][dest];
    }

    //returns the pheromone level along a path
    public static double getPheroLevel(int i, int j) {
        return edgeMatrix[i][j].getPheromone();
    }

    //returns the distance from source to destination
    public static double getEdgeLength(int i, int j) {
        return edgeMatrix[i][j].getLength();
    }

    //useful for debugging and for kicks
    public void printTour(Edge[] tour) {
        for (int i = 0; i < tour.length; i++) {
            System.out.println("Edge from  " + tour[i].getSource() + " to " + tour[i].getDest());
        }
    }
    
    //pretty self explanatory (again)
    public double getBestTourLength(){
        return bestTourLength;
    }

}
