package com.perpustakaan.perpusapi.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;

import java.util.Date;

public class TokenUtil {
    private static final String SECRET = "supersecretkey1234567890123456"; // Minimal 32 karakter

    // Generate Token JWT
    public static String generateToken(String email) {
        try {
            JWSSigner signer = new MACSigner(SECRET);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 3600000)) // Expire 1 jam
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Error creating JWT", e);
        }
    }

    // Verifikasi Token JWT
    public static boolean verifyToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SECRET);
            return signedJWT.verify(verifier);
        } catch (Exception e) {
            return false;
        }
    }

    // Ambil Email dari Token
    public static String getEmailFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }
}

