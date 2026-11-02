package com.cfs.auth;

import java.security.SecureRandom;
import java.util.Base64;

public class JwtSecretGenerator {

    public static void main(String[] args) {

        byte[] key = new byte[32];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(key);

        String secret =
                Base64.getEncoder()
                      .encodeToString(key);

        System.out.println(secret);
    }
}