import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPmultiServer_threads implements Runnable {
    protected int          serverPort   = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected ExecutorService threadPool =
            Executors.newFixedThreadPool(10);

    public TCPmultiServer_threads(int port){
        this.serverPort = port;
    }

    public static void main(String args[]) throws IOException {
        TCPmultiServer_threads server = new TCPmultiServer_threads(3333);
        new Thread(server).start();

        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping Server");
        server.stop();
    }

    @Override
    public void run() {
            synchronized(this){
                this.runningThread = Thread.currentThread();
            }
            openServerSocket();
            String query;
            while(!isStopped()){
                Socket clientSocket = null;
                try {
                    clientSocket = this.serverSocket.accept();
                    System.out.println("Got connection from " + clientSocket.getInetAddress());
                    BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);    //autoflush=true
                    query = input.readLine();
                    out.println(query);
                } catch (IOException e) {
                    if(isStopped()) {
                        System.out.println("Server Stopped.") ;
                        break;
                    }
                    throw new RuntimeException(
                            "Error accepting client connection", e);
                }
                this.threadPool.execute(
                        new WorkerRunnable(clientSocket,
                                "Thread Pooled Server"));
            }
            this.threadPool.shutdown();
            System.out.println("Server Stopped.") ;
    }
    
    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }


}