package cn.guye.bts.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import cn.guye.bitshares.models.Asset;
import cn.guye.bitshares.models.GrapheneObject;
import cn.guye.bitshares.models.ObjectType;

/**
 * Created by nieyu2 on 18/1/15.
 */

public class DataCenter {
    public static final String TYPE_ASSET = "ASSET";

    private Map<ObjectType, Map<String , GrapheneObject>> datas = new HashMap<>(10);

    private ExecutorService poolExecutor;

    public DataCenter() {
        poolExecutor = Executors.newFixedThreadPool(3);
       
    }


    public void addData(GrapheneObject data){
        ObjectType type = data.getObjectType();
        Map<String , GrapheneObject> map = datas.get(type);
        if(map == null){
            map = new HashMap<>();
            datas.put(type,map);
        }
        map.put(data.getObjectId(),data);
    }

    public void updateData(GrapheneObject data){
        ObjectType type = data.getObjectType();
        Map<String , GrapheneObject> map = datas.get(type);
        if(map == null){
            map = new HashMap<>();
            datas.put(type,map);
        }
        map.put(data.getObjectId(),data);
    }

    public void deleteData(GrapheneObject data){
        ObjectType type = data.getObjectType();
        Map<String , GrapheneObject> map = datas.get(type);
        if(map == null){
            map = new HashMap<>();
            datas.put(type,map);
        }
        map.remove(data.getObjectId());
    }

    public GrapheneObject getData(String id){
        ObjectType type = new GrapheneObject(id).getObjectType();
        Map<String , GrapheneObject> map = datas.get(type);
        if(map == null){
            return null;
        }else{
            return map.get(id);
        }
    }

    public void clearAll(){
        datas.clear();
    }

}
