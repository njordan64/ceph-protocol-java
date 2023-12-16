/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.utils;

/**
 * Provides methods for converting byte arrays into hexadecimal strings.
 */
public class HexFunctions {
    private static final char[] HEX_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
     * Converts an array of bytes into a hexadecimal string. The result separates each byte with a space.
     * There are 16 bytes per line with a larger gap between the 8th and 9th bytes.
     *
     * @param bytes array of bytes to convert
     * @return a string of hexadecimal characters representing the byte array
     */
    public static String hexToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            if (i > 0 && i % 16 == 0) {
                sb.append("\n");
            } else if (i % 16 == 8) {
                sb.append("    ");
            } else if (i > 0) {
                sb.append(" ");
            }

            char highChar = HEX_CHARS[(bytes[i] & 0xf0) >> 4];
            char lowChar = HEX_CHARS[bytes[i] & 0x0f];

            sb.append(highChar);
            sb.append(lowChar);
        }

        return sb.toString();
    }

    /**
     * Prints an array of bytes into a hexadecimal string. The result separates each byte with a space.
     * There are 16 bytes per line with a larger gap between the 8th and 9th bytes.
     *
     * @param bytes array of bytes to convert
     */
    public static void printHexString(byte[] bytes) {
        System.out.println(hexToString(bytes));
    }
}