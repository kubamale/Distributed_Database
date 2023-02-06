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

        //Setting up Writer and Reader for server
        InetAddress IP = InetAddress.getByName(serverName);
        Socket clientSocket = new Socket(IP, serverPot);
        OutputStream os = clientSocket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bufferedWriter = new BufferedWriter(osw);
        InputStream inputStream = clientSocket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        LocalDateTime messageId = LocalDateTime.now();

        //combine opening flag with message id
        openingFlag += ":" + messageId.toString().replace(':', '.');

        Functions.writeToServer(bufferedWriter, openingFlag);
        Functions.writeToServer(bufferedWriter, operation);

        String answer = bufferedReader.readLine();

        System.out.println(answer);

        clientSocket.close();
    }


}
