package cn.tianyu.client.util.impl.type;

import lombok.Generated;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @Author: CN_tianyu
 */
public class EvictingList<T> extends LinkedList<T> implements Serializable {
    private int aEi;

    public EvictingList(int var1) {
        this.aEi = var1;
    }

    public EvictingList(Collection<? extends T> var1, int var2) {
        super(var1);
        this.aEi = var2;
    }

    @Override
    public boolean add(T var1) {
        if (this.size() >= this.rS()) {
            this.removeFirst();
        }

        return super.add(var1);
    }

    public boolean rQ() {
        return this.size() >= this.rS();
    }

    public EvictingList<T> rR() {
        EvictingList adz = new EvictingList(this.aEi);

        for (int i = this.size() - 1; i >= 0; i--) {
            adz.add(this.get(i));
        }

        return adz;
    }

    @Generated
    public int rS() {
        return this.aEi;
    }

    @Generated
    public void aj(int var1) {
        this.aEi = var1;
    }
}
