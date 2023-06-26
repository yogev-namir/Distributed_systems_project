// TCP Implementation
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Node implements Runnable {
    private int id;
    private int color;
    private int numNodes;
    private int maxDeg;
    private List<Integer> neighborsColors;
    // keeping the ports of the neighbours with a higher ID
    private ArrayList<Integer> largerNeighbors;
    // keeping the ports of the neighbours with a lower ID
    private ArrayList<Integer> smallerNeighbors;
    //keeping the colors of all the neighbours
    private ArrayList<Integer> neighboursColors;

    public Node(int id, int numNodes, int maxDeg, int[][] neighbors) {
        this.id = id;
        this.color = id;
        this.numNodes = numNodes;
        this.maxDeg = maxDeg;
        this.neighborsColors = new ArrayList<>();
        this.largerNeighbors = new ArrayList<>();
        this.smallerNeighbors = new ArrayList<>();
        for (int[] neighbor : neighbors) {
            int neighborID = neighbor[0];
            if (neighborID > this.id) {
                // For receiving messages
                int neighborOutputPort = neighbor[2];
                this.largerNeighbors.add(neighborOutputPort);
            } else {
                // For sending messages
                int neighborInputPort = neighbor[1];
                this.smallerNeighbors.add(neighborInputPort);
            }
        }
    }

    public void run() {
//        System.out.print("\nNode "+this.id+" invoked");
        // True if this Node is has the highest ID among its neighbours
        if (this.largerNeighbors.size() == 0) {
            this.color = 0;
            // All of its neighbours are 'smaller'
            for (Integer port : smallerNeighbors) {
//                System.out.print("\nNode " + this.id + " sending " +
//                        this.color+" to port "+port);
                sendMessage(this.color, port);
            }
        } else {
            receiveMessages();
            setMinimalNonConflictingColor();
            for (Integer port : smallerNeighbors)
                sendMessage(this.color, port);
        }
    }

    private void receiveMessages() {
        CopyOnWriteArrayList<Thread> receivingList = new CopyOnWriteArrayList<>();
        for (Integer port : this.largerNeighbors){
            Thread thread = new Thread(() -> receiveMessage(port));
            receivingList.add(thread);
            thread.start();
            try{
                Thread.sleep(2);
            } catch (InterruptedException ignored){
            }
        }
        for (Thread thread : receivingList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveMessage(int receiverPort) {
        try {
            ServerSocket serverSocket = new ServerSocket(receiverPort);
            Socket socket = serverSocket.accept();
            ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());
            int receivedColor = (int) objectInput.readObject();
            socket.close();
            serverSocket.close();
            neighborsColors.add(receivedColor);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(int message, int receiverPort) {
        try {
            Socket socket = new Socket("localhost", receiverPort);
            ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
            objectOutput.writeObject(message);
            objectOutput.flush();
            objectOutput.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getColor() {
        return color;
    }

    private void setMinimalNonConflictingColor() {
        int minimalColor = 0;
        while (neighborsColors.contains(minimalColor)) {
            minimalColor++;
        }
        color = minimalColor;
    }

    public int getNodeId() {
        return id;
    }

    public int getId() {
        return this.id;
    }
}


// UDP Implementation
//import java.io.*;
//import java.net.*;
//import java.util.ArrayList;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//import static java.lang.Thread.sleep;
//
//public class Node implements Runnable {
//    private int id;
//    private int color;
//    private int numNodes;
//    private int maxDeg;
//    // keeping the ports of the neighbours with a higher ID
//    private ArrayList<Integer> largerNeighbors;
//    // keeping the ports of the neighbours with a lower ID
//    private ArrayList<Integer> smallerNeighbors;
//    //keeping the colors of all the neighbours
//    private ArrayList<Integer> neighboursColors;
//
//    public Node(int id, int numNodes, int maxDeg, int[][] neighbors) {
//        this.id = id;
//        this.color = id; //only for first step
//        this.numNodes = numNodes;
//        this.maxDeg = maxDeg;
//        this.largerNeighbors = new ArrayList<>();
//        this.smallerNeighbors = new ArrayList<>();
//
//        for (int[] neighbor : neighbors) {
//            int neighborID = neighbor[0];
//            if (neighborID > this.id) {
//                // For receiving messages
//                int neighborOutputPort = neighbor[2];
//                this.largerNeighbors.add(neighborOutputPort);
//            } else {
//                // For sending messages
//                int neighborInputPort = neighbor[1];
//                this.smallerNeighbors.add(neighborInputPort);
//            }
//        }
//    }
//
//    public void run() {
//        System.out.print("\nNode "+this.id+" invoked");
//        // True if this Node is has the highest ID among its neighbours
//        if (this.largerNeighbors.size() == 0) {
//            this.color = 0;
//            // All of its neighbours are 'smaller'
//            for (Integer port : smallerNeighbors) {
//                System.out.print("\nNode " + this.id + " sending " +
//                        this.color+" to port "+port);
//                sendMessage(this.color, port);
//            }
//        } else {
//            receiveFromHigher();
//            minimalNonConflictingColor();
//            for (Integer port : smallerNeighbors)
//                sendMessage(this.color, port);
//        }
//    }
//
//    private void receiveFromHigher() {
//        CopyOnWriteArrayList<Thread> receivingList = new CopyOnWriteArrayList<>();
//        for (Integer port : this.largerNeighbors){
//            Thread thread = new Thread(() -> receiveMessage(port));
//            receivingList.add(thread);
//            thread.start();
//            try{
//                sleep(2);
//            } catch (InterruptedException ignored){
//            }
//        }
//        for (Thread thread : receivingList) {
//            try {
//                thread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//        private void receiveMessage(int port) {
//        try {
//            // Create a DatagramSocket bound to the specified port
//            DatagramSocket socket = new DatagramSocket(port);
//            // Create a byte array to hold the received message
//            byte[] buffer = new byte[Integer.BYTES];
//
//            // Create a DatagramPacket to receive the message
//            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//
//            // Receive the packet
//            socket.receive(packet);
//
//            int receivedPort = socket.getLocalPort();
//            System.out.println("\nReceived packet from port: " + receivedPort);
//
//            ByteArrayInputStream byteInput = new ByteArrayInputStream(packet.getData());
//            ObjectInputStream objectInput = new ObjectInputStream(byteInput);
//
//            int input = (int) objectInput.readObject();
//            this.neighboursColors.add(input);
//
//            byteInput.close();
//            objectInput.close();
//            socket.close();
//
//
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    //nodeID [[neighborId, writingPort, readingPort], …]
//    private void sendMessage(int message, int port) {
//        try {
//            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
//            ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
//
//            // Write the integer message to the ObjectOutputStream
//            objectOutput.writeObject(message);
//            objectOutput.flush();
//
//            // Get the message bytes from the ByteArrayOutputStream
//            byte[] messageBytes = byteOutput.toByteArray();
//
//            // Get the localhost IP address
////            InetAddress destinationAddress = InetAddress.getLocalHost();
//            InetAddress destinationAddress = InetAddress.getByName("localhost");
//
//            // Create a DatagramPacket with the message bytes, destination address, and port
//            DatagramPacket packet = new DatagramPacket(messageBytes,
//                    messageBytes.length, destinationAddress, port);
//
//            // Create a DatagramSocket
//            DatagramSocket socket = new DatagramSocket();
//
//            // Send the packet
//            socket.send(packet);
//
//            // Close the socket
//            socket.close();
//
//            // Close the ObjectOutputStream and ByteArrayOutputStream
//            byteOutput.close();
//            objectOutput.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    private void minimalNonConflictingColor() {
//        int temp = 0;
//        ArrayList<Integer> used_colors = this.neighboursColors;
//        while (true) {
//            if (used_colors.contains(temp))
//                temp++;
//            else {
//                setColor(temp);
//                System.out.print("\nNode "+this.id+" chose color "+temp);
//                break;
//            }
//        }
//    }
//
//
//    public int getId() {
//        return id;
//    }
//
//    public int getColor() {
//        return color;
//    }
//
//    public void setColor(int color) {
//        this.color = color;
//    }
//
////    private void receiveColors() {
////        List<Future<Integer>> receivedColors = new ArrayList<>();
////        ExecutorService executor = Executors.newFixedThreadPool(neighbors.length);
////        for (int[] neighbor : neighbors) {
////            int neighborId = neighbor[0];
////            int neighborPort = neighbor[1];
////            if (neighborId > id) {
////                receivedColors.add(executor.submit(() -> {
////                    try {
////                        ServerSocket serverSocket = new ServerSocket(neighborPort);
////                        Socket socket = serverSocket.accept();
////                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
////                        int receivedColor = (int) ois.readObject();
////                        socket.close();
////                        serverSocket.close();
////                        return receivedColor;
////                    } catch (IOException | ClassNotFoundException e) {
////                        return -1;
////                    }
////                }));
////            }
////        }
//
//
//}


