import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Functions {

    public static void addPort(Map<String, List<Integer>> serverData, String serverName, int port){
        if (!serverData.containsKey(serverName)) {
            List<Integer> newList = new ArrayList<>();
            newList.add(port);
            serverData.put(serverName, newList);
        } else {
            if (!serverData.get(serverName).contains(port)) {
                List<Integer> newList = serverData.get(serverName);
                newList.add(port);
                serverData.put(serverName, newList);
            }
        }
    }

    public static BufferedWriter createBufferedWriter(Socket socket) throws IOException {
        OutputStream nos = socket.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(nos);
        return new BufferedWriter(osw);

    }

    public static BufferedReader createBufferedReader(Socket socket) throws IOException {
        InputStream nis = socket.getInputStream();
        InputStreamReader isr = new InputStreamReader(nis);
        return  new BufferedReader(isr);
    }

    public static void writeToServer(BufferedWriter writer, String message){
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
