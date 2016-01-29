package com.bwat.programmer;

public enum CellType {
    TEXT,
    COMBO,
    CHECK,
    NUMBER;

    /**
     * @return Type name for use with the file format
     */
    public String getTypeName() {
        return name().toLowerCase();
    }
}