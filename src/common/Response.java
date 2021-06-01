package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Response implements Serializable {
    //数据标识
    public static final long serialVersionUID= -9157065447414217058L;
    //响应动作
    private Action action;
    //响应数据
    private Map<String,Object> dataMap;


    public Response(){
        this.dataMap = new HashMap<String,Object>();
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
