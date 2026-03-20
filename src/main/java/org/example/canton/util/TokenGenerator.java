package org.example.canton.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TokenGenerator {

    private static final Gson GSON = new Gson();
    private static final OkHttpClient HTTP = new OkHttpClient();

    private TokenGenerator() {}
    public static String generateToken(String realm, String clientId, String clientSecret)
            throws IOException {
        String baseUrl = System.getProperty("keycloakbase_url");
        String tokenUrl = baseUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        FormBody.Builder form = new FormBody.Builder()
                .add("client_id", clientId)
                .add("grant_type", "client_credentials")
                .add("scope", "openid");

        if (clientSecret != null && !clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }

        RequestBody body = form.build();
        Request request = new Request.Builder()
                .url(tokenUrl)
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = HTTP.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Keycloak token error HTTP " + response.code() + " - " + respBody);
            }
            JsonElement root = GSON.fromJson(respBody, JsonElement.class);
            if (root == null || !root.isJsonObject()) {
                throw new IOException("Invalid token response: " + respBody);
            }
            JsonObject obj = root.getAsJsonObject();
            JsonElement token = obj.get("access_token");
            if (token == null || !token.isJsonPrimitive()) {
                throw new IOException("Token missing in response: " + respBody);
            }
            return token.getAsString();
        }
    }






}
