package Client;

import common.Action;
import common.Response;
import common.User;
import common.Util;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private String ip_adress = "127.0.0.1";
    private int port = 6644;

    public Client() throws IOException, ClassNotFoundException {

        //建立客户端Socket
        Socket client_socket = new Socket(InetAddress.getLocalHost(), port);//修改为服务器IP地址
        Scanner sss = new Scanner(System.in);
        String s=new String();
        System.out.println("（外部接口）登录/注册/发送文件：");
        while((s=sss.next())!="-1") {
            //测试代码
            if(s.equals("登录")) {
                System.out.println("输入id：");
                Scanner input = new Scanner(System.in);
                int id = input.nextInt();
                System.out.println("输入密码：");
                String password = input.next();
                //登录接口
                login(client_socket, id, password);
            }
            //测试代码
            if(s.equals("注册")){
                System.out.println("输入您的id：");
                Scanner input = new Scanner(System.in);
                int id = input.nextInt();
                System.out.println("输入密码：");
                String password1 = input.next();
                System.out.println("重复密码：");
                String password2 = input.next();
                //注册接口
                if(password1.equals(password2))register(client_socket, id, password1);
                else System.out.println("两次密码不匹配.");

            }
            if (s.equals("发送文件")){
                int teacherid=123; //需要获取老师的id
                sendFile(client_socket,teacherid);
            }
            System.out.println("输入操作：");
        }
    }
    /* 文件传输协议 先封装文件头发送，得到服务器的响应后建立文件数据的传输 */
    private static void sendFile(Socket client_socket,int teacherid) throws IOException {
        OutputStream os = client_socket.getOutputStream();
        ObjectOutputStream oos= new ObjectOutputStream(os);
        //封装请求对象
        User sendf=new User(0,"0");
        sendf.setAction(Action.SENDFILE);
        //对象序列化传输建立连接
        oos.writeObject(sendf);
        OutputStream out = client_socket.getOutputStream();
        //把文件接收者的id传过去
        byte [] id= Util.intToBytes(teacherid);
        out.write(id);

        //测试代码 获取要发送的文件
        System.out.println("传输的文件路径:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String inputpath = br.readLine();

        File file = new File(inputpath);//读取文件
        String name = file.getName();//获取文件完整名
        String[] fileName = name.split("\\.");//将文件名按照.来分割
        String fileLast = fileName[fileName.length-1];//获取后缀名
        //写入信息到输出流
        out.write(name.getBytes());
        //读取服务端的反馈信息
        InputStream in = client_socket.getInputStream();
        byte[] names = new byte[100];
        int len = in.read(names);
        String nameIn = new String(names, 0, len);
        System.out.println(nameIn);
        if(!fileLast.equals(nameIn)){
            //结束输出，并结束当前线程
            client_socket.close();
            System.exit(1);
        }
        //如果正确，则发送文件信息

        //读取文件信息
        FileInputStream fr = new FileInputStream(file);
        //发送文件信息
        byte[] buf = new byte[1024];
        int data_len=0;
        //读取文件数据
        while((data_len=fr.read(buf))!=-1){
            //发送给服务器
            out.write(buf,0,data_len);
        }
        //关流
        out.close();
        fr.close();
        //可以先不关闭socket连接
        client_socket.close();
    }

    //注册接口
    public static void register(Socket client_socket,int id,String password) throws IOException, ClassNotFoundException {
        OutputStream os = client_socket.getOutputStream();
        ObjectOutputStream oos= new ObjectOutputStream(os);

        //封装请求对象
        User user=new User(id,password);
        user.setAction(Action.REGISTER);
        //对象序列化写出
        oos.writeObject(user);

        InputStream is= client_socket.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(is);
        //获取相应响应
        User response = (User) ois.readObject();
        if (response.getAction().equals(Action.REGISTERSUCCESS)) {
            System.out.println("注册成功！");
        }
    }

    //登录接口
    public static void login(Socket client_socket,int id,String password) throws IOException, ClassNotFoundException {

        //获得输出流
        OutputStream os = client_socket.getOutputStream();
        ObjectOutputStream oos= new ObjectOutputStream(os);

        //封装请求对象
        User user=new User(id,password);
        user.setAction(Action.LOGIN);
        //对象序列化写出
        oos.writeObject(user);

        InputStream is= client_socket.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(is);
            //获取相应响应
            User response = (User) ois.readObject();
            if (response.getAction()==Action.LOGINSUCCESS) {
                System.out.println("登录成功！");
            }



    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new Client();
  }

}
