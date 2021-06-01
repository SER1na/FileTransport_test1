package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    //数据标识
    public static final long serialVersionUID = 5639807981022728229L;
    //请求动作
    private Action action;
    //请求包数据
    private Map<String,Object> dataMap;

    public Request(){
        this.dataMap = new HashMap<String,Object>();
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }
    //获取对象
    public Object getAttribute(String name){
        return this.dataMap.get(name);
    }
}
