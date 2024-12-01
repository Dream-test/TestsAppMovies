package ru.productstar.tests;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MoviesRoutesTest implements CommonSetup {
    OkHttpClient client = new OkHttpClient();

    @Test
    void appReturnedMovieGenres() {
        String[] filmGenres = null;
        try {
            Statement statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT genre FROM genres");
            List<String> genreList = new ArrayList<>();
            while (resultSet.next()) {
                genreList.add(resultSet.getString("genre"));
            }
            filmGenres =genreList.toArray(new String[0]);
            //System.out.println("film genres: " + Arrays.toString(filmGenres));

             Request request = new Request.Builder()
                .url("http://localhost:4000/v1/genres")
                .build();

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
        String[] filmTitles;
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT title FROM movies");
            List<String> titleList = new ArrayList<>();
            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
            filmTitles =titleList.toArray(new String[0]);
            //System.out.println("appReturnedMovies / film titles: " + Arrays.toString(filmTitles));

            Request request = new Request.Builder()
                .url("http://localhost:4000/v1/movies")
                .build();

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
    void appReturnMoviesByGenre() {
        String[] filmTitles = null;
        String genreID = "";
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select id from genres limit 1");
            while (resultSet.next()) {
                genreID = resultSet.getString("id");
            }
            //System.out.println("Genre ID: " + genreID);

            resultSet = statement.executeQuery("select title from movies left join movies_genres on movies.id = movies_genres.movie_id left join genres on movies_genres.genre_id = genres.id where genres.id = " + genreID);
            List<String> titleList = new ArrayList<>();
            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
            filmTitles =titleList.toArray(new String[0]);
            //System.out.println("appReturnMoviesByGenres / film titles: " + Arrays.toString(filmTitles));

            Request request = new Request.Builder()
                .url("http://localhost:4000/v1/movies/genres/" + genreID)
                .build();

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
    void appReturnedMoviesGenreId_WhenGenreIdNotExist() {
        String genreID = "";
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select max(id)+1 as maxId from genres");
            while (resultSet.next()) {
                genreID = resultSet.getString("maxId");
            }
            //System.out.println("appReturnedMoviesById_WhenIdNotExist /film id: " + filmID);
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
        appReturnedMoviesByGenre_WhenGenreIdWrong(genreID);
    }

    @Test
    void appReturnedMoviesById() {
        String filmTitle = "";
        String filmID = "";
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, title FROM movies LIMIT 1");
            while (resultSet.next()) {
                filmTitle = resultSet.getString("title");
                filmID = resultSet.getString("id");
            }
            //System.out.println("appReturnedMoviesById /film title: " + filmTitle + "  film id: " + filmID);

            Request request = new Request.Builder()
                .url("http://localhost:4000/v1/movies/" + filmID)
                .build();

            String response = this.client.newCall(request).execute().body().string();
            //System.out.println("appReturnedMoviesById response:" + response);
            Assertions.assertTrue(response.contains("\"id\":" + filmID + ",\"title\":\"" + filmTitle + "\","));
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void appReturnedMoviesById_WhenIdNotExist() {
        String filmID = "";
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select max(id)+1 as maxId from movies");
            while (resultSet.next()) {
                filmID = resultSet.getString("maxId");
            }
            //System.out.println("appReturnedMoviesById_WhenIdNotExist /film id: " + filmID);
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
        appReturnedMoviesById_WhenIdWrong(filmID);
    }

    @Test
    void appReturnedMoviesById_WhenIdNegative() {
        appReturnedMoviesById_WhenIdWrong("-1");
    }

    @Test
    void appReturnedMoviesById_WhenIdZero() {
        appReturnedMoviesById_WhenIdWrong("0");
    }

    private void appReturnedMoviesById_WhenIdWrong(String filmID) {
        try {
            //System.out.println("appReturnedMoviesById_WhenIdNegative /film id: " + filmID);
            Request request = new Request.Builder()
                    .url("http://localhost:4000/v1/movies/" + filmID)
                    .build();

            Response response = this.client.newCall(request).execute();
            String responseMessage = response.body().string();
            //System.out.println("appReturnedMoviesById_WhenIdWrong response:" + response);
            Assertions.assertEquals(400, response.code());
            Assertions.assertTrue(responseMessage.contains("sql: no rows in result set"));
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    private void appReturnedMoviesByGenre_WhenGenreIdWrong(String genreID) {
        try {
            //System.out.println("appReturnedMoviesById_WhenIdNegative /film id: " + filmID);
            Request request = new Request.Builder()
                    .url("http://localhost:4000/v1/movies/genres/" + genreID)
                    .build();

            Response response = this.client.newCall(request).execute();
            String responseMessage = response.body().string();
            //System.out.println("appReturnedMoviesByGenre_WhenGenreIdWrong response:" + response);
            Assertions.assertEquals(400, response.code());
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }
}
