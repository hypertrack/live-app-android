package com.hypertrack.lib.internal.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulhas on 13/05/16.
 */
public class ListUtility {

    public static <T> List<List<T>> partition(List<T> list, final int L) {
        List<List<T>> partition = new ArrayList<List<T>>();
        final int N = list.size();

        for (int i = 0; i < N; i += L) {
            partition.add(new ArrayList<T>(
                    list.subList(i, Math.min(N, i + L)))
            );
        }

        return partition;
    }
}
