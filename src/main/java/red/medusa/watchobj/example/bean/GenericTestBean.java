package red.medusa.watchobj.example.bean;

public class GenericTestBean<T, Q> extends TestBean {
    private T t;
    private Q q;

    public GenericTestBean(T t, Q q) {
        this.t = t;
        this.q = q;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public Q getQ() {
        return q;
    }

    public void setQ(Q q) {
        this.q = q;
    }
}