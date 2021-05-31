package common;

import java.io.Serializable;

public class Response implements Serializable {
    public static final long serialVersionUID= -9157065447414217058L;
    //响应类型
    private Action action;

    public Response(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
