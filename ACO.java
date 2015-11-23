/*
 * ACO creates instances of both ACS and EAS for each of the values being tested
 * Each set of parameters is run 3 times and then averaged before being stored
 * in the CSV. All values, other than alpha, beta, and rho, are held at the rule
 * of thumb values.
 */
package aco;

import java.util.*;
import java.io.*;

/**
 *
 * @author PryhuberA
 */
public class ACO {

    private static final int STAND_NUM_ANTS = 20;
    private static final double STAND_ALPHA = 1;
    private static final double STAND_BETA = 3.5;
    private static final double STAND_RHO = .1;
    private static final double STAND_ELITISM = 20;
    private static final int NUM_ITER = 1000;
    private static final double MAX_PERCENT_OF_OPT = 1;
    private static final double NUM_RUNS = 3;

    //For EAS
    private static double elitism;

    //For ACS
    private static final double Q_FINAL = 0.9;
    private static final double EPSILON = 0.1;

    private static String file;
    private static double optLen;
    private static double numCities;

    private ArrayList<Double> data = new ArrayList<Double>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        
        PrintStream out = System.out;
        PrintStream std = System.out;
        try {
            FileOutputStream pw = new FileOutputStream("acoDataFile3.csv", true);
            out = new PrintStream(pw);
            System.setOut(out);
                
        }
        catch(FileNotFoundException ex){
            System.out.println(ex.getMessage());
        }
        
    String file1 = "pr2392.tsp";
    double file1optLen = 378032;
    double file1cities = 2392;

    String file2 = "fl3795.tsp";
    double file2optLen = 28772;
    double file2cities = 3795;

    String file3 = "fnl4461.tsp";
    double file3optLen = 182566;
    double file3cities = 4461;
    
    
    String file4 = "rl5915.tsp";
    double file4optLen = 565530;
    double file4cities = 5915;

    //for loop to run through all of our different files
    for (int j = 0; j < 4; j++) {
        if (j == 0) {
//            file = file1;
//            optLen = file1optLen;
//            numCities = file1cities;
            continue;
        }
        if (j == 1) {
//            file = file2;
//            optLen = file2optLen;
//            numCities = file2cities;
            continue;
        }
        if (j == 2) {
            file = file3;
            optLen = file3optLen;
            numCities = file3cities;
            //continue;
        }
        if (j == 3) {
//            file = file4;
//            optLen = file4optLen;
//            numCities = file4cities;
            continue;
        }

        System.out.printf("\n\n\n\nOpt tour length is: " + optLen + "\n");
        System.out.printf("Number of cities is: " + numCities + "\n\n");
        
        //for all "rule of thumb" values
        System.out.printf("Base case for ACS\n");
            ACO acsB = new ACO(STAND_NUM_ANTS, NUM_ITER, STAND_ALPHA, STAND_BETA,
                    STAND_RHO, Q_FINAL, EPSILON, optLen, MAX_PERCENT_OF_OPT, file);

        System.out.printf("\nBase case for EAS\n");
            ACO easB = new ACO(STAND_NUM_ANTS, NUM_ITER, STAND_ALPHA, STAND_BETA,
                    STAND_RHO, STAND_ELITISM, file, optLen, MAX_PERCENT_OF_OPT);

        
            
            //testing for different values of rho
            System.out.printf("\nRHO TESTS\n");
            for (int r = 5; r < 36; r += 15){
                double rho = r * 0.01;               
            
            //testing different values of alpha and beta
            //have to do this first since for loops need ints
            System.out.printf("\n\nALPHA and BETA TESTS and RHO TESTS\n");
            for(int a = 5; a < 16; a += 5){
                double alpha = a * 0.1;
                
                for(int b = 20; b < 51; b += 15){
                    double beta = b * 0.1;
                    
                    //to avoid redundant code
                    if(beta == STAND_BETA && alpha == STAND_ALPHA && rho == STAND_RHO){
                        continue;
                    }
                    
                    System.out.printf("\nAlpha equals: ," + alpha + "\n");
                    System.out.printf("Beta equals: ," + beta + "\n");
                    System.out.printf("\nRho equals: ," + rho + "\n");
                    
                    System.out.printf("\nACS:\n");
                    ACO acs1 = new ACO(STAND_NUM_ANTS, NUM_ITER, alpha, beta,
                        rho, Q_FINAL, EPSILON, optLen, MAX_PERCENT_OF_OPT, file);
                    System.out.printf("\nEAS:\n");
                    ACO eas1 = new ACO(STAND_NUM_ANTS, NUM_ITER, alpha, beta,
                        rho, STAND_ELITISM, file, optLen, MAX_PERCENT_OF_OPT);
                    
                }
            }
            
            } 
        }
    }

    //Elitist Ant System
    public ACO(int numAnts, int numIter, double alpha, double beta, double rho,
            double elitism, String fileName, double optLength, double percent) {
        
        double totTime = 0;
        double totLen = 0;

        for (int run = 0; run < NUM_RUNS; run++) {
            //clocks how long running code once takes
            long startTime = System.nanoTime();
            EAS test = new EAS(numAnts, numIter, alpha, beta, rho, elitism, fileName, optLength, percent);
            long timeElapsed = System.nanoTime() - startTime;
            
            //turns nanoseconds into normal seconds
            double finalTime = (double)timeElapsed/1000000000;
            
            //adds tour length to csv file
            double bestTourLen = test.getBestTourLength();
            data.add(bestTourLen);
            System.out.print("Run " + (run + 1) + ": , ");
            System.out.print(data.get(run) + ", ");
            System.out.printf("\n");
            
            //keeps track of values for average
            totTime += finalTime;
            totLen += bestTourLen;
        }
        double avgTime = (totTime/NUM_RUNS);
        double avgLen = totLen/NUM_RUNS;
        double tourRatio = avgLen/optLen;
        
        System.out.printf("Average time: , " + avgTime + ", ");
        System.out.printf("Average length: , " + avgLen + ", ");
        System.out.printf("Tour Ratio: , " + tourRatio + "\n");
    

    }

    //Ant Colony System
    public ACO(int numAnts, int numIter, double alpha, double beta, double rho,
            double q0, double epsilon, double optLength, double percent, String file) {

        double totTime = 0;
        double totLen = 0;

        for (int run = 0; run < NUM_RUNS; run++) {
            //clocks how long running code once takes
            long startTime = System.nanoTime();
            ACS test = new ACS(numAnts, numIter, alpha, beta, rho, q0, epsilon, optLength, percent, file);
            long timeElapsed = System.nanoTime() - startTime;
            
            //turns nanoseconds into normal seconds
            double finalTime = (double)timeElapsed/1000000000;
            
            //adds tour length to csv file
            double bestTourLen = test.getBestTourLength();
            data.add(bestTourLen);
            System.out.printf("Run " + (run + 1) + ": , ");
            System.out.print(data.get(run) + ", ");
            System.out.printf("\n");
            
            //keeps track of values for average
            totTime += finalTime;
            totLen += bestTourLen;
        }
        
        double avgTime = (totTime/NUM_RUNS);
        double avgLen = totLen/NUM_RUNS;
        double tourRatio = avgLen/optLen;
        
        System.out.printf("Average time: , " + avgTime + ", ");
        System.out.printf("Average length: , " + avgLen + ", ");
        System.out.printf("Tour Ratio: , " + tourRatio + "\n");
    }

}
