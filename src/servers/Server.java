package servers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    //端口
    public static final int PORT=6644;
    //线程池最大值
    public static final int THREAD_MAX=20;
    public static void main(String[] args) throws IOException {
        //线程池创建
        ExecutorService threadPool= Executors.newFixedThreadPool(THREAD_MAX);
        //建立服务器端监听socket，绑定监听端口
        ServerSocket server_socket = new ServerSocket(PORT);
        while(true){
            //监听客户端的请求
            Socket client_socket = server_socket.accept();
            //new Thread(new Task(client_socket)).start();
            threadPool.execute(new Task(client_socket));//使用线程池
        }
    }
}
