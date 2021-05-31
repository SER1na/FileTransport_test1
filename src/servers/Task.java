package servers;

import common.Action;
import common.Response;
import common.User;
import common.Util;

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
                User user=(User) ois.readObject();
                System.out.print("Server读取了客户端的请求,客户端的IP是:");
                System.out.println(getIP(client_socket));
                System.out.println("当前请求由线程 "+Thread.currentThread()+"处理中。");

                if(user.getAction()==Action.LOGIN){
                    System.out.println("正在登录。。。");
                    if(login(client_socket,user)){
                        System.out.println("用户"+user.getId()+"登录成功,ip:"+getIP(client_socket));
                    }
                }
                if(user.getAction().equals(Action.REGISTER)){
                    System.out.println("正在注册。。。");
                    if(register(client_socket,user)){
                        System.out.println("用户"+user.getId()+"注册成功,ip:"+getIP(client_socket));
                    }
                }
                if(user.getAction().equals(Action.SENDFILE)){
                    System.out.println("正在接收文件：");
                    recFile(client_socket);
                }
            }


        } catch (IOException | ClassNotFoundException e) {
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
            System.out.println("目标老师id"+teacherid);

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
            client_socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private boolean register(Socket client_socket, User user) {
        try {
            //数据库注册接口
            if(user.getPassword().length()>2){
            User registerRes=new User(user.getId(), user.getPassword());
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

    public boolean login(Socket client_socket,User user) {
        try {
            //数据库登录接口
            if(user.getId()==123 && user.getPassword().equals("123")){
                System.out.println("登录成功.");
                //响应
                User loginres=new User(user.getId(), user.getPassword());
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
