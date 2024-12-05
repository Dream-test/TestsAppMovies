package ru.productstar.tests;

import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Assertions;
import ru.productstar.tests.models.AuthResponse;

public class InitAuthenticatedHttpClient {
    OkHttpClient client;

    OkHttpClient initHttpClient() {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + this.getAccessToken())
                            .build();
                    return chain.proceed(request);
                })
                .build();
        return this.client;
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
}
