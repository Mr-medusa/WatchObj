package red.medusa.watchobj.example.bean;

import java.util.HashMap;
import java.util.Map;

public class ListIntegerTestBean extends GenericListTestBean<Integer> {
    private String watchedName = "Wrapper Bean";
    private Map<String, Object> map = new HashMap<>();

    public ListIntegerTestBean() {
        super(1);
        map.put("a", "a");
        map.put("b", "b");
        map.put("testIntegerBean", new GenericListTestBean<>("HELLO WORLD"));
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public String getWatchedName() {
        return watchedName;
    }

    public void setWatchedName(String watchedName) {
        this.watchedName = watchedName;
    }
}