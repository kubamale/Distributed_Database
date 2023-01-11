import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class DatabaseClient {

    private static String openingFlag = "1000";

    public static void main(String[] args) throws IOException {

        String connectionData = args[1];
        String operation = "";

        String[] splitConnectionData = connectionData.split(":");
        String serverName = splitConnectionData[0];
        int serverPot = Integer.parseInt(splitConnectionData[1]);




        for (int i = 3; i < args.length; i++) {
            operation += args[i] + " ";
        }

        operation = operation.trim();


        InetAddress IP = InetAddress.getByName(serverName);
        Socket clientSocket = new Socket(IP, serverPot);
        OutputStream os = clientSocket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bufferedWriter = new BufferedWriter(osw);
        InputStream inputStream = clientSocket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime messageId = LocalDateTime.now();

        openingFlag += ":" + messageId.toString().replace(':', '.');

        writeToServer(bufferedWriter, openingFlag);
        writeToServer(bufferedWriter, operation);

        String answer = bufferedReader.readLine();

        System.out.println(answer);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        clientSocket.close();
    }

    private static void writeToServer (BufferedWriter bufferedWriter, String message) throws IOException {
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
}
