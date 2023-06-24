import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Node implements Runnable {
    private int id;
    private int color;
    private int numNodes;
    private int maxDeg;
    // keeping the ports of the neighbours with a higher ID
    private ArrayList<Integer> largerNeighbors;
    // keeping the ports of the neighbours with a lower ID
    private ArrayList<Integer> smallerNeighbors;
    //keeping the colors of all the neighbours
    private ArrayList<Integer> neighboursColors;

    public Node(int id, int numNodes, int maxDeg, int[][] neighbors) {
        this.id = id;
        this.color = id; //only for first step
        this.numNodes = numNodes;
        this.maxDeg = maxDeg;
        this.largerNeighbors = new ArrayList<>();
        this.smallerNeighbors = new ArrayList<>();

        for (int[] neighbor : neighbors) {
            int neighborID = neighbor[0];
            if (neighborID > this.id) {
                // For receiving messages
                int neighborOutputPort = neighbor[1];
                this.largerNeighbors.add(neighborOutputPort);
            } else {
                // For sending messages
                int neighborInputPort = neighbor[2];
                this.smallerNeighbors.add(neighborInputPort);
            }
        }
    }

    public void run() {
        // True if this Node is has the highest ID among its neighbours
        if (this.largerNeighbors.size() == 0) {
            this.color = 0;
            // All of its neighbours are 'smaller'
            for (Integer port : smallerNeighbors)
                sendMessage(this.color, port);
        } else {
            reciveFromHigher();
            waitNeighbors();
            minimalNonConflictingColor();
            for (Integer port : smallerNeighbors)
                sendMessage(this.color, port);
        }
    }

    private void reciveFromHigher() {
        for (Integer port : this.largerNeighbors){
            Thread thread = new Thread(() -> receiveMessage(port));
            thread.start();
        }
    }

    private void receiveMessage(Integer port) {
        try {
            // Create a DatagramSocket bound to the specified port
            DatagramSocket socket = new DatagramSocket(port);

            // Create a byte array to hold the received message
            byte[] buffer = new byte[Integer.BYTES];

            // Create a DatagramPacket to receive the message
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Receive the packet
            socket.receive(packet);

            ByteArrayInputStream byteInput = new ByteArrayInputStream(packet.getData());
            ObjectInputStream objectInput = new ObjectInputStream(byteInput);

//            int input = (int) objectInput.readObject();
            int input = objectInput.readInt();
            this.neighboursColors.add(input);

            byteInput.close();
            objectInput.close();
            socket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void minimalNonConflictingColor() {
        int temp = 0;
        ArrayList<Integer> used_colors = this.neighboursColors;
        while (true) {
            if (used_colors.contains(temp))
                temp++;
            else {
                setColor(temp);
                break;
            }
        }
    }

    private void waitNeighbors() {
    }


    private void sendMessage(int message, int port) {
        try {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);

            // Write the integer message to the ObjectOutputStream
            objectOutput.writeInt(message);
            objectOutput.flush();

            // Get the message bytes from the ByteArrayOutputStream
            byte[] messageBytes = byteOutput.toByteArray();

            // Get the localhost IP address
            InetAddress destinationAddress = InetAddress.getLocalHost();

            // Create a DatagramPacket with the message bytes, destination address, and port
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, destinationAddress, port);

            // Create a DatagramSocket
            DatagramSocket socket = new DatagramSocket();

            // Send the packet
            socket.send(packet);

            // Close the socket
            socket.close();

            // Close the ObjectOutputStream and ByteArrayOutputStream
            byteOutput.close();
            objectOutput.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
