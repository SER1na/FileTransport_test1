package Client;

import common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String ip_adress = "127.0.0.1";
    private int port = 6644;

    private Socket client_socket;        //创建客户端Socket

    public Client() throws IOException, ClassNotFoundException {
        System.out.println("测试输入：登录/注册/发送文件/exit：");


        client_socket = new Socket(InetAddress.getLocalHost(), port);//使用IP，端口连接服务器
        Scanner sss = new Scanner(System.in);
        String s= sss.next();
        while(true) {
            System.out.println("测试输入：登录/注册/发送文件/exit：");


            //测试代码
            if(s.equals("登录")) {
                System.out.println("输入id：");
                Scanner input = new Scanner(System.in);
                int id = input.nextInt();
                System.out.println("输入密码：");
                String password = input.next();


                //登录
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
                if(password1.equals(password2)) register(client_socket, id, password1);
                else System.out.println("两次密码不匹配.");

            }
            if (s.equals("发送文件")){
                int teacherid=123; //要发给哪位老师
                //if(sendFileRequestWithRes(client_socket).getAction()==Action.READYTOSENDFILE)
                sendFile(client_socket,teacherid); //开始发送文件

            }
            if (s.equals("exit")){
                System.out.println("退出。");
                client_socket.close();
                break;
            }
            ObjectInputStream ois = new ObjectInputStream(client_socket.getInputStream());
            Request server_req=(Request) ois.readObject();
            System.out.println("读取服务器请求。");
            if(server_req.getAction()==Action.SENDFILETOTEACHER){
                System.out.println("准备接收文件。");
                reciveFileByTeacher(client_socket);
            }

        }
    }
    //发送文件请求包并接收一个响应
    private static Response sendFileRequestWithRes(Socket client_socket){
        Response res=new Response();
        //res.setAction();
        Request request=new Request();

        request.setAction(Action.SENDFILE);//设置请求的动作
        try {
            OutputStream os = client_socket.getOutputStream();
            ObjectOutputStream oos= new ObjectOutputStream(os);
            oos.writeObject(request);
            System.out.println("0000");
            InputStream is= client_socket.getInputStream();
            ObjectInputStream ois=new ObjectInputStream(is);
            res=(Response)ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }
    //发送文件传输完毕包，不接收响应
    private static void sendRequest(Socket client_socket){

    Request request=new Request();
    request.setAction(Action.SENDFINISHED);
        try {
            OutputStream os = client_socket.getOutputStream();
            ObjectOutputStream oos= new ObjectOutputStream(os);
            oos.writeObject(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


/* 学生给老师发送文件：
        先封装文件头数据包发送，得到响应后开始发送文件 */
    private static void sendFile(Socket client_socket,int teacherid) throws IOException {
        OutputStream os = client_socket.getOutputStream();
        ObjectOutputStream oos= new ObjectOutputStream(os);
        //封装请求对象
        Request sendf=new Request();
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
        //System.out.println(nameIn);
        if(!fileLast.equals(nameIn)){
            //服务器无响应，结束输出，并结束当前线程
            client_socket.close();
            System.exit(1);
        }

        System.out.println("收到来自服务器的响应。开始传输文件。");
        //如果收到正确响应，则开始发送文件
        FileInputStream fr = new FileInputStream(file);
        System.out.println("文件大小："+String.valueOf(fr.available()/1000)+"k");
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

        //接收服务器的响应文件上传成功...




        //可以先不关闭socket连接
        //client_socket.close();
    }
    //从服务器接收文件
    public static void reciveFileByTeacher(Socket client_socket) throws IOException {

        InputStream in = client_socket.getInputStream();
        byte[] names = new byte[100];
        int len = in.read(names);
        String fileName = new String(names, 0, len);
        String[] fileNames = fileName.split("\\.");
        String fileLast = fileNames[fileNames.length-1];

        //新建目录
        File dir = new File("E:\\test\\FilesFromServer");
        if(!dir.exists())
            dir.mkdirs();
        //新建文件

        File file = new File(dir,fileNames[0]+"."+fileLast);
        FileOutputStream fos = new FileOutputStream(file);
        //将Socket输入流中的信息读入到文件
        byte[] bufIn = new byte[1024];
        while(( len = in.read(bufIn))!=-1){
            fos.write(bufIn, 0, len);
        }
        fos.close();

    }
    //注册接口
    public static void register(Socket client_socket,int id,String password) throws IOException, ClassNotFoundException {
        OutputStream os = client_socket.getOutputStream();
        ObjectOutputStream oos= new ObjectOutputStream(os);

        //封装请求对象
        User user=new User(id,password);
        Request request=new Request();
        request.setAction(Action.REGISTER);
        request.getDataMap().put("user",user);
        //对象序列化写出
        oos.writeObject(request);

        InputStream is= client_socket.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(is);
        //获取相应响应
        Response response = (Response) ois.readObject();
        if (response.getAction().equals(Action.REGISTERSUCCESS)) {
            System.out.println("注册成功！");
        }
    }
    public static void login(Socket client_socket,int id,String password) throws IOException, ClassNotFoundException {
    //发送请求包
        //获得输出流
        OutputStream os = client_socket.getOutputStream();
        ObjectOutputStream oos= new ObjectOutputStream(os);
        //用户信息
        User user=new User(id,password);
        //封装登录的请求对象
        Request request=new Request();
        request.setAction(Action.LOGIN);
        request.getDataMap().put("user",user);
        //发送
        oos.writeObject(request);

        //获取输入流
        InputStream is= client_socket.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(is);
        //接收响应
        Response response = (Response) ois.readObject();
            if (response.getAction()==Action.LOGINSUCCESS) {
                System.out.println("登录成功！");
            }

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        new Client();
  }


}
