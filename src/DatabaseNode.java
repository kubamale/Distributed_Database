import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseNode extends Thread {
    private int PORT;
    private Map<String, String> values;
    private Map<String, List<Integer>> availblePorts;

    private static Map<String, List<Integer>> usedPorts = new HashMap<>();
    private boolean canRun;

    public DatabaseNode(int PORT, Map<String, String> values, Map<String, List<Integer>> availblePorts) {
        this.PORT = PORT;
        this.values = values;
        this.availblePorts = availblePorts;
        canRun = true;
    }


    public static void main(String[] args) throws IOException {
       new DatabaseNodeConfig(args);
    }

    public static Map<String, List<Integer>> getUsedPorts() {
        return usedPorts;
    }

    public static void log(String message) {
        System.out.println("[S]: " + message);
        System.out.flush();
    }

    public Map<String, List<Integer>> getAvailblePorts() {
        return availblePorts;
    }

    public void deleteConnection(String address, int portToDestroy) {


        System.out.println("deleting[ " + address + ":" + portToDestroy + " ]");
        List<Integer> currentPorts = availblePorts.get(address);
        List<Integer> newPorts = new ArrayList<>();

        for (Integer port : currentPorts){
            if (port != portToDestroy){
                newPorts.add(port);
            }
        }
        availblePorts.put(address,newPorts);
    }


    public void setCanRun(boolean canRun) {
        this.canRun = canRun;
    }

    @Override
    public void run() {
        try {
            log("Starting");
            log("Server socket opening");
            InetAddress sa = InetAddress.getByName("localhost");
            ServerSocket welcomeSocket = new ServerSocket(PORT);
            log("Server socket opened");
            while (canRun) {
                System.out.println("Server is listening");
                Socket clientSocket = welcomeSocket.accept();
                System.out.println("Server is connected");
                if (canRun) {
                    ServerA st = new ServerA(this.availblePorts, this.values, clientSocket, usedPorts, this);
                    Thread thread = new Thread(st);
                    thread.start();
                }

            }
            log("Server socket is closing");
            welcomeSocket.close();
        } catch (IOException ignored) {


        }
    }

    public int getPORT() {
        return PORT;
    }

    public Map<String, List<Integer>> updateUsedPorts(String id) {
        if (!usedPorts.containsKey(id)) {
            List<Integer> l = new ArrayList<>();
            l.add(PORT);
            usedPorts.put(id, l);
        } else {
            if (!usedPorts.get(id).contains(PORT)) {
                List<Integer> l = usedPorts.get(id);
                l.add(PORT);
                usedPorts.put(id, l);
            }
        }

        return usedPorts;
    }
}