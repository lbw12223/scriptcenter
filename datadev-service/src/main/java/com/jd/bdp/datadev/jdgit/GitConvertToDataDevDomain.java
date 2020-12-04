package com.jd.bdp.datadev.jdgit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangrui25 on 2018/5/18.
 */
public abstract class GitConvertToDataDevDomain<T, E> {

    abstract T convertDataDevDomain(E e);


    /**
     * covert Iterable
     * @param es
     * @return
     */
    public List<T> covertDataDevDomainIterable(Iterable<E> es) {
        List<T> result = new ArrayList<T>();
        for (E e : es) {
            result.add(convertDataDevDomain(e));
        }
        return result;
    }
}
