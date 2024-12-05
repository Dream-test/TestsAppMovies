package ru.productstar.tests;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MoviesRoutesTest extends InitDBConnection {
    OkHttpClient client = new OkHttpClient();

    @Test
    void appReturnedMovieGenres() {
        //Arrange
        String[] filmGenres;
        try {
            Statement statement = MoviesRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT genre FROM genres"); //Получаем из БД список genres
            List<String> genreList = new ArrayList<>();
            while (resultSet.next()) {
                genreList.add(resultSet.getString("genre"));
            }
            filmGenres =genreList.toArray(new String[0]); //Формируем строковый массив из списка genres
            //System.out.println("film genres: " + Arrays.toString(filmGenres));

            //Act
             Request request = new Request.Builder()
                .url("http://localhost:4000/v1/genres")
                .build();

            String response = this.client.newCall(request).execute().body().string();  //Выполняем запрос списка genres через API
            //System.out.println(response);

            //Assert
            for (String genre : filmGenres) {
                Assertions.assertTrue(response.contains(genre), "genre not found in response: " + genre);  //Проверяем что все genres полученные из БД присутствуют в response API
            }
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void appReturnedMovies() {
        //Arrange
        String[] filmTitles;
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = MoviesRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT title FROM movies"); //Получаем из БД список titles of movies
            List<String> titleList = new ArrayList<>();
            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
            filmTitles =titleList.toArray(new String[0]);  //Формируем строковый массив из списка titles of movies
            //System.out.println("appReturnedMovies / film titles: " + Arrays.toString(filmTitles));

            //Act
            Request request = new Request.Builder()
                .url("http://localhost:4000/v1/movies")
                .build();

            String response = this.client.newCall(request).execute().body().string();  //Выполняем запрос списка movies через API
            //System.out.println("appReturnedMovies response:" + response);

            //Assert
            for (String title : filmTitles) {
                Assertions.assertTrue(response.contains("\"title\":\"" + title + "\","), "\"title\" not found in response: " + title);  //Проверяем что все titles полученные из БД присутствуют в response API
            }
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void appReturnMoviesByGenre() {
        //Arrange
        String[] filmTitles = null;
        String genreID = "";
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = MoviesRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select id from genres limit 1");  //Получаем id первого genre в БД
            while (resultSet.next()) {
                genreID = resultSet.getString("id");
            }
            //System.out.println("Genre ID: " + genreID);

            resultSet = statement.executeQuery("select title from movies left join movies_genres on movies.id = movies_genres.movie_id left join genres on movies_genres.genre_id = genres.id where genres.id = " + genreID); //Получаем из БД список titles of movies с genre id из первого запроса
            List<String> titleList = new ArrayList<>();
            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
            filmTitles =titleList.toArray(new String[0]);  //Формируем строковый массив из списка titles of movies
            //System.out.println("appReturnMoviesByGenres / film titles: " + Arrays.toString(filmTitles));

            //Act
            Request request = new Request.Builder()
                .url("http://localhost:4000/v1/movies/genres/" + genreID)
                .build();

            String response = this.client.newCall(request).execute().body().string();  //Выполняем запрос через API списка movies с genre id полученным из БД
            //System.out.println("appReturnMoviesByGenres response:" + response);

            //Assert
            for (String title : filmTitles) {
                Assertions.assertTrue(response.contains("\"title\":\"" + title + "\","), "\"title\" not found in response: " + title);  //Проверяем что все titles полученные из БД присутствуют в response API
            }
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void appReturnedMoviesGenreId_WhenGenreIdNotExist() {
        //Arrange
        String genreID = "";
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = MoviesRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select max(id)+1 as maxId from genres");  //Получаем из БД максимальное значение genre id увеличенное на 1
            while (resultSet.next()) {
                genreID = resultSet.getString("maxId");
            }
            //System.out.println("appReturnedMoviesById_WhenIdNotExist /film id: " + filmID);
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        //Act & Assert
        appReturnedMoviesByGenre_WhenGenreIdWrong(genreID); // Поверяем ответ сервера на запрос списка movies с несуществующим genre id
    }

    @Test
    void appReturnedMoviesById() {
        //Arrange
        String filmTitle = "";
        String filmID = "";
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = MoviesRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, title FROM movies LIMIT 1");  //Получаем id и title первого movie из БД
            while (resultSet.next()) {
                filmTitle = resultSet.getString("title");
                filmID = resultSet.getString("id");
            }
            //System.out.println("appReturnedMoviesById /film title: " + filmTitle + "  film id: " + filmID);

            //Act
            Request request = new Request.Builder()
                .url("http://localhost:4000/v1/movies/" + filmID)
                .build();

            String response = this.client.newCall(request).execute().body().string();  //Выполняем запрос через API movie с id полученным из БД
            //System.out.println("appReturnedMoviesById response:" + response);

            //Assert
            Assertions.assertTrue(response.contains("\"id\":" + filmID + ",\"title\":\"" + filmTitle + "\",")); //Проверяем совпадение title полученных movie
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void appReturnedMoviesById_WhenIdNotExist() {
        //Arrange
        String filmID = "";
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = MoviesRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select max(id)+1 as maxId from movies");  //Получаем из БД максимальное значение movie id увеличенное на 1
            while (resultSet.next()) {
                filmID = resultSet.getString("maxId");
            }
            //System.out.println("appReturnedMoviesById_WhenIdNotExist /film id: " + filmID);
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

        //Act & Assert
        appReturnedMoviesById_WhenIdWrong(filmID);  // Поверяем ответ сервера на запрос movie с несуществующим movie id
    }

    @Test
    void appReturnedMoviesById_WhenIdNegative() {
        appReturnedMoviesById_WhenIdWrong("-1"); // Поверяем ответ сервера на запрос movie с negative movie id
    }

    @Test
    void appReturnedMoviesById_WhenIdZero() {
        appReturnedMoviesById_WhenIdWrong("0");  // Поверяем ответ сервера на запрос movie с zero movie id
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
