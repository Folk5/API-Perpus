package com.perpustakaan.perpusapi.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import com.perpustakaan.perpusapi.config.DatabaseConfig;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public class TokenUtil {

    private static String SECRET;
    private static final long EXPIRATION_TIME = 86400000;

    static {
        try {
            Properties props = new Properties();

            // Cari file di classpath (src/main/resources)
            InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.properties");

            if (input == null) {
                throw new RuntimeException("File config.properties tidak ditemukan di classpath!");
            }

            props.load(input);
            SECRET = props.getProperty("SecretKey");

//            System.out.println("Database Props " + dbUrl + " " + dbUser + " " + dbPassword);

        } catch (Exception e) {
            System.err.println("Gagal membaca file konfigurasi: " + e.getMessage());
        }
    }

    public static String generateToken(String userId) {
        try {
            JWSSigner signer = new MACSigner(SECRET.getBytes());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userId) //Ini Payload
//                    .claim("userId", userId)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean verifyToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SECRET.getBytes());

            return signedJWT.verify(verifier) &&
                    new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getSubjectFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
