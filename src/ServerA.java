import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerA extends Thread {
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


    public ServerA(Map<String, List<Integer>> availblePorts, Map<String, String> values, Socket clientSocket, Map<String, List<Integer>> usedPorts, DatabaseNode parent) {
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
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            InputStreamReader isr = new InputStreamReader(is);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedReader bufferedReader = new BufferedReader(isr);
            BufferedWriter bufferedWriter = new BufferedWriter(osw);
            System.out.println("czekam");

            String input = bufferedReader.readLine();
            String[] firstMessage = input.split(":");
            String flag = firstMessage[0];
            id = firstMessage[1];
            if (!flag.equals(openingFlag)) {
                bufferedWriter.write("wrong flag access denied");
                bufferedWriter.newLine();
                bufferedWriter.close();
                return;
            }


            usedPorts = parent.updateUsedPorts(id);

            String message = bufferedReader.readLine();
            String[] messageSplit = message.split(" ");

            String method = messageSplit[0];
            answer = noValueMessage;
            switch (method) {
                case "get-value" -> {
                    String value = messageSplit[1];
                    if (values.containsKey(value)) {
                        answer = values.get(value);
                    } else {
                        callAnotherServer(message);
                    }
                }
                case "set-value" -> {
                    String[] values = messageSplit[1].split(":");
                    String key = values[0];
                    String newValue = values[1];
                    if (this.values.containsKey(key)) {
                        this.values.put(key, newValue);
                        answer = "OK";
                    } else {
                        callAnotherServer(message);
                    }
                }
                case "find-key" -> {
                    String value = messageSplit[1];
                    if (values.containsKey(value)) {
                        answer = clientSocket.getLocalAddress().toString() + ":" + clientSocket.getLocalPort();
                    } else {
                        callAnotherServer(message);
                    }
                }
                case "get-min" -> {

                    if (messageSplit.length > 1) {
                        currentMin = messageSplit[1];
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
                    callAnotherServer(message);

                }
                case "get-max" -> {

                    if (messageSplit.length > 1) {
                        currentMax = messageSplit[1];
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
                    callAnotherServer(message);
                }
                case "new-record" -> {
                    String[] values = messageSplit[1].split(":");
                    String key = values[0];
                    String value = values[1];
                    System.out.println(key + ":" + value);
                    this.values.put(key, value);

                    answer = "OK";
                }
                case "terminate" -> {
                    System.out.println("terminate");
                    message = "destroy " + clientSocket.getLocalPort();
                    callAnotherServer(message);
                    answer = "OK";
                    parent.setCanRun(false);

                    int port = parent.getPORT();
                    Socket socket = new Socket(myServerName, port);
                    OutputStream osn = socket.getOutputStream();
                    OutputStreamWriter oswn = new OutputStreamWriter(osn);
                    BufferedWriter bufferedWritern = new BufferedWriter(oswn);

                    bufferedWritern.write("close");

                }
                case "destroy" -> {
                    System.out.println("destroy");
                    parent.deleteConnection(myServerName, Integer.parseInt(messageSplit[1]));

                    answer = "OK";
                }
                case "connect" -> {
                    System.out.println("connecting");
                    String[] splitValue = messageSplit[1].split(":");
                    String serverName = splitValue[0];
                    int port = Integer.parseInt(splitValue[1]);

                    parent.addPort(serverName, port);
                    answer = "connected";

                    isConnecting = false;
                }
                default -> System.out.println("bad method");
            }

            if (!isConnecting) {
                System.out.println("odpisuje");
                bufferedWriter.write(answer);
                bufferedWriter.newLine();
                bufferedWriter.flush();
                System.out.println("skonczyłem odpisywac");
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void callAnotherServer(String message) throws IOException {
        boolean foundMessage = false;
        for (String address : availblePorts.keySet()) {
            for (Integer port : availblePorts.get(address)) {
                if (!usedPorts.get(id).contains(port) && !foundMessage) {

                    Socket newConnection = new Socket(address, port);
                    InputStream nis = newConnection.getInputStream();
                    OutputStream nos = newConnection.getOutputStream();
                    InputStreamReader nisr = new InputStreamReader(nis);
                    OutputStreamWriter nosw = new OutputStreamWriter(nos);
                    BufferedReader nbufferedReader = new BufferedReader(nisr);
                    BufferedWriter nbufferedWriter = new BufferedWriter(nosw);

                    nbufferedWriter.write(openingFlag + ":" + id);
                    nbufferedWriter.newLine();
                    nbufferedWriter.flush();

                    System.out.println(clientSocket + " -> " + port);
                    nbufferedWriter.write(message);
                    nbufferedWriter.newLine();
                    nbufferedWriter.flush();
                    if (!usedPorts.containsKey(id)) {
                        List<Integer> l = new ArrayList<>();
                        l.add(port);
                        usedPorts.put(id, l);
                    } else {
                        if (!usedPorts.get(id).contains(port)) {
                            List<Integer> l = usedPorts.get(id);
                            l.add(port);
                            usedPorts.put(id, l);
                        }
                    }
                    System.out.println("czekam na odpowiedz");
                    answer = nbufferedReader.readLine();
                    System.out.println("odebrałem");
                    if (!answer.equals(noValueMessage)) {
                        foundMessage = true;
                    }
                }
            }
        }

        if (answer == null)
            answer = "ERROR";
    }
}