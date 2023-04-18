package red.medusa.watchobj.example.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionBean<T extends Number> {
    public T t;
    public String name;
    public List rowList = new ArrayList<>();
    public List<? super Number> numbers = new ArrayList<>();
    public Map<String, Map<String, String>> mapMap = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public List<? super Number> getNumbers() {
        return numbers;
    }
    public void setNumbers(List<? super Number> numbers) {
        this.numbers = numbers;
    }

    public Map<String, Map<String, String>> getMapMap() {
        return mapMap;
    }

    public void setMapMap(Map<String, Map<String, String>> mapMap) {
        this.mapMap = mapMap;
    }

    public List getRowList() {
        return rowList;
    }

    public void setRowList(List rowList) {
        this.rowList = rowList;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }
}

