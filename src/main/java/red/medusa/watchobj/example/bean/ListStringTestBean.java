package red.medusa.watchobj.example.bean;

import java.math.BigDecimal;
import java.util.*;

public class ListStringTestBean extends GenericListTestBean<String> {
    private Set<Date> dateSet = new HashSet<>();
    private List<BigDecimal> bigDecimalList = new ArrayList<>();
    private List<GenericListTestBean<Character>> listStringTestBeans = new ArrayList<>();

    public ListStringTestBean() {
        super("1");
        dateSet.add(new Date());
        dateSet.add(new Date());
        bigDecimalList.add(BigDecimal.ZERO);
        bigDecimalList.add(BigDecimal.ONE);

        listStringTestBeans.add(new GenericListTestBean<>('C'));
    }

    public Set<Date> getDateSet() {
        return dateSet;
    }

    public void setDateSet(Set<Date> dateSet) {
        this.dateSet = dateSet;
    }

    public List<BigDecimal> getBigDecimalList() {
        return bigDecimalList;
    }

    public void setBigDecimalList(List<BigDecimal> bigDecimalList) {
        this.bigDecimalList = bigDecimalList;
    }

    public List<GenericListTestBean<Character>> getListStringTestBeans() {
        return listStringTestBeans;
    }

    public void setListStringTestBeans(List<GenericListTestBean<Character>> listStringTestBeans) {
        this.listStringTestBeans = listStringTestBeans;
    }
}