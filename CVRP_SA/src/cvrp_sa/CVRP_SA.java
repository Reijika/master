package cvrp_sa;
 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;


/**
 *
 * @author Simon Chan
 */
public class CVRP_SA {
    
    //Data configuration variables 
    static int capacity = -1;
    static int dimension = -1;
    static int depot_node = -1;
    static HashMap<Integer, Node> node_coordinates = new HashMap<Integer, Node>();
    static HashMap<Integer, Integer> node_demands = new HashMap<Integer, Integer>();
    static Random rng = new Random();
    
    //an object that stores the xy coordinates associated with each node
    static class Node {
        int x, y;
        Node(int x, int y) { this.x = x; this.y = y; }
    }
    
    //an object meant to be inserted into a priority queue for sorting purposes
    static class HeapNode {
        double priority;
        int city1, city2;
        HeapNode(double a, int b, int c) { 
            this.priority = a;
            this.city1 = b;
            this.city2 = c;
        }
    }
    
    //sorts HeapNode such that the objects with the shortest distance (priority) are at the top
    public static class DistanceComparator implements Comparator<HeapNode>{
        @Override
        public int compare(HeapNode x, HeapNode y){            
            if (x.priority < y.priority){
                return -1;
            }
            if (x.priority > y.priority){
                return 1;
            }
            return 0;
        }
    }
    
    //sorts HeapNode such that the objects with the largest avg distance (priority) are at the top
    public static class AverageComparator implements Comparator<HeapNode>{
        @Override
        public int compare(HeapNode x, HeapNode y){            
            if (x.priority > y.priority){
                return -1;
            }
            if (x.priority < y.priority){
                return 1;
            }
            return 0;
        }
    }
    
    //implements the "Replace Highest Average" neighborhood operator described in the attached academic paper
    public static ArrayList<ArrayList<Integer>> replace_avg(ArrayList<ArrayList<Integer>> solution){
        
        Comparator<HeapNode> comparator = new AverageComparator();
        PriorityQueue<HeapNode> queue = new PriorityQueue<HeapNode>(10, comparator);
        ArrayList<Integer> selected = new ArrayList<Integer>();
        ArrayList<Integer> violaters = new ArrayList<Integer>();
        
        int attempts = 10;
        ArrayList<ArrayList<Integer>> backup_copy = new ArrayList<ArrayList<Integer>>();
        backup_copy = deepCopy(solution);
    
        double distance1 = 0;
        double distance2 = 0;
        double avg = 0;
        int city1 = -1;
        int city2 = -1;
        int city3 = -1;
        
        //insert each pair of cities and their avg distances into the min heap
        for (ArrayList<Integer> route: solution){
            for (int i = 1; i < route.size()-1;i++){               
                city1 =  route.get(i-1);
                city2 =  route.get(i);
                city3 =  route.get(i+1);
                distance1 = getDistance(city1, city2);
                distance2 = getDistance(city2, city3);
                avg = (distance1 + distance2)/2.0d;
                queue.add(new HeapNode(avg, city2, 0));
            }
        }
        
        //pulls the 5 vertices with the largest avg distances
        for (int i = 0; i < 5; i++){
            HeapNode current = queue.remove();
            selected.add(current.city1);
        }        
        selected.remove(Integer.valueOf(depot_node));
        //System.out.println("replace_avg() -> selected cities: " + selected);
        
        //attempt to remove the selected vertices from the current solution
        for(Integer i : selected){
            for (ArrayList<Integer> route : solution){
                if (route.contains(i)){
                    if (route.size() == 3){ //if there are only 3 elements left, we can't remove anymore
                        violaters.add(Integer.valueOf(i));
                    }    
                    else{
                        route.remove(Integer.valueOf(i)); //remove it from the route in preparation
                    }
                    break;
                }
            }
        }
        //remove any invalid selected elements from the set
        for (Integer i : violaters){            
            selected.remove(Integer.valueOf(i));
        }
        
        //attempt to randomly insert the selected elements into the solution
        while(selected.size() != 0){
            
            //this condition is meant to handle the rare case where the immediate solution is structured in a way
            //that it is not possible to insert a new element into ANY route due to overcapacity
            //this checks for that and forcefully reverts the transformation to avoid infinite looping
            attempts--;
            if (attempts == 0){ 
                return backup_copy;
            }
                        
            int selected_city = selected.get(0);
            int random_route = rng.nextInt(solution.size()); 
            ArrayList<Integer> potential_route = solution.get(random_route);
            
            int random_index = rng.nextInt(potential_route.size()); 
            if (random_index == 0){
                random_index = 1;
            }
            else if (random_index == (potential_route.size() - 1) ){
                random_index = (potential_route.size() - 2);
            }
            
            potential_route.add(random_index, selected_city);
            if (checkCapacity(potential_route) == true){    //if this new route stays under capacity                
                selected.remove(0); 
            }
            else{
                potential_route.remove(Integer.valueOf(selected_city));
            }            
        }
        
//        System.out.println("After replace_avg operator:");
//        displaySolution(solution);
        
        return solution;
    }
    
    //implements the "Move" neighborhood operator described in the attached academic paper
    public static ArrayList<ArrayList<Integer>> move(ArrayList<ArrayList<Integer>> solution){
        
        Comparator<HeapNode> comparator = new DistanceComparator();
        PriorityQueue<HeapNode> queue = new PriorityQueue<HeapNode>(10, comparator);
        ArrayList<Integer> full_set = new ArrayList<Integer>();
        ArrayList<Integer> selected = new ArrayList<Integer>();
        ArrayList<Integer> violaters = new ArrayList<Integer>();
        
        int attempts = 10;
        ArrayList<ArrayList<Integer>> backup_copy = new ArrayList<ArrayList<Integer>>();
        backup_copy = deepCopy(solution);
        
        int distance = 0;
        int city1 = -1;
        int city2 = -1;
        
        //generate a full set of elements from 1 to dimension
        for (int i = 1; i <= dimension; i++){
            full_set.add(i);
        }
        
        //insert each pair of cities and their distances into the min heap
        for (ArrayList<Integer> route: solution){
            for (int i = 0; i < route.size()-1;i++){               
                city1 =  route.get(i);
                city2 =  route.get(i+1);
                distance = getDistance(city1, city2);
                queue.add(new HeapNode(distance, city1, city2));
            }
        }
               
        //extracts the 5 vertices with the shortest distance and excludes from the full set
        for (int i = 0; i < 5; i++){
            HeapNode current = queue.remove();
            full_set.remove(Integer.valueOf(current.city2));
        }        
        full_set.remove(Integer.valueOf(depot_node)); //remove the depot node as well        
                
        //select 5 random integers in this remaining set and dump it into selected      
        Collections.shuffle(full_set);
        for (int i = 0; i< 5;i++){            
            selected.add(Integer.valueOf(full_set.get(i)));
        }
        
        //attempt to remove the selected vertices from the current solution
        for(Integer i : selected){
            for (ArrayList<Integer> route : solution){
                if (route.contains(i)){
                    if (route.size() == 3){ //if there are only 3 elements, we can't remove anymore
                        violaters.add(Integer.valueOf(i));
                    }    
                    else{
                        route.remove(Integer.valueOf(i)); //remove it from the route in preparation
                    }
                    break;
                }
            }
        }
        //remove any invalid selected elements from the set
        for (Integer i : violaters){
            selected.remove(Integer.valueOf(i));
        }
        
        //attempt to randomly insert the selected elements into the solution
        while(selected.size() != 0){
            
            //this condition is meant to handle the rare case where the immediate solution is structured in a way
            //that it is not possible to insert a new element into ANY route due to overcapacity
            //this checks for that and forcefully reverts the transformation to avoid infinite looping
            attempts--;
            if (attempts == 0){
                return backup_copy;
            }
            
            //these gets are all based on index
            int selected_city = selected.get(0);
            int random_route = rng.nextInt(solution.size()); //routes 0 to size - 1
            ArrayList<Integer> potential_route = solution.get(random_route);
            
            int random_index = rng.nextInt(potential_route.size()); //0 to size - 1
            if (random_index == 0){
                random_index = 1;
            }
            else if (random_index == (potential_route.size() - 1) ){
                random_index = (potential_route.size() - 2);
            }
            
            potential_route.add(random_index, selected_city);
            if (checkCapacity(potential_route) == true){    //if this new route stays under capacity                
                selected.remove(0); 
            }
            else{                
                potential_route.remove(Integer.valueOf(selected_city));
            }            
        }     
        
//        System.out.println("After move() operator:");
//        displaySolution(solution);
        
        return solution;        
    }
    
    //returns the Euclidean distance between two elements
    public static int getDistance(int start, int end){
        Node s_node = node_coordinates.get(start);
        Node e_node = node_coordinates.get(end);
        double xd = (double)(s_node.x - e_node.x);
        double yd = (double)(s_node.y - e_node.y);
        int euclidean = (int)Math.round(Math.sqrt((xd*xd) + (yd*yd)));
        return euclidean; 
    }
    
    //checks for a single route to see if it exceeds the capacity constraint(used by the neighborhood operator functions)
    public static boolean checkCapacity(ArrayList<Integer> route){
        int route_cost = 0;
        for (Integer i : route){
            route_cost = route_cost + node_demands.get(i);
        }        
        
        if (route_cost > capacity){
            return false;
        }
        else{
            return true;
        }
    }
    
    //calculates the total travel cost for an entire solution 
    public static double evaluateCost(ArrayList<ArrayList<Integer>> solution){
        double total_distance = 0;
        
        double distance = 0;
        int city1 = -1;
        int city2 = -1;
        for (ArrayList<Integer> route: solution){
            for (int i = 0; i < route.size()-1;i++){               
                city1 =  route.get(i);
                city2 =  route.get(i+1);
                distance = getDistance(city1, city2);
                total_distance = total_distance + distance;
            }
        }
        return total_distance;
    }
    
    //parses a "file list" (for scripting purposes)
    public static ArrayList<String> parseFileList(String fileList){
        String line = null;
        ArrayList<String> list = new ArrayList<String>();
        
        try {
            FileReader fileReader = new FileReader(fileList);
            BufferedReader bufferedReader = new BufferedReader(fileReader);            
            String[] current_line;
            
            while((line = bufferedReader.readLine()) != null) {
                list.add("input_data/" + line + ".vrp");
            }            
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileList + "'");                
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }

        return list;
    }
    
    //parse a specific "file" and sets SA algorithm variables (for testing purposes)
    public static void parseFile(String fileName){
        
        String line = null;
        
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);            
            String[] current_line;
            
            while((line = bufferedReader.readLine()) != null) {
                
                if (line.contains("DIMENSION")){
                    current_line = line.split(" : ");
                    dimension = Integer.parseInt(current_line[1]);
                    //System.out.println("Parsed dimension: " + dimension);
                }
                else if (line.contains("CAPACITY")){
                    current_line = line.split(" : ");
                    capacity = Integer.parseInt(current_line[1]);
                    //System.out.println("Parsed capacity: " + capacity);
                }
                else if (line.contains("NODE_COORD_SECTION")){
                    for (int i = 0; i < dimension; i++){
                        line = bufferedReader.readLine();                        
                        current_line = line.split("\\s+");
                        int node_num = Integer.parseInt(current_line[1]);
                        int x = Integer.parseInt(current_line[2]);
                        int y = Integer.parseInt(current_line[3]);                              
                        node_coordinates.put(node_num, new Node(x,y));
                        //System.out.println("Parsed node coord: " + current_line[1] + ", " + current_line[2] + ", " + current_line[3]);
                    }
                }
                else if (line.contains("DEMAND_SECTION")){
                    for (int i = 0; i < dimension; i++){
                        line = bufferedReader.readLine();                        
                        current_line = line.split("\\s+");
                        int node_num = Integer.parseInt(current_line[0]);
                        int demand = Integer.parseInt(current_line[1]);
                        node_demands.put(node_num, demand);
                        if (demand == 0){
                            depot_node = node_num;
                        }
                        //System.out.println("Parsed node demand: " + current_line[0] + ", " + current_line[1]);
                    }
                }
                else{
                    //System.out.println(line);
                }
            }            
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    //prints the details of the SA algorithm variables (set arg to true for all the demands and edges)
    public static void displayConfig(boolean additional){
        System.out.println("Route capacity: " + capacity);
        System.out.println("Number of nodes: " + dimension);
        System.out.println("Depot Node: " + depot_node);
        
        if (additional){
            for (HashMap.Entry<Integer, Node> entry : node_coordinates.entrySet()){
                System.out.println(entry.getKey() + " : x->" + entry.getValue().x +", y->" + entry.getValue().y);
            }  
            for (HashMap.Entry<Integer, Integer> entry : node_demands.entrySet()){
                System.out.println(entry.getKey() + " : demand->" + entry.getValue());
            }
        }
    }
    
    //prints out the entire solutiont to console
    public static void displaySolution(ArrayList<ArrayList<Integer>> solution){
        int route_num = 1;
        int route_cost = 0;
        
        for (ArrayList<Integer> route : solution){
            for (Integer i : route){
                route_cost = route_cost + node_demands.get(i);
            }
            System.out.println("Route #" + route_num +": " + route +" -> Total capacity: " + route_cost );
            route_num++;
            route_cost = 0;
        }
    }
    
    //makes a deep copy of the passed in solution, meant to avoid reference errors
    public static ArrayList<ArrayList<Integer>> deepCopy(ArrayList<ArrayList<Integer>> solution){
        ArrayList<ArrayList<Integer>> new_solution = new ArrayList<ArrayList<Integer>>();
        //manually a deep copy of current_solution into new_solution
        for(ArrayList<Integer> route : solution){
            ArrayList<Integer> route_copy = new ArrayList<Integer>();
            for (Integer i : route){
                route_copy.add(i);
            }
            new_solution.add(route_copy);
        }

        return new_solution;
    }
    
    //generates a deterministic initial solution to start the SA algorithm with
    public static ArrayList<ArrayList<Integer>> generateInitial(){
        ArrayList<ArrayList<Integer>> solution = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> route = new ArrayList<Integer>();
        route.add(depot_node);

        int route_capacity = 0;
        for(int i = 1; i <= dimension; i++){
            if (i != depot_node){
                route_capacity = route_capacity + node_demands.get(i);
                if (route_capacity <= capacity){
                    //System.out.println("Current capacity: " + route_capacity);
                    route.add(i);
                }
                else{
                    //System.out.println("Capacity exceeded: " + route_capacity);
                    route.add(depot_node);
                    solution.add(route);
                    
                    route = new ArrayList<Integer>();
                    route.add(depot_node);
                    route.add(i);
                    route_capacity = 0;
                    route_capacity = route_capacity + node_demands.get(i);                    
                }
            }
        }
        route.add(depot_node);
        solution.add(route);
        return solution;
        //System.out.println("Solution: " + current_solution);
        
    }
    
    //executes the Simulated Annealing algorithm
    public static double CVRP_SA(boolean display){

        ArrayList<ArrayList<Integer>> current_solution = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> new_solution = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> best_solution = new ArrayList<ArrayList<Integer>>();
        double current_cost = 0;
        double new_cost = 0;        
        double best_cost = 0;
        double delta_cost = 0;
        double accept_prob = 0;
                
        double alpha = 0.99; //0.99
        double iteration_limit = 5; //2500
        double iterations = 0;      
        double temperature = 5000; //5000
        double final_temperature = 0.001; //0.001
        
        //generate initial solution and record the cost
        current_solution = generateInitial(); 
        current_cost = evaluateCost(current_solution);
        best_cost = current_cost;        
        best_solution = deepCopy(current_solution);
        
        if (display){
            System.out.println("Initial Solution");
            displaySolution(current_solution);
            System.out.println("Cost: " + current_cost);
        }

        while (temperature > final_temperature){
            iterations = iteration_limit;
            
            while (iterations >= 0){
                new_solution = deepCopy(current_solution);
                
                //attempt to apply the neighborhood operator
                if (Math.random() <= 0.8){
                    new_solution = move(new_solution);
                }
                new_solution = replace_avg(new_solution);
                new_cost = evaluateCost(new_solution);                
                delta_cost = new_cost - current_cost;
                //System.out.println("Delta: " + delta_cost + ", new cost: " + new_cost + ", current cost: " + current_cost);
                
                //if this new solution is better 
                if (delta_cost < 0){      
                    //System.out.println("Accepted better solution");
                    current_solution = deepCopy(new_solution);
                    current_cost = new_cost;
                    
                    //if this new solution is the best solution seen so far
                    if (new_cost < best_cost){
                        best_cost = new_cost;
                        best_solution = deepCopy(new_solution);
                    }
                }
                //if this new solution is worse
                else {
                    //System.out.println("Bad solution");
                    //accept it anyway if it is below the acceptance probability
                    accept_prob = Math.exp(-delta_cost/temperature);
                    if (Math.random() < accept_prob){            
                        //System.out.println("Accepted bad solution");
                        current_solution = deepCopy(new_solution);
                        current_cost = new_cost; 
                    }
                }
                iterations = iterations - 1;                
            }
            //decrease the temperature
            temperature = temperature * alpha;
        }
        
        //redundant, but no harm
        best_cost = evaluateCost(best_solution);
        
        if (display){
            System.out.println("Final Solution");
            displaySolution(best_solution);
            System.out.println("Cost: " + best_cost);
        }        
        
        return best_cost;        
    }
    
    public static void main(String[] args) {
        
        //OUTPUT CONFIG A:
        //reads a file list and attempts to run the SA algorithm on each file 3 times
        //please be sure to set the iteration_limit varible in CVRP_SA() to something high like 2500 for close-to optimal results
        //warning: Running the program with a high iteration_limit will take a LONG time. (close to 1.5 hours for everything)        
        //You can use a iteration_limit of 5 if you just want to test for correctness/functionality
        //You can uncomment this block of code to run the SA algorithm for every file 3 times.
        //if you are going to do this, please comment out the other block of code below completely.
        
//        ArrayList<String> fileList = new ArrayList<String>();
//        fileList = parseFileList("input_data/file_list.txt");
//        for (String name : fileList){            
//            parseFile(name);
//            
//            int runs = 3;
//            long startTime = 0;
//            long endTime = 0;
//            double duration = 0;
//            double cost = 0;
//
//            ArrayList<Double> costs = new ArrayList<Double>();
//
//            for (int i = 0; i < runs; i++){
//                startTime = System.nanoTime();
//                cost = CVRP_SA(false);              
//                endTime = System.nanoTime();
//                System.gc();
//                costs.add(cost);
//                duration = (double) (endTime - startTime) / 1000000d;
//            }
//            System.out.println("File: " + name + " -> " + costs);
//        }
        
        //OUTPUT CONFIG B:
        //You can run this block of code to run the SA algorithm for ONE file n times (specify using the runs variable).
        //please be sure to set the iteration_limit varible in CVRP_SA() to something high like 2500 for close-to optimal results
        //warning: Running the program with a high iteration_limit will take a LONG time. (close to 1.5 hours for everything)        
        //You can use a iteration_limit of 5 if you just want to test for correctness/functionality
        //if you are going to do this, please comment out the above block of code completely. 
        parseFile("input_data/A-n32-k5.vrp");
            
        int runs = 1;
        long startTime = 0;
        long endTime = 0;
        double duration = 0;
        double cost = 0;

        ArrayList<Double> costs = new ArrayList<Double>();

        for (int i = 0; i < runs; i++){
            startTime = System.nanoTime();
            cost = CVRP_SA(true);              
            endTime = System.nanoTime();
            System.gc();
            costs.add(cost);
            duration = (double) (endTime - startTime) / 1000000d;
            System.out.println("Execution time: " + duration + " ms");
            System.out.println("KB: " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024);
        }        
        
        System.out.println("Output -> " + costs); 
        
        
        
    
        
    }    
}
