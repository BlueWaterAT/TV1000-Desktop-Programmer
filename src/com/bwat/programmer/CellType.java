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
	
	/**
	 * Gets the Class for the value type of this column used for Comparators
	 * 
	 * @param optionalValues Used to determine which Class to use for combo boxes
	 * @return Class type for column
	 */
	public Class<?> getCellClass( String... optionalValues ) {
		switch ( this ) {
			case TEXT:
				return String.class;
			case COMBO:
				// Defaults to String unless all values are numeric
				if ( optionalValues == null ) {
					return String.class;
				}
				
				boolean numeric = true;
				for ( int i = 0; i < optionalValues.length; i++ ) {
					if ( !StringUtils.isNumber( optionalValues[i] ) ) {
						numeric = false;
						break;
					}
				}
				return numeric ? Integer.class : String.class;
			case CHECK:
				return Boolean.class;
			case NUMBER:
				return Integer.class;
			default:
				return Object.class;
		}
	}
}