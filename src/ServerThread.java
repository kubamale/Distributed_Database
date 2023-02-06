import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ServerThread extends Thread {
    private final Map<String, List<Integer>> availblePorts;
    private String myServerName;
    private String currentMin;
    private String currentMax;
    private Map<String, List<Integer>> usedPorts;
    private static final String openingFlag = "1000";

    private static final String noValueMessage = "ERROR";
    private final Map<String, String> values;
    private String id;
    Socket clientSocket;
    private String answer;
    private final DatabaseNode parent;
    private boolean isConnecting;
    private String message;

    public ServerThread(Map<String, List<Integer>> availblePorts, Map<String, String> values, Socket clientSocket, Map<String, List<Integer>> usedPorts, DatabaseNode parent) {
        this.availblePorts = availblePorts;
        this.values = values;
        this.clientSocket = clientSocket;
        this.usedPorts = usedPorts;
        this.parent = parent;
        myServerName = "localhost";
        isConnecting = false;
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = createBufferedReader(clientSocket);
            BufferedWriter bufferedWriter = createBufferedWriter(clientSocket);
            System.out.println("Waiting...");

            String input = bufferedReader.readLine();

            String[] firstMessage = input.split(":");
            String flag = firstMessage[0];
            id = firstMessage[1];
            if (!flag.equals(openingFlag)) {
                Functions.writeToServer(bufferedWriter, "wrong flag access denied");
                return;
            }


            usedPorts = parent.updateUsedPorts(id);

            message = bufferedReader.readLine();
            String[] messageSplit = message.split(" ");
            String method = messageSplit[0];
            answer = noValueMessage;
            switch (method) {
                case "get-value" -> {
                    String key = messageSplit[1];
                    getValue(key);
                }
                case "set-value" -> {
                    String[] values = messageSplit[1].split(":");
                    setValue(values);
                }
                case "find-key" -> {
                    String value = messageSplit[1];
                    findKey(value);
                }
                case "get-min" -> {
                    getMin(messageSplit);

                }
                case "get-max" -> {
                    getMax(messageSplit);
                }
                case "new-record" -> {
                    String[] values = messageSplit[1].split(":");
                    String key = values[0];
                    String value = values[1];
                    addValue(key, value);
                }
                case "terminate" -> {
                    terminate();
                }
                case "destroy" -> {
                    parent.deleteConnection(myServerName, Integer.parseInt(messageSplit[1]));
                    answer = noValueMessage;
                }
                case "connect" -> {
                   connect(messageSplit);
                }
                default -> System.out.println("bad method");
            }

            if (!isConnecting) {
                System.out.println("Sending message back...");
                Functions.writeToServer(bufferedWriter, answer);
                System.out.println("Message sent");
                clientSocket.close();
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void connect(String[] serverData){
        System.out.println("connecting");
        String[] splitValue = serverData[1].split(":");
        String serverName = splitValue[0];
        int port = Integer.parseInt(splitValue[1]);

        Functions.addPort(parent.getAvailblePorts(), serverName, port);
        answer = "connected";

        isConnecting = true;
    }
    private void getMax(String[] receivedMessage) throws IOException {
        if (receivedMessage.length > 1) {
            currentMax = receivedMessage[1];
        }

        for (String key : values.keySet()) {

            if (currentMax == null)
                currentMax = values.get(key);

            int number = Integer.parseInt(values.get(key));
            int maxNumber = Integer.parseInt(currentMax);

            if (number > maxNumber)
                currentMax = number + "";
        }
        message = "get-max " + currentMax;
        answer = currentMax;
        callAnotherServer();
    }
    private void terminate() throws IOException {
        System.out.println("terminate");
        message = "destroy " + clientSocket.getLocalPort();
        callAnotherServer();
        answer = "OK";
        parent.setCanRun(false);

        int port = parent.getPORT();
        Socket socket = new Socket(myServerName, port);
        BufferedWriter writer  = createBufferedWriter(socket);
        Functions.writeToServer(writer, "close");
    }
    private void addValue(String key, String value){
        this.values.put(key, value);

        answer = "OK";
    }
    private void getMin(String[] receivedMessage) throws IOException {
        if (receivedMessage.length > 1) {
            currentMin = receivedMessage[1];
        }

        for (String key : values.keySet()) {

            if (currentMin == null)
                currentMin = values.get(key);


            int number = Integer.parseInt(values.get(key));
            int minNumber = Integer.parseInt(currentMin);

            if (number < minNumber)
                currentMin = number + "";

        }

        message = "get-min " + currentMin;
        answer = currentMin;
        callAnotherServer();
    }
    private void findKey(String value) throws IOException {
        if (values.containsKey(value)) {
            answer = clientSocket.getLocalAddress().toString() + ":" + clientSocket.getLocalPort();
        } else {
            callAnotherServer();
        }
    }

    private void getValue(String key) throws IOException {
        if (values.containsKey(key)) {
            answer = values.get(key);
        } else {
            callAnotherServer();
        }
    }

    private void setValue(String[] data) throws IOException {
        String key = data[0];
        String newValue = data[1];
        if (this.values.containsKey(key)) {
            this.values.put(key, newValue);
            answer = "OK";
        } else {
            callAnotherServer();
        }
    }

    private void callAnotherServer() throws IOException {
        boolean foundMessage = false;
        for (String address : availblePorts.keySet()) {
            for (Integer port : availblePorts.get(address)) {
                if (!usedPorts.get(id).contains(port) && !foundMessage) {

                    Socket newConnection = new Socket(address, port);
                    BufferedWriter bufferedWriter = createBufferedWriter(newConnection);
                    BufferedReader bufferedReader = createBufferedReader(newConnection);
                    String authorization = openingFlag + ":" + id;
                    Functions.writeToServer(bufferedWriter, authorization);

                    System.out.println("Calling another server\nFrom " + clientSocket + " -> " + port);
                    Functions.writeToServer(bufferedWriter, message);
                    Functions.addPort(usedPorts, id, port);

                    System.out.println("Waiting for answer...");
                    answer = bufferedReader.readLine();
                    System.out.println("Answer received");
                    if (!answer.equals(noValueMessage)) {
                        foundMessage = true;
                    }
                    newConnection.close();
                }
            }
        }

        if (answer == null)
            answer = "ERROR";
    }

    private BufferedWriter createBufferedWriter(Socket socket) throws IOException {
        OutputStream nos = socket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(nos);
        return new BufferedWriter(osw);

    }

    private BufferedReader createBufferedReader(Socket socket) throws IOException {
        InputStream nis = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(nis);
        return  new BufferedReader(isr);
    }

}