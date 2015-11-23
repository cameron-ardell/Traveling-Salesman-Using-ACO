/*
 * Creates an instance of one ant for the EAS algorithm.
 */
package aco;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author PryhuberA
 */
public class AntEAS {

    private final int startCity;
    private final double pheroWeight;
    private final double heuristicWeight;
    private final int numEdges;
    private final int numCities;
    
    private Edge[] tour;
    private double tourLength = 0.0;
    private int currCity;
    private int tourIndex = 0;
    private ArrayList<Integer> allowedCities = new ArrayList<Integer>();
    
    //constructor for ant
    public AntEAS(int startCity, int numCities, double pheroWeight, double heuristicWeight) {

        this.startCity = startCity;
        this.numEdges = numCities - 1;
        this.numCities = numCities;
        this.pheroWeight = pheroWeight;
        this.heuristicWeight = heuristicWeight;

        tour = new Edge[numCities];

        //add all but the startCity to allowedCities
        for (int cityIndex = 0; cityIndex < numCities; cityIndex++) {
            if (cityIndex == startCity) {
                continue;
            }
//            System.out.println("adding " + cityIndex + " to allowedCities");
            allowedCities.add(cityIndex);
        }

        currCity = startCity;

        //build the tour by adding numEdges edges
        for (int i = 0; i < numEdges; i++) {
            addEdge();
        }

        // go home ants, you're drunk
        tour[tourIndex] = EAS.getEdge(currCity, startCity);

        tourLength += tour[tourIndex].getLength();

    }
    
    //adds edge to the tour so far, and updates the tour length
    public void addEdge() {

        double allowedEdgeSum = 0;

        for (int city = 0; city < allowedCities.size(); city++) {
            allowedEdgeSum += calcEdgeData(currCity, allowedCities.get(city));
        }

        //create random double between 0.0 and 1.0
        Random rand = new Random();
        double currRand = rand.nextDouble();

        double sum = 0.0;

        //go through all allowedCities, incrementally adding probabilities, until 
        //doubleSum surpasses currRand
        for (int index = 0; index < allowedCities.size(); index++) {

            sum += calcEdgeData(currCity, allowedCities.get(index)) / allowedEdgeSum;

            //once we surpass currRand, travel to the current allowed city we
            //are considering
            if (currRand < sum) {

                tour[tourIndex] = EAS.getEdge(currCity, allowedCities.get(index));
                currCity = allowedCities.get(index);
                allowedCities.remove(index);
                break;
            }

        }

        //add the length of the edge we just added to the current tour length
        tourLength += (tour[tourIndex]).getLength();

        tourIndex++;

    }

    //print info from each edge of the Ant's tour
    public void printPath() {
        for (int i = 0; i < tour.length; i++) {
            System.out.println("Edge from  " + tour[i].getSource() + " to " + tour[i].getDest());
        }
    }

    //return current tour as an edge matrix
    public Edge[] getTour() {
        return tour;
    }

    //getter for tour length
    public double getTourLength() {
        return tourLength;
    }

    // returns pheromone and heuristic info about an edge to be used to calculate
    // probabilities of adding an edge
    public double calcEdgeData(int source, int dest) {
        return ((Math.pow(EAS.getPheroLevel(source, dest), pheroWeight))
                * Math.pow(1 / EAS.getEdgeLength(source, dest), heuristicWeight));
    }

}
