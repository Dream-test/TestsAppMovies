package ru.productstar.tests;

import okhttp3.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.sql.Statement;

public class UserRoutesTest {
    //OkHttpClient client;

    @BeforeAll
    static void initDBConnection() {
        AdminRoutesTest.initDBConnection();
    }

    @AfterAll
    static void closeDBConnection() {
        AdminRoutesTest.closeDBConnection();
    }

    @Test
    void logOutUser() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/logout")
                .build();

        try {
            Response response = client.newCall(request).execute();
            //System.out.println("logOutLogInUser response:" + response.code());
            Assertions.assertEquals(202, response.code());
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void logInUser() {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\"email\":\"admin@example.com\",\"password\":\"secret\"}", mediaType);
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/authenticate")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            //System.out.println("logOutLogInUser response:" + response.code());
            Assertions.assertEquals(200, response.code());
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void getUserByEmailWhenAuthorised() {
        try {
            Statement statement = AdminRoutesTest.connection.createStatement();
            statement.executeUpdate("insert into users (first_name, last_name, email, password, created_at, updated_at) values ('User1', 'LastNameUser1', 'user@example.com', '$2a$14$wVsaPvJnJJsomWArouWCtusem6S/.Gauq/GjOIEHpyh2DAMmso1wy', '2024-11-28', '2024-11-28')");
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        AdminRoutesTest adminRoutesTest = new AdminRoutesTest();
        adminRoutesTest.initHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/user?email=user%40example.com")
                .build();
        try {
            String response = adminRoutesTest.client.newCall(request).execute().body().string();
            Assertions.assertTrue(response.contains("\"email\":\"user@example.com\""));
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        try {
            Statement statement = AdminRoutesTest.connection.createStatement();
            statement.executeUpdate("DELETE FROM users WHERE email = 'user@example.com'");
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void getUserByEmailWhenUnauthorised() {
        try {
            Statement statement = AdminRoutesTest.connection.createStatement();
            statement.executeUpdate("insert into users (first_name, last_name, email, password, created_at, updated_at) values ('User1', 'LastNameUser1', 'user@example.com', '$2a$14$wVsaPvJnJJsomWArouWCtusem6S/.Gauq/GjOIEHpyh2DAMmso1wy', '2024-11-28', '2024-11-28')");
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/user?email=user%40example.com")
                .build();

        try {
            Response response = client.newCall(request).execute();
            //System.out.println("getUserByEmailWhenUnauthorised response:" + response.code());
            Assertions.assertEquals(401, response.code());
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        try {
            Statement statement = AdminRoutesTest.connection.createStatement();
            statement.executeUpdate("DELETE FROM users WHERE email = 'user@example.com'");
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }
}
