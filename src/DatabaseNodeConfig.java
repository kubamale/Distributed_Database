import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseNodeConfig {
    private static final String openingFlag = "1000";
    Map<String, String> records = new HashMap<>();
    Map<String, List<Integer>> ports = new HashMap<>();
    private int port;
    public DatabaseNodeConfig(String[] args) throws IOException {
        String[][] nodesInfo = getNodesInfo(args);
        port = 0;

        for (String[] info : nodesInfo) {
            String keyValue = info[0];
            String value = info[1];
            switch (keyValue) {
                case "-tcpport" -> {
                    port = Integer.parseInt(value);
                }
                case "-record" -> {
                    addValues(value);
                }
                case "-connect" -> {
                    String[] serverInfo = value.split(":");
                    String serverName = serverInfo[0];
                    int port = Integer.parseInt(serverInfo[1]);
                    Functions.addPort(ports, serverName, port);

                    Socket clientSocket = new Socket(serverName, port);
                    BufferedWriter bufferedWriter = Functions.createBufferedWriter(clientSocket);
                    String message = openingFlag + ":" + 1;

                    Functions.writeToServer(bufferedWriter, message);

                    Functions.writeToServer(bufferedWriter, "connect localhost:" + port);
                }
                default -> {
                    System.out.println("wrong commend in programs arguments");
                }
            }
        }

        DatabaseNode databaseNode = new DatabaseNode(port, records, ports);
        Thread thread = new Thread(databaseNode);
        thread.start();
    }
    private static String[][] getNodesInfo(String[] args){
        String[][] info = new String[args.length / 2][2];
        int column = 0;
        int raw = 0;

        int counter = 0;
        for (String str : args) {

            if (counter >= 2) {
                counter = 0;
                column++;
                raw = 0;
            }

            info[column][raw] = str;
            raw++;
            counter++;
        }
        return info;
    }

    private void addValues( String entryValue){
        String[] splitValue = entryValue.split(":");
        String key = splitValue[0];
        String record = splitValue[1];

        records.put(key, record);
    }
}
