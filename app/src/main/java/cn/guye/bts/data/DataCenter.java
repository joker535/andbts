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


    public interface DataChangeHandler{
        public void onDataChange(int event , GrapheneObject[] data);
    }

    public static final int ADD =  1;
    public static final int UPDATE = 2;
    public static final int DELETE = 3;
    public static final int GET = 4;
    private Map<ObjectType, Map<String , GrapheneObject>> datas = new HashMap<>(10);

    private List<DataChangeHandler> handlers = new ArrayList<>();

    private ExecutorService poolExecutor;

    public DataCenter() {
        poolExecutor = Executors.newFixedThreadPool(3);

    }

    public void addDataHandle(DataChangeHandler handler){
        handlers.add(handler);
    }

    public void addData(List<GrapheneObject> data) {
        Runnable runable = new Runnable(){

            @Override
            public void run() {

                addDataInternal(data);

            }
        };
        poolExecutor.execute(runable);
    }

    public void addData(final GrapheneObject[] data){
        Runnable runable = new Runnable(){

            @Override
            public void run() {
                addDataInternal(data);
            }
        };
        poolExecutor.execute(runable);
    }


    private void addDataInternal(GrapheneObject[] data){
        for (GrapheneObject o:
             data) {
            ObjectType type = o.getObjectType();
            Map<String , GrapheneObject> map = datas.get(type);
            if(map == null){
                map = new HashMap<>();
                datas.put(type,map);
            }
            map.put(o.getObjectId(),o);
        }

        postNotify(ADD ,data);
    }

    private void addDataInternal(List<GrapheneObject> data){
        for (GrapheneObject o:
                data) {
            ObjectType type = o.getObjectType();
            Map<String , GrapheneObject> map = datas.get(type);
            if(map == null){
                map = new HashMap<>();
                datas.put(type,map);
            }
            map.put(o.getObjectId(),o);
        }

        postNotify(ADD ,data.toArray(new GrapheneObject[]{}));
    }

    private void postNotify(final int add,final GrapheneObject[] data) {
        Runnable runable = new Runnable(){

            @Override
            public void run() {
                for (DataChangeHandler dc:
                     handlers) {
                    dc.onDataChange(add,data);
                }
            }
        };
        poolExecutor.execute(runable);
    }


    public GrapheneObject getDataSync(String id){
        ObjectType type = new GrapheneObject(id).getObjectType();
        Map<String , GrapheneObject> map = datas.get(type);
        if(map == null){
            return null;
        }else{
            return map.get(id);
        }
    }

    public String getData(final String id){
        Runnable runable = new Runnable(){

            @Override
            public void run() {
                GrapheneObject o = getDataSync(id);
                for (DataChangeHandler dc:
                        handlers) {
                    dc.onDataChange(GET,new GrapheneObject[]{o});
                }
            }
        };
        poolExecutor.execute(runable);
        return id;
    }


    private void clearAll(){
        datas.clear();
    }

}
