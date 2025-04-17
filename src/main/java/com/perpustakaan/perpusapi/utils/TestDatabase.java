package com.perpustakaan.perpusapi.utils;

import com.perpustakaan.perpusapi.config.DatabaseConfig;
import java.sql.Connection;

public class TestDatabase {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn != null) {
                System.out.println("Koneksi ke database berhasil!");
            } else {
                System.out.println("Koneksi gagal.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
