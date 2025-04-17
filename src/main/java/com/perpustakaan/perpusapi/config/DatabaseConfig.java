package com.perpustakaan.perpusapi.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    static {
        try {
            Properties props = new Properties();
            InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.properties");

            if (input == null) {
                throw new RuntimeException("❌ File config.properties TIDAK ditemukan di classpath!");
            } else {
                System.out.println("✅ File config.properties ditemukan!");
            }

            props.load(input);
            dbUrl = props.getProperty("db.url");
            dbUser = props.getProperty("db.user");
            dbPassword = props.getProperty("db.password");

//            System.out.println("Database URL: " + dbUrl);
//            System.out.println("Database User: " + dbUser);

        } catch (Exception e) {
            System.err.println("Gagal membaca file konfigurasi: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver JDBC tidak ditemukan!", e);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal koneksi ke database! " + e.getMessage(), e);
        }
    }
}
