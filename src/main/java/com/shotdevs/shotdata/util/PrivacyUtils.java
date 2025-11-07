package com.shotdevs.shotdata.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for handling privacy-related functions.
 */
public class PrivacyUtils {

    /**
     * Anonymizes a player's name using SHA-256 hashing.
     *
     * @param name The player's name.
     * @return The anonymized name.
     */
    public static String anonymizeName(String name) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(name.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return "anon_" + hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "anon_" + name;
        }
    }
}
