import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SocketServer {
    ServerSocket server;
    Socket sk;
    InetAddress addr;
    FileWriter fr = new FileWriter("log.txt");
    
    ArrayList<ServerThread> list = new ArrayList<ServerThread>();

    public SocketServer() throws IOException {
        try {
        	addr = InetAddress.getByName("127.0.0.1");
        	//addr = InetAddress.getByName("192.168.43.1");
            
        	server = new ServerSocket(1234,50,addr);
            System.out.println("\n Waiting for Client connection");
            SocketClient.main(null);
            while(true) {
                sk = server.accept();
                System.out.println(sk.getInetAddress() + " connect");

                //Thread connected clients to ArrayList
                ServerThread st = new ServerThread(this);
                addThread(st);
                st.start();
            }
        } catch(IOException e) {
            System.out.println(e + "-> ServerSocket failed");
        }
    }

    public void addThread(ServerThread st) {
        list.add(st);
    }

    public void removeThread(ServerThread st){
        try {
            fr.append("Logout: ").append(st.getUserName()).append("\n");
            fr.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        list.remove(st); //remove
    }

    public void broadCast(String message){
        try {
            fr.append(message).append("\n");
            fr.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for(ServerThread st : list){
            st.pw.println(message);
        }
    }

    public String userList(){
        StringBuilder sb = new StringBuilder();
        for(ServerThread s: list){
            sb.append(s.getUserName()).append("\n");
        }
        return String.valueOf(sb);
    }

    public static void main(String[] args) {
        try {
            new SocketServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

class ServerThread extends Thread {
    SocketServer server;
    PrintWriter pw;
    String name;

    public ServerThread(SocketServer server) {
        this.server = server;
    }

    public String getUserName(){
        return name;
    }


    @Override
    public void run() {
        try {
            // read
            BufferedReader br = new BufferedReader(new InputStreamReader(server.sk.getInputStream()));

            // writing
            pw = new PrintWriter(server.sk.getOutputStream(), true);
            name = br.readLine();
            server.broadCast("**["+name+"] Entered**");
            //log.append("**[").append(name).append("] Entered**\n");


            String data;
            while((data = br.readLine()) != null ){
                if(data.equals("/list")){
                    pw.println(server.userList());
                } else if (data.equals("/sports")) {
                    if(Math.random() > .5){
                        pw.println("Your team won");
                    }else{
                        pw.println("Your team lost... again");
                    }

                } else if(data.equals("/stock")){
                    if(Math.random() > .5){
                        pw.println("The dow fell " + (int) (Math.random() * (20-5) - 5) + " points");
                    }else{
                        pw.println("The dow rose " + (int) (Math.random() * (20-5) - 5) + " points");
                    }
                } else if(data.equals("/weather")){
                    pw.println("look out a window");
                }else{
                    server.broadCast("["+name+"] "+ data);
                }

                //log.append("**[").append(name).append("] ").append(data).append("\n");
            }

        } catch (Exception e) {
            //Remove the current thread from the ArrayList.
            server.removeThread(this);
            server.broadCast("**["+name+"] Left**");
            System.out.println(server.sk.getInetAddress()+" - ["+name+"] Exit");
            System.out.println(e + "---->");
        }
    }
}