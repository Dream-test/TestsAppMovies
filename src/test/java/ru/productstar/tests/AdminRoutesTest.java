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
        //Arrange
        String[] filmTitles = null;
        try {
            // Class.forName("org.postgresql.Driver");
            Statement statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT title FROM movies"); //Получаем из БД список titles of movies
            List<String> titleList = new ArrayList<>();
            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
            filmTitles =titleList.toArray(new String[0]);  //Формируем строковый массив из списка titles of movies
            //System.out.println("adminCanGetMovies / film titles: " + Arrays.toString(filmTitles));

            //Act
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies")
                .build();

            String response = this.client.newCall(request).execute().body().string();  //Выполняем запрос списка movies через API
            //System.out.println("adminCanGetMovies response:" + response);

            //Assert
            for (String title : filmTitles) {
                Assertions.assertTrue(response.contains("\"title\":\"" + title + "\","), "\"title\" not found in response: " + title);  //Проверяем что все titles полученные из БД присутствуют в response API
            }
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void patchMovie() {
        //Arrange
        String filmTitle = "";
        String filmID = "";
        try {
            Statement statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, title FROM movies LIMIT 1");  //Получаем id и title первого movie из БД
            while (resultSet.next()) {
                filmTitle = resultSet.getString("title");
                filmID = resultSet.getString("id");
            }
            //System.out.println("film title: " + filmTitle);

            //Act
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\"title\":\"" + filmTitle + "\", \"description\":\"new description\"}", mediaType);  //Выполняем запрос на изменение description movie с id полученным из БД
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies/" + filmID)
                .patch(body)
                .build();

            String response = this.client.newCall(request).execute().body().string();  //Выполняем запрос через API movie с id полученным из БД
            //System.out.println(response);

            //Assert
            Assertions.assertTrue(response.contains("new description"));  //Проверяем состояние description в полученном response
        } catch (Exception e) {
            Assertions.fail("Exception:" + e.getMessage());
        }
    }

    @Test
    void deleteMovie() {
        //Arrange
        String[] filmTitles;
        String filmTitle = "";
        String filmID = "";
        try {
            Statement statement = InitDBConnection.connection.createStatement();
            statement.executeUpdate("insert into movies (title) values ('New title')"); //Добавляем новый movie в БД

            statement = InitDBConnection.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, title FROM movies where title = 'New title'");  //Получаем id добавленного movie
            while (resultSet.next()) {
                filmTitle = resultSet.getString("title");
                filmID = resultSet.getString("id");
            }

            //Act
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies/" + filmID)
                .delete()
                .build();

            Response response = this.client.newCall(request).execute();  //выполняем запрос к API на удаление movie с полученным id
            //System.out.println("deleteMovie response:" + response.code());

            //Assert
            Assertions.assertEquals(200, response.code());  //Проверяем статус код response

            // Class.forName("org.postgresql.Driver");
            statement = InitDBConnection.connection.createStatement();
            resultSet = statement.executeQuery("SELECT title FROM movies");  //Получаем из БД список titles of movies
            List<String> titleList = new ArrayList<>();
            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
            filmTitles =titleList.toArray(new String[0]);  //Формируем строковый массив из списка titles of movies
            //System.out.println("adminCanGetMovies / film titles: " + Arrays.toString(filmTitles));
            Assertions.assertFalse(Arrays.asList(filmTitles).contains(filmTitle));  //Проверяем что все добавленный title отсутствует в списке titles полученных из БД
            } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }

    }
}
