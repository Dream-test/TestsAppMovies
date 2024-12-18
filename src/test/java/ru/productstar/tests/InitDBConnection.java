package ru.productstar.tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.DriverManager;

public class InitDBConnection {
    static String DB_URL = "jdbc:postgresql://localhost:5432/movies";
    static String DB_USER = "postgres";
    static String DB_PASSWORD = "postgres";
    static Connection connection;

    @BeforeAll
    public static void initDBConnection() {
        try {
            // Class.forName("org.postgresql.Driver");
            InitDBConnection.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @AfterAll
    static void closeDBConnection() {
        try {
            InitDBConnection.connection.close();
        } catch (Exception e) {
            Assertions.fail("Exception:" + e.getMessage());
        }
    }
}
