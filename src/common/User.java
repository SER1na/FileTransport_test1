package common;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID= 869464708392966405L;
    //记录用户请求的动作，以及信息

    private int id;
    private String password;

    public User(int id, String password){
        this.id=id;
        this.password=password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
