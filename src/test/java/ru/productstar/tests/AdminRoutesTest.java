package ru.productstar.tests;

import okhttp3.*;
import org.junit.jupiter.api.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminRoutesTest implements CommonSetup{
    OkHttpClient client;

    @BeforeEach
    void InitAuthenticatedHttpClient(){
        this.client = new InitAuthenticatedHttpClient().initHttpClient();
    }

    @Test
    void adminCanGetMovies() {
        String[] filmTitles = null;
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT title FROM movies");
            List<String> titleList = new ArrayList<>();
            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
            filmTitles =titleList.toArray(new String[0]);
            System.out.println("adminCanGetMovies / film titles: " + Arrays.toString(filmTitles));

        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies")
                .build();

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
            Statement statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, title FROM movies LIMIT 1");
            while (resultSet.next()) {
                filmTitle = resultSet.getString("title");
                filmID = resultSet.getString("id");
            }
            //System.out.println("film title: " + filmTitle);
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\"title\":\"" + filmTitle + "\", \"description\":\"new description\"}", mediaType);
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies/" + filmID)
                .patch(body)
                .build();

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
            Statement statement = InitDBConnection.connection.createStatement();
            statement.executeUpdate("insert into movies (title) values ('New title')");

            statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, title FROM movies where title = 'New title'");
            while (resultSet.next()) {
                filmTitle = resultSet.getString("title");
                filmID = resultSet.getString("id");
            }

        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies/" + filmID)
                .delete()
                .build();

            Response response = this.client.newCall(request).execute();
            //System.out.println("deleteMovie response:" + response.code());
            Assertions.assertEquals(200, response.code());

            // Class.forName("org.postgresql.Driver");
            statement = InitDBConnection.connection.createStatement();
            resultSet = statement.executeQuery("SELECT title FROM movies");
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
