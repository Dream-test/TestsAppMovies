package ru.productstar.tests;

import okhttp3.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.sql.Statement;

public class UserRoutesTest extends InitDBConnection {
    OkHttpClient client = new OkHttpClient();

    @Test
    void logOutUser() {
        //Arrange
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/logout")
                .build();

        //Act
        try {
            Response response = client.newCall(request).execute();  //Выполняем запрос к API для logout
            //System.out.println("logOutLogInUser response:" + response.code());

            // Assert
            Assertions.assertEquals(202, response.code());  //Проверяем статус код Request
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void logInUser() {
        //Arrange
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\"email\":\"admin@example.com\",\"password\":\"secret\"}", mediaType);
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/authenticate")
                .post(body)
                .build();

        //Act
        try {
            Response response = client.newCall(request).execute();  //Выполняем запрос к API для login
            //System.out.println("logOutLogInUser response:" + response.code());

            //Assert
            Assertions.assertEquals(200, response.code());  //Проверяем статус код Request
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void getUserByEmailWhenAuthorised() {
        //Arrange
        try {
            Statement statement = UserRoutesTest.connection.createStatement();
            statement.executeUpdate("insert into users (first_name, last_name, email, password, created_at, updated_at) values ('User1', 'LastNameUser1', 'user@example.com', '$2a$14$wVsaPvJnJJsomWArouWCtusem6S/.Gauq/GjOIEHpyh2DAMmso1wy', '2024-11-28', '2024-11-28')"); //Создаем в БД нового user

            //Act
        OkHttpClient authenticatedClient = new InitAuthenticatedHttpClient().initHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/user?email=user%40example.com")
                .build();

            String response = authenticatedClient.newCall(request).execute().body().string();  //Выполняем аутентифицированный запрос к API на получение user по email

            //Assert
            Assertions.assertTrue(response.contains("\"email\":\"user@example.com\""));  //Проверяем что response возвращает email нового user

            statement = UserRoutesTest.connection.createStatement();
            statement.executeUpdate("DELETE FROM users WHERE email = 'user@example.com'");  //Удаляем созданного user из БД
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void getUserByEmailWhenUnauthorised() {
        //Arrange
        try {
            Statement statement = UserRoutesTest.connection.createStatement();
            statement.executeUpdate("insert into users (first_name, last_name, email, password, created_at, updated_at) values ('User1', 'LastNameUser1', 'user@example.com', '$2a$14$wVsaPvJnJJsomWArouWCtusem6S/.Gauq/GjOIEHpyh2DAMmso1wy', '2024-11-28', '2024-11-28')");  //Создаем в БД нового user

            //Act
            Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/user?email=user%40example.com")
                .build();

            Response response = client.newCall(request).execute();  //Выполняем не аутентифицированный запрос к API на получение user по email
            //System.out.println("getUserByEmailWhenUnauthorised response:" + response.code());

            //Assert
            Assertions.assertEquals(401, response.code());  //Проверяем статус код response

            statement = UserRoutesTest.connection.createStatement();
            statement.executeUpdate("DELETE FROM users WHERE email = 'user@example.com'");  //Удаляем созданного user из БД
        } catch (Exception e) {
            Assertions.fail("Exception: " + e.getMessage());
        }
    }
}
