/*
 * Creates an instance of the ACS algorithm. Calls on AntACS to generate ants
 * that follow the rules of ACS. Takes into the constructor all parameters 
 * needed for this problem type.
 */
package aco;

import java.io.*;
import java.util.*;

/**
 *
 * @author PryhuberA
 */
public class ACS {

    private final int numAnts;
    private final int numIter;
    private final double pheroWeight;
    private final double heuristicWeight;
    private final double evapFactor;
    private final double wearFactor;
    private final double q0;
    private final double percentOfOpt;
    private final double optLength;

    private Edge[] bestTour;
    private double bestTourLength = Double.MAX_VALUE;
    private int numCities;
    private AntACS[] ants;
    private City[] cities;
    private static Edge[][] edgeMatrix;
    private double t0;

    
    //constructor for ACS
    ACS(int numAnts, int numIter, double alpha, double beta, double rho,
            double q0, double epsilon, double optLength, double percentage, String file) {

        this.numAnts = numAnts;
        this.numIter = numIter;
        //the degree of influence of the pheromone component
        this.pheroWeight = alpha;
        //the degree of influence of the heurisitic component
        this.heuristicWeight = beta;
        //the pheromone evaporation factor
        this.evapFactor = rho;
        //the probability that an ant will choose the best leg for the next leg of the tour
        this.q0 = q0;
        //epsilons control the "wearing away" of pheromones
        this.wearFactor = epsilon;
        this.percentOfOpt = percentage;
        this.optLength = optLength;

        //housekeeping to read in initial file and create the edge matrix for all cities
        readFile(file);

        createEdges();
        
        //timer 
        long startTime = System.nanoTime();
        for (int iter = 0; iter < numIter; iter++) {
            
            //create a new set of ants ("the colony") on every iteration
            generateAnts();

            //progressively add edges and wear away pheromone after each ant has
            //simultaneously added this edge (for all but last edge)
            for (int edge = 0; edge < numCities - 1; edge++) {

                //goes through ant by ant, updating one edge to ant's tour
                for (int antIndex = 0; antIndex < numAnts; antIndex++) {
                    ants[antIndex].addEdge();
                }

                //removes pheromone on edges ants have now walked across
                removePheroOnTheGo();
            }

            //makes sure ants go home (they're still drunk)
            for (int antIndex = 0; antIndex < numAnts; antIndex++) {
                ants[antIndex].finalEdge();
            }

            //removes pheromone on final edge 
            removePheroOnTheGo();

            //updates best tour 
            for (int i = 0; i < numAnts; i++) {
                if (ants[i].getTourLength() < bestTourLength) {
                    bestTourLength = ants[i].getTourLength();
                    bestTour = ants[i].getTour();
                }
            }

            //evaporates pheromone levels on each edge
            evapPheromone();

            //adds pheromone to edges on path of the best tour so far
            for (int tour = 0; tour < bestTour.length; tour++) {
                int source = bestTour[tour].getSource();
                int dest = bestTour[tour].getDest();

                double length = bestTour[tour].getLength();

                edgeMatrix[source][dest].addPheromone(evapFactor / length);
                edgeMatrix[dest][source].addPheromone(evapFactor / length);

            }

            //if by some miracle we get the perfect solution (or any percentage of the optimal
            //set in ACO) we will return early
            if(bestTourLength/optLength <= percentOfOpt){
                return;
            }
            long timeElapsed = System.nanoTime() - startTime;
        //if the time elapsed is longer than five minutes, quit out
        if(((double)timeElapsed/1000000000)> 300){
            return;
        } 
            
        }

        
    }

    //takes off pheromone after ants have walked across an edge
    public void removePheroOnTheGo() {
        for (int antIndex = 0; antIndex < numAnts; antIndex++) {
            int currCity = ants[antIndex].getCurrCity();
            int prevCity = ants[antIndex].getPrevCity();
            edgeMatrix[currCity][prevCity].wearPheromone(wearFactor, t0);
            edgeMatrix[prevCity][currCity].wearPheromone(wearFactor, t0);
        }
    }

    //used in debugging, but prints tour in question
    public void printTour(Edge[] tour) {
        for (int i = 0; i < tour.length; i++) {
            System.out.println("Edge from  " + tour[i].getSource() + " to " + tour[i].getDest());
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

                //set the number of cities
                if (result[0].equals("DIMENSION")) {
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

    //Initialize matrix of edges
    public void createEdges() {
        edgeMatrix = new Edge[numCities][numCities];
        
        //initialize edgeMatrix
        for (int source = 0; source < numCities; source++) {
            for (int dest = 0; dest < numCities; dest++) {
                edgeMatrix[source][dest] = new Edge(cities[source], cities[dest]);
                
            }
        }
        
        //run a nearest neighbor tour so we can calculate t0 before laying down initial pheromone
        greedyTour();
        
        // set initial pheromone level on all edges to be the same constant
        for (int source = 0; source < numCities; source++) {
            for (int dest = 0; dest < numCities; dest++) {
                edgeMatrix[source][dest].addPheromone(t0);
            }
        }
        
    }
    
    //creates an ant that runs a nearest neighbor tour and sets value of t0
    public void greedyTour() {
        int randomCity = randInt(0, numCities + 1);
        
        AntACS greedyAnt = new AntACS(randomCity, numCities);
        
        t0 = 1 / (numAnts * greedyAnt.getTourLength());
        
    }
    
    //returns a random int bewteen min and max exclusive
    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min)) + min;
        return randomNum;
    }

    /**
     * creates numAnts and runs tours for each
     */
    public void generateAnts() {

        ants = new AntACS[numAnts];

        int startCity = 0;

        for (int index = 0; index < numAnts; index++) {

            //if there are more ants than cities then start looping through the 
            //cities again until we exhaust the numAnts
            if (startCity >= numCities) {
                startCity = startCity % numCities;
            }

            ants[index] = new AntACS(startCity, numCities, pheroWeight, heuristicWeight, q0);

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

    //returns edge object that represents the source to the destination
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
    
    //pretty self explanatory
    public double getBestTourLength(){
        return bestTourLength;
    }

}
