package com.security.app.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashUtil {
    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte bb : b) {
                sb.append(String.format("%02x", bb));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
