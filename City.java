/*
 * Object to hold coordinates of each city
 */
package aco;

/**
 *
 * @author PryhuberA
 */
public class City {

    private int number;
    private double xCoordinate;
    private double yCoordinate;

    //constructor
    public City(int cityNumber, double x, double y) {
        number = cityNumber-1;
        xCoordinate = x;
        yCoordinate = y;
    }

    //getter for x coordinate
    public double getX() {
        return xCoordinate;
    }

    //getter for y coordinate
    public double getY() {
        return yCoordinate;
    }
    
    //getter for city number
    public int getNum(){
        return number;
    }
}
