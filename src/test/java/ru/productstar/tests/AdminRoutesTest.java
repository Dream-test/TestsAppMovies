package ru.productstar.tests;

import com.google.gson.Gson;
import ru.productstar.tests.models.AuthResponse;
import okhttp3.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminRoutesTest {
    static String DB_URL = "jdbc:postgresql://localhost:5432/movies";
    static String DB_USER = "postgres";
    static String DB_PASSWORD = "postgres";
    static Connection connection;
    OkHttpClient client;

    @BeforeAll
    static void initDBConnection() {
        try {
            // Class.forName("org.postgresql.Driver");
            AdminRoutesTest.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @AfterAll
    static void closeDBConnection() {
        try {
            AdminRoutesTest.connection.close();
        } catch (Exception e) {
            Assertions.fail("Exception:" + e.getMessage());
        }
    }

    @BeforeEach
    void initHttpClient() {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + this.getAccessToken())
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }

    String getAccessToken() {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\"email\":\"admin@example.com\",\"password\":\"secret\"}", mediaType);
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/authenticate")
                .post(body)
                .build();

        try {
            String response = client.newCall(request).execute().body().string();
            AuthResponse authResponse = new Gson().fromJson(response, AuthResponse.class);
            return authResponse.accessToken;
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
            return "";
        }
    }

    @Test
    void adminCanGetMovies() {
        String[] filmTitles = null;
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = AdminRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT title FROM movies");
            List<String> titleList = new ArrayList<>();
            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
            filmTitles =titleList.toArray(new String[0]);
            //System.out.println("adminCanGetMovies / film titles: " + Arrays.toString(filmTitles));
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies")
                .build();
        try {
            String response = this.client.newCall(request).execute().body().string();
            //System.out.println("adminCanGetMovies response:" + response);
            for (String title : filmTitles) {
                Assertions.assertTrue(response.contains("\"title\":\"" + title + "\","), "\"title\" not found in response: " + title);
            }

        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void patchMovie() {
        String filmTitle = "";
        String filmID = "";
        try {
            Statement statement = AdminRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, title FROM movies LIMIT 1");
            while (resultSet.next()) {
                filmTitle = resultSet.getString("title");
                filmID = resultSet.getString("id");
            }
            //System.out.println("film title: " + filmTitle);
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\"title\":\"" + filmTitle + "\", \"description\":\"new description\"}", mediaType);
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies/" + filmID)
                .patch(body)
                .build();
        try {
            String response = this.client.newCall(request).execute().body().string();
            //System.out.println(response);
            Assertions.assertTrue(response.contains("new description"));
        } catch (Exception e) {
            Assertions.fail("Exception:" + e.getMessage());
        }
    }

    @Test
    void deleteMovie() {
        String[] filmTitles;
        String filmTitle = "";
        String filmID = "";
        try {
            Statement statement = AdminRoutesTest.connection.createStatement();
            statement.executeUpdate("insert into movies (title) values ('New title')");
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        try {
            Statement statement = AdminRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, title FROM movies where title = 'New title'");
            while (resultSet.next()) {
                filmTitle = resultSet.getString("title");
                filmID = resultSet.getString("id");
            }
            //System.out.println("film title: " + filmTitle + " | film id: " + filmID);
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies/" + filmID)
                .delete()
                .build();
        try {
            Response response = this.client.newCall(request).execute();
            //System.out.println("deleteMovie response:" + response.code());
            Assertions.assertEquals(200, response.code());
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = AdminRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT title FROM movies");
            List<String> titleList = new ArrayList<>();
            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
            filmTitles =titleList.toArray(new String[0]);
            //System.out.println("adminCanGetMovies / film titles: " + Arrays.toString(filmTitles));
            Assertions.assertFalse(Arrays.asList(filmTitles).contains(filmTitle));
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

    }
}
