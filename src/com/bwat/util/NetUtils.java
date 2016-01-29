package com.bwat.util;

/**
 * Net related utility functions
 *
 * @author Kareem ElFaramawi
 */
public class NetUtils {
    public static final String IP_ADDRESS_PATTERN = "^(([01]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([01]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))$";

    /**
     * @param ip IP address to be tested
     * @return If the given IP matches the IPV4 pattern
     */
    public static boolean isValidIPAddress(String ip) {
        return ip.matches(IP_ADDRESS_PATTERN);
    }
}
