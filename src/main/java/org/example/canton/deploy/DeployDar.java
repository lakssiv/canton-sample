package org.example.canton.deploy;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.canton.util.TokenGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeployDar {

   // private static final Gson GSON = new Gson();
    private static final OkHttpClient HTTP = new OkHttpClient();


    public String deployDar(String jsonApiBaseUrl, String darPath, String realm, String client_id, String secret) throws IOException {

        String token = TokenGenerator.generateToken(realm, client_id, secret);
        Path dar = Path.of(darPath);
        if (!Files.exists(dar)) {
            throw new IOException("DAR not found: " + darPath);
        }

        String url = jsonApiBaseUrl.endsWith("/") ? jsonApiBaseUrl + "v2/packages" : jsonApiBaseUrl + "/v2/packages";
        RequestBody body =
                RequestBody.create(dar.toFile(), okhttp3.MediaType.get("application/octet-stream"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = HTTP.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Upload failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

}
