/* Each Edge object holds two cities, as well as the length between them and 
 * the amount of pheromone on that path. Each each is also capable of maintaining
 * its own pheromone levels through evaporation, wearing away, and laying down.
*/
package aco;

import java.io.*;

/**
 *
 * @author PryhuberA
 */
public class Edge {

    private final City cityA;
    private final City cityB;

    private double pheroLevel;
    private double edgeLength;

    //construct an edge object
    public Edge(City source, City dest) {
        cityA = source;
        cityB = dest;
        pheroLevel = 0.0;

        if (source == dest) {
            edgeLength = 0;
        } else {
            setLength(source, dest);
        }

    }

    //evaporate pheromone proportional to how much is already there
    public void evapPheromone(double evapFactor) {
        pheroLevel = (1 - evapFactor) * pheroLevel;
    }

    //wear the pheromone away proportional to how much is already there and tau
    public void wearPheromone(double wearFactor, double tO) {
        pheroLevel = (1 - wearFactor) * pheroLevel + wearFactor * tO;
    }

    //return pheromone
    public double getPheromone() {
        return pheroLevel;
    }

    //add pheromone
    public void addPheromone(double newPhero) {
        pheroLevel += newPhero;
    }

    //return "source" city
    public int getSource() {
        return cityA.getNum();
    }

    //return "destination" city
    public int getDest() {
        return cityB.getNum();
    }

    //return distance between the cities
    public double getLength() {
        return edgeLength;
    }

    //method to calculate Euclidean distance between cities
    //using c1.getX, c1.getY and so on
    public void setLength(City c1, City c2) {
        edgeLength = Math.sqrt(Math.pow((c1.getX() - c2.getX()), 2)
                + Math.pow((c1.getY() - c2.getY()), 2));
    }

}
