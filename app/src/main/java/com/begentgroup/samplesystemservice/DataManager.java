package com.begentgroup.samplesystemservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2016-08-17.
 */
public class DataManager {

    List<MyData> items = new ArrayList<>();
    private static long idGlobal = 1;

    private static DataManager instance;
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private DataManager() {

    }

    public MyData getAlarmData() {
        if (items.size() > 0) {
            sortMyData();
            return items.get(0);
        }
        return null;
    }

    public void addMyData(MyData data) {
        if (data.id == -1) {
            data.id = idGlobal++;
            items.add(data);
        }
    }

    public void removeMyData(MyData data) {
        items.remove(data);
    }

    public List<MyData> listProcessData(long time) {
        List<MyData> list = new ArrayList<>();
        sortMyData();
        for (int i = 0 ; i < items.size(); i++) {
            MyData d = items.get(i);
            if (d.time <= time) {
                list.add(d);
            } else {
                break;
            }
        }
        return list;
    }

    public void updateMyData(MyData data) {
        int index = items.indexOf(data);
        if (index > 0) {
            items.set(index, data);
        }
    }

    private void sortMyData() {
        Collections.sort(items, new Comparator<MyData>() {
            @Override
            public int compare(MyData d1, MyData d2) {
                return (int)(d1.time - d2.time);
            }
        });
    }
}
