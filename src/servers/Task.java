package servers;

import common.*;

import java.io.*;
import java.net.Socket;

public class Task implements Runnable {
    private Socket client_socket;
    public Task(Socket socket){
        this.client_socket = socket;
    }
    @Override
    public void run() {
        try {
            ObjectInputStream ois=new ObjectInputStream(client_socket.getInputStream());
            boolean flag=true;
            while (flag){
                Request user_req=(Request) ois.readObject();
                System.out.print("Server读取了客户端的请求,客户端的IP是:");
                System.out.println(getIP(client_socket));
                System.out.println("当前请求由线程 "+Thread.currentThread()+"处理中。");

                if(user_req.getAction()==Action.LOGIN){
                    System.out.println("正在登录。。。");
                    if(login(client_socket,user_req)){
                        System.out.println("用户"+user_req.getAttribute("user")+"登录成功,ip:"+getIP(client_socket));
                    }
                }
                if(user_req.getAction().equals(Action.REGISTER)){
                    System.out.println("正在注册。。。");
                    if(register(client_socket,user_req)){
                        System.out.println("用户"+user_req.getAttribute("user")+"注册成功,ip:"+getIP(client_socket));
                    }
                }
                if(user_req.getAction().equals(Action.SENDFILE)){
                    System.out.println("收到来自学生的文件发送请求,学生ip："+getIP(client_socket));
                    //sendResponseToStudent(client_socket);
                    recFile(client_socket);
                }
            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    //准备接收学生的文件
    private void sendResponseToStudent(Socket client_socket){
        Response response=new Response();
        response.setAction(Action.READYTOSENDFILE);
        try {
            OutputStream os=new ObjectOutputStream(client_socket.getOutputStream());
            ObjectOutputStream oos=new ObjectOutputStream(os);
            oos.writeObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void recFile(Socket client_socket) {
        String studentip = client_socket.getInetAddress().getHostAddress();

        try{
            //获取客户端输入流
            InputStream in = client_socket.getInputStream();
            //读取目的id
            byte[] teacherids = new byte[100];
            int d = in.read(teacherids);
            int teacherid = Util.bytesToInt(teacherids);
            System.out.println("学生要发送文件给老师(id="+teacherid+")");

            //读取信息
            byte[] names = new byte[100];
            int len = in.read(names);
            String fileName = new String(names, 0, len);
            String[] fileNames = fileName.split("\\.");
            String fileLast = fileNames[fileNames.length-1];
            //然后将后缀名发给客户端
            OutputStream out = client_socket.getOutputStream();
            out.write(fileLast.getBytes());
            //新建目录
            File dir = new File("E:\\test\\"+studentip);
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

            //服务器暂存文件


            //转发给指定老师端

            //Socket dest = Server.userSocketMap.get(new String(teacherids)); //获取连接
            Socket dest = Server.userSocketMap.get("127.0.0.1"); //


                OutputStream d_os = dest.getOutputStream();
                ObjectOutputStream d_oos= new ObjectOutputStream(d_os);
                //封装请求对象
                Request sendf=new Request();
                sendf.setAction(Action.SENDFILETOTEACHER);
                //对象序列化传输建立连接
                d_oos.writeObject(sendf);


                File sendfile = new File("E:\\test\\"+studentip+"\\"+fileName);//读取文件
            String fname = sendfile.getName();//获取文件完整名
            String[] sendfileName = fname.split("\\.");//将文件名按照.来分割
            String sendfileLast = sendfileName[sendfileName.length-1];//获取后缀名
            //写入信息到输出流
            out.write(fname.getBytes());


                FileInputStream frToTeacher = new FileInputStream(sendfile);
                System.out.println("文件大小："+String.valueOf(frToTeacher.available()/1000)+"k");
                //发送文件信息
                byte[] buf = new byte[1024];
                int data_len=0;
                //读取文件数据
                while((data_len=frToTeacher.read(buf))!=-1){
                    //发送给服务器
                    d_os.write(buf,0,data_len);
                }
                //关流
            d_os.close();
            frToTeacher.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private boolean register(Socket client_socket, Request request) {
        try {
            User user = (User) request.getAttribute("user");

            //数据库注册接口
            if(user.getPassword().length()>2){
            Response registerRes=new Response();
            registerRes.setAction(Action.REGISTERSUCCESS);
            ObjectOutputStream oos=new ObjectOutputStream(client_socket.getOutputStream());
            oos.writeObject(registerRes);
            System.out.println("响应已发送.");
            return true;

        }
    } catch (IOException e) {
        e.printStackTrace();
    }

        return false;
    }

    public boolean login(Socket client_socket,Request request) {
        try {
            //获取用户信息
            User user = (User) request.getAttribute("user");
            //检查用户数据
            if(user.getId()==123 && user.getPassword().equals("123")){
                System.out.println("登录成功.");
                //给客户端反馈
                Response loginres=new Response();
                loginres.setAction(Action.LOGINSUCCESS);
                ObjectOutputStream oos=new ObjectOutputStream(client_socket.getOutputStream());
                oos.writeObject(loginres);
                System.out.println("响应已发送.");
                return true;
            }
            else {
                //登录失败
                return false;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


        return false;

    }

    public static String getIP(Socket socket){
            return socket.getInetAddress().getHostAddress();
    }
}
