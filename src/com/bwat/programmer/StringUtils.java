package com.bwat.programmer;

public final class StringUtils {
	static final String REG_NUM = "-?\\d+";
	
	public static boolean isNumber( String str ) {
		return str != null && str.trim().matches( REG_NUM );
	}
}
