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
    private static final String openingFlag = "1000";

    String serverName;

    private static Map<String, List<Integer>> usedPorts = new HashMap<>();
    private boolean canRun;

    public DatabaseNode(int PORT, Map<String, String> values, Map<String, List<Integer>> availblePorts) {
        this.PORT = PORT;
        this.values = values;
        this.availblePorts = availblePorts;
        canRun = true;
    }


    public static void main(String[] args) {
        String[][] values = new String[args.length / 2][2];
        int port = 0;
        Map<String, String> passedValues = new HashMap<>();
        Map<String, List<Integer>> ports = new HashMap<>();
        int column = 0;
        int raw = 0;

        int counter = 0;

        for (String str : args) {

            if (counter >= 2) {
                counter = 0;
                column++;
                raw = 0;
            }

            values[column][raw] = str;
            raw++;
            counter++;
        }


        for (int i = 0; i < values.length; i++) {
            String keyValue = values[i][0];
            String value = values[i][1];
            switch (keyValue) {
                case "-tcpport" -> {
                    port = Integer.parseInt(value);

                }
                case "-record" -> {

                    String[] splitValue = value.split(":");
                    String key = splitValue[0];
                    String record = splitValue[1];

                    passedValues.put(key, record);
                }
                case "-connect" -> {

                    String[] splitPorts = value.split(":");
                    String key = splitPorts[0];
                    int newPort = Integer.parseInt(splitPorts[1]);


                    if (ports.containsKey(key)) {
                        List<Integer> list = ports.get(key);
                        if (!list.contains(newPort)) {
                            list.add(newPort);
                            ports.get(key).add(newPort);
                            System.out.println("already existing list of ports -> " + key + ":" +newPort);
                        }

                    } else {
                        List<Integer> list = new ArrayList<>();
                        list.add(newPort);
                        ports.put(key, list);

                        System.out.println("new list of ports -> " + key + ":" +newPort);
                    }

                    System.out.println("=====================================");
                    System.out.println(key + "   " + port);
                    try {
                        Socket clientSocket = new Socket(key, newPort);
                        InputStream is = clientSocket.getInputStream();
                        OutputStream os = clientSocket.getOutputStream();
                        OutputStreamWriter osw = new OutputStreamWriter(os);
                        BufferedWriter bufferedWriter = new BufferedWriter(osw);

                        bufferedWriter.write(openingFlag + ":" + 1);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();

                        bufferedWriter.write("connect " + key + ":" + port);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                default -> {
                    System.out.println("wrong commend in programs arguments");
                }
            }
        }


        for (String key : ports.keySet()) {
            System.out.println(key + " -> " + ports.get(key));
        }

        DatabaseNode databaseNode = new DatabaseNode(port, passedValues, ports);
        Thread thread = new Thread(databaseNode);
        thread.start();
    }


    public static void log(String message) {
        System.out.println("[S]: " + message);
        System.out.flush();
    }

    public void deleteConnection(String address, int portToDestroy) {


        System.out.println("deleting\n ============================");
        List<Integer> currentPorts = availblePorts.get(address);
        List<Integer> newPorts = new ArrayList<>();

        for (Integer port : currentPorts){
            if (port != portToDestroy){
                newPorts.add(port);
            }
        }
        availblePorts.put(address,newPorts);
        for (String s : availblePorts.keySet()){
            System.out.println( s + " : " + availblePorts.get(s));
        }

    }

    public void addPort(String key, int newPort) {

        if (availblePorts.containsKey(key)) {
            List<Integer> list = availblePorts.get(key);
            if (!list.contains(newPort)) {
                list.add(newPort);
                availblePorts.get(key).add(newPort);
            }

        } else {
            List<Integer> list = new ArrayList<>();
            list.add(newPort);
            availblePorts.put(key, list);
        }

        for (String s : availblePorts.keySet()) {
            System.out.println(s + ":" + availblePorts.get(s));
        }
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
                    System.out.println("can run : " + canRun);
                }

            }

            welcomeSocket.close();
        } catch (IOException e) {


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