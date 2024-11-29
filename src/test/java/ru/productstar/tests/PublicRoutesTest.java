package ru.productstar.tests;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.*;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PublicRoutesTest {
    OkHttpClient client;

    @BeforeAll
    static void initDBConnection() {
        AdminRoutesTest.initDBConnection();
    }

    @AfterAll
    static void closeDBConnection() {
        AdminRoutesTest.closeDBConnection();
    }

    @BeforeEach
    void initHttpClient() {
        this.client = new OkHttpClient();
    }



    @Test
    void appReturnedMovieGenres() {
        String[] filmGenres = null;
        try {
            Statement statement = AdminRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT genre FROM genres");
            List<String> genreList = new ArrayList<>();
            while (resultSet.next()) {
                genreList.add(resultSet.getString("genre"));
            }
            filmGenres =genreList.toArray(new String[0]);
            //System.out.println("film genres: " + Arrays.toString(filmGenres));
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        // OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/genres")
                .build();

        try {
            String response = this.client.newCall(request).execute().body().string();
            //System.out.println(response);
            for (String genre : filmGenres) {
                Assertions.assertTrue(response.contains(genre), "genre not found in response: " + genre);
            }
            Assertions.assertTrue(response.contains(filmGenres[1]));
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void appReturnedMovies() {
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
            //System.out.println("appReturnedMovies / film titles: " + Arrays.toString(filmTitles));
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/movies")
                .build();
        try {
            String response = this.client.newCall(request).execute().body().string();
            //System.out.println("appReturnedMovies response:" + response);
            for (String title : filmTitles) {
                Assertions.assertTrue(response.contains("\"title\":\"" + title + "\","), "\"title\" not found in response: " + title);
            }
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void appReturnMoviesByGenres() {
        String[] filmTitles = null;
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = AdminRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select title from movies left join movies_genres on movies.id = movies_genres.movie_id left join genres on movies_genres.genre_id = genres.id where genres.id = 11");
            List<String> titleList = new ArrayList<>();
            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
            filmTitles =titleList.toArray(new String[0]);
            //System.out.println("appReturnMoviesByGenres / film titles: " + Arrays.toString(filmTitles));
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/movies/genres/11")
                .build();

        try {
            String response = this.client.newCall(request).execute().body().string();
            //System.out.println("appReturnMoviesByGenres response:" + response);
            for (String title : filmTitles) {
                Assertions.assertTrue(response.contains("\"title\":\"" + title + "\","), "\"title\" not found in response: " + title);
            }
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void appReturnedMoviesById() {
        String filmTitle = "";
        String filmID = "";
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = AdminRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, title FROM movies LIMIT 1");
            while (resultSet.next()) {
                filmTitle = resultSet.getString("title");
                filmID = resultSet.getString("id");
            }
            //System.out.println("appReturnedMoviesById /film title: " + filmTitle + "  film id: " + filmID);
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/movies/" + filmID)
                .build();

        try {
            String response = this.client.newCall(request).execute().body().string();
            //System.out.println("appReturnedMoviesById response:" + response);
            Assertions.assertTrue(response.contains("\"id\":" + filmID + ",\"title\":\"" + filmTitle + "\","));
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }
}
