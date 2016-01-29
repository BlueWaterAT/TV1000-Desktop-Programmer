package com.bwat.util;

/**
 * @author Kareem ElFaramawi
 */
public class NetUtils {
    public static final String IP_ADDRESS_PATTERN = "^(([01]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([01]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))$";
    public static final String PORT_PATTERN = "^\\d{1,5}$";

    public static boolean isValidIPAddress(String ip) {
        return ip.matches(IP_ADDRESS_PATTERN);
    }

    public static boolean isValidPort(String port) {
        return port.matches(PORT_PATTERN) && Integer.parseInt(port) <= 65535;
    }
}
