package com.bwat.programmer;

/**
 * Represents the type of data that can be entered in a column
 *
 * @author Kareem ElFaramawi
 */
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