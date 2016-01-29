package com.bwat.programmer;

import java.util.Vector;

public class InternalTable implements Comparable<InternalTable> {
    public int index;
    public Vector<Vector<Object>> data;

    public InternalTable(int index, Vector<Vector<Object>> data) {
        this.index = index;
        this.data = data;
    }

    public int compareTo(InternalTable o) {
        return Integer.compare(index, o.index);
    }
}
