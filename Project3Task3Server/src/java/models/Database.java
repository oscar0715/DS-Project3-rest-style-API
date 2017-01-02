package models;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Singleton Database class representing the database for the report
 * 
 * @author qifanj
 */
public class Database {
    
    private static ArrayList<String[]> sensor1;
    private static ArrayList<String[]> sensor2;
    private static Database instance;
    
    /**
     * Instantiate
     **/
    public static Database getInstance() {
        if(instance == null) {
            instance = new Database();
            sensor1 = new ArrayList<>();
            sensor2 = new ArrayList<>();
        }
        return instance;
    }
    
    /**
     * store the report
     * 
     * @param report to be stored
     */
    public static void store(String[] report) {
        if(report[0].equals("1")) {
            sensor1.add(report);
        } else {
            sensor2.add(report);
        }
    }
    
    /**
     * get all the reports
     * 
     * @return the reports
     */
    public static String getAll() {
        StringBuilder result = new StringBuilder();
        for(String[] report: sensor1) {
            result.append(Arrays.toString(report));
        }
        for(String[] report: sensor2) {
            result.append(Arrays.toString(report));
        }
        // if no report stored yet
        if(result.length()==0) return "No records yet";
        return result.toString();
    }
    
    /**
     * get last temperature report for given sensor
     * 
     * @param id sensorID
     * @return the last temperature report
     */
    public static String getLast(int id) {
        if(id==1) {
            if(sensor1.isEmpty()) return "No records yet";
            return Arrays.toString(sensor1.get(sensor1.size()-1));
        } else {
            if(sensor2.isEmpty()) return "No records yet";
            return Arrays.toString(sensor2.get(sensor2.size()-1));
        }
    }
}
