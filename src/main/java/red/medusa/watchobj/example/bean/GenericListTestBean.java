package red.medusa.watchobj.example.bean;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GenericListTestBean<E> extends GenericTestBean<List<E>, Date> {
    private String watchedName = "GenericListTestBean";
    private int[] array = new int[5];
    private E[] eArray;
    private E e;

    public GenericListTestBean(E e) {
        super(new ArrayList<>(Arrays.asList(e, e, e)), new Date());
        this.e = e;
        eArray = (E[]) Array.newInstance(e.getClass(), 5);
        Arrays.fill(eArray, e);
    }

    public int[] getArray() {
        return array;
    }

    public void setArray(int[] array) {
        this.array = array;
    }

    public E[] geteArray() {
        return eArray;
    }

    public void seteArray(E[] eArray) {
        this.eArray = eArray;
    }

    public E getE() {
        return e;
    }

    public void setE(E e) {
        this.e = e;
    }

    public String getWatchedName() {
        return watchedName;
    }

    public void setWatchedName(String watchedName) {
        this.watchedName = watchedName;
    }
}