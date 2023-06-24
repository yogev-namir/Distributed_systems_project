import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;

import static java.lang.Thread.sleep;

public class Manager {
    private ArrayList<Node> nodes_array;
    private ArrayList<Thread> threads;
    private int nodes_number;
    private int delta;
    private StringBuilder output;
    public Manager() {
        this.nodes_array = new ArrayList<Node>();
    }

    public void readInput(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            // Read total number of nodes
            this.nodes_number = Integer.parseInt(reader.readLine().trim());

            // Read delta
            this.delta = Integer.parseInt(reader.readLine().trim());

            // Read node information

            for (int i = 0; i < this.nodes_number; i++) {
                String line = reader.readLine().trim();
                int nodeId = Integer.parseInt(line.substring(0, line.indexOf(" ")));
                String neighbors = line.substring(4, line.lastIndexOf("]]"));
                String[] parts = neighbors.split("], \\[");
                int[][] neighborsArr = new int[parts.length][3];
                for (int j = 0; j < parts.length; j++) {
                    String[] neighborParts = parts[j].substring(0, parts[j].length() - 1).split(", ");
                    int neighborId = Integer.parseInt(neighborParts[0].trim());
                    int writingPort = Integer.parseInt(neighborParts[1].trim());
                    int readingPort = Integer.parseInt(neighborParts[2].trim());
                    neighborsArr[j] = new int[]{neighborId, writingPort, readingPort};
                }
                Node new_node = new Node(nodeId, this.nodes_number, this.delta, neighborsArr);
                this.nodes_array.add(new_node);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String start() {
        // Initializing
        for (Node node : this.nodes_array){
            Thread thread = new Thread(node);
            thread.start();
            this.threads.add(thread);
            try {
                sleep(1);
            }
            catch (InterruptedException ignored){}
        }
        // Finishing
        for (Thread thread : this.threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Output builder
        output = new StringBuilder();
        for (Node node : nodes_array){
            output.append("Node ID: ").append(node.getId()).
                    append(", Color: ").append(node.getColor()).append("\n");
        }
        return output.toString();
    }

    public String terminate() {
        // your code here
        return "coloring massage in output format";
    }
}