/* Creates an instance of one ant for the ACS algorithm.
*/

package aco;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author PryhuberA
 */
public class AntACS {

    private final int startCity;
    private final int numEdges;
    private final int numCities;
    

    private Edge[] tour;
    private double tourLength = 0.0;
    private int prevCity;
    private int currCity;
    private int tourIndex = 0;
    private double pheroWeight;
    private double heuristicWeight;
    private double q0;
    private ArrayList<Integer> allowedCities = new ArrayList<Integer>();

    //constructor for ACS ants,
    public AntACS(int startCity, int numCities, double pheroWeight, double heuristicWeight, double q0) {

        this.startCity = startCity;
        this.numEdges = numCities - 1;
        this.numCities = numCities;
        this.pheroWeight = pheroWeight;
        this.heuristicWeight = heuristicWeight;
        this.q0 = q0;

        //create a new edge object 
        tour = new Edge[numCities];

        //add every city to the allowed city arrayList
        for (int cityIndex = 0; cityIndex < numCities; cityIndex++) {
            if (cityIndex == startCity) {
                continue;
            }
            allowedCities.add(cityIndex);
        }

        currCity = startCity;
        prevCity = startCity;

    }

    //construcor to create a nearest neighbor tour, will lead to tO (tau_O)
    public AntACS(int startCity, int numCities) {
        this.startCity = startCity;
        this.numEdges = numCities - 1;
        this.numCities = numCities;
        this.currCity = startCity;

        //create new edge object
        tour = new Edge[numCities];

        //add every city to the allowed city arrayList
        for (int cityIndex = 0; cityIndex < numCities; cityIndex++) {
            if (cityIndex == startCity) {
                continue;
            }
            allowedCities.add(cityIndex);
        }

        //for every edge(path) between cities, update our tour
        for (int edge = 0; edge < numEdges; edge++) {

            double shortestLength = Double.MAX_VALUE;
            int closestCity = currCity;
            int closestCityIndex = allowedCities.indexOf(closestCity);

            //goes through every possible allowed city looking for the closest
            for (int index = 0; index < allowedCities.size(); index++) {

                double currLength = ACS.getEdge(currCity, allowedCities.get(index)).getLength();

                //checks to see if value is shorter than current best so far
                if (currLength < shortestLength) {
                    shortestLength = currLength;
                    closestCity = allowedCities.get(index);
                    closestCityIndex = index;
                }

            }

            //updates next city to be the next closest city
            tour[tourIndex] = ACS.getEdge(currCity, closestCity);
            prevCity = currCity;
            currCity = closestCity;
            allowedCities.remove(closestCityIndex);

            tourLength += (tour[tourIndex]).getLength();
            tourIndex++;
        }

        tour[tourIndex] = ACS.getEdge(currCity, startCity);
        tourLength += tour[tourIndex].getLength();

    }

    //method to add an edge to our current tour
    public void addEdge() {
        //determine if next move is probabilistic or determinisitic
        Random actionSelect = new Random();
        double choice = actionSelect.nextDouble();

        if (choice <= q0) {
            addDeterministic();
        } else {
            addProbabilistic();
        }

        //add the length of the edge we just added to the current tour length
        tourLength += (tour[tourIndex]).getLength();
        tourIndex++;

    }

    //method to probabilistically add tour edges
    public void addProbabilistic() {

        double allowedEdgeSum = 0.0;

        //calculate allowedEdgeSum denominator for the cities allowed 
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
                tour[tourIndex] = ACS.getEdge(currCity, allowedCities.get(index));
                prevCity = currCity;
                currCity = allowedCities.get(index);
                allowedCities.remove(index);
                break;
            }
        }

    }

    //chooses next edge greedily 
    public void addDeterministic() {
        double bestVal = 0.0;
        int bestCity = currCity;
        int bestCityIndex = allowedCities.indexOf(bestCity);

        //goes through every possible value for allowed cities
        for (int index = 0; index < allowedCities.size(); index++) {

            double currVal;
            Edge nextEdge = ACS.getEdge(currCity, allowedCities.get(index));

            double pheroLevel = nextEdge.getPheromone();
            double eta = 1 / (nextEdge.getLength());
            double etaToBeta = Math.pow(eta, heuristicWeight);

            currVal = pheroLevel * etaToBeta;

            //checks to see if value is better than current best
            if (currVal > bestVal) {
                bestVal = currVal;
                bestCity = allowedCities.get(index);
                bestCityIndex = index;
            }

        }

        //updates next city to be the best value according to this method of assesment
        tour[tourIndex] = ACS.getEdge(currCity, bestCity);
        prevCity = currCity;
        currCity = bestCity;

        allowedCities.remove(bestCityIndex);

    }

//    makes sure ants go home
    public void finalEdge() {
        tour[tourIndex] = ACS.getEdge(currCity, startCity);
        tourLength += tour[tourIndex].getLength();
    }

    //print info from each edge of the Ant's tour
    public void printPath() {
        for (int i = 0; i < tour.length; i++) {
            System.out.println("Edge from  " + tour[i].getSource() + " to " + tour[i].getDest());
        }
    }

    //getter to return current tour as an edge matrix
    public Edge[] getTour() {
        return tour;
    }

    //getter for tour length
    public double getTourLength() {
        return tourLength;
    }

    //getter for current city
    public int getCurrCity() {
        return currCity;
    }

    //getter for previous city
    public int getPrevCity() {
        return prevCity;
    }

    // returns pheromone and heuristic info about an edge to be used to calculate
    // probabilities of adding an edge
    public double calcEdgeData(int source, int dest) {
        return ((Math.pow(ACS.getPheroLevel(source, dest), pheroWeight))
                * Math.pow(1 / ACS.getEdgeLength(source, dest), heuristicWeight));
    }

}
