package cn.tianyu.client.util.impl.type;

import java.util.Collection;

/**
 * @Author: CN_tianyu
 */
public class IndexedEvictingList<T> extends EvictingList<T> implements Cloneable {
    public IndexedEvictingList(int var1) {
        super(var1);
    }

    public IndexedEvictingList(Collection<? extends T> var1, int var2) {
        super(var1, var2);
    }

    public IndexedEvictingList(IndexedEvictingList<? extends T> var1) {
        super(var1, var1.rS());
    }

    public T jq() {
        return this.getLast();
    }

    @Override
    public T get(int var1) {
        return super.get(this.size() - var1 - 1);
    }

    public IndexedEvictingList<T> jr() {
        return (IndexedEvictingList<T>)super.clone();
    }

    @Override
    public Object clone() {
        return this.jr();
    }
}
