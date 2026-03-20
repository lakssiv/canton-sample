package org.example.canton.user.onboarding;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.canton.util.TokenGenerator;

import java.io.IOException;

public class PartyOnboarding {

    private static final OkHttpClient HTTP = new OkHttpClient();


    public String createParty(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String partyIdHint,
            String displayName
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/parties"
                : jsonApiBaseUrl + "/v2/parties";

        String json = """
        {
          "partyIdHint": "%s",
          "displayName": "%s"
        }
        """.formatted(partyIdHint, displayName);

        RequestBody body = RequestBody.create(
                json,
                okhttp3.MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = HTTP.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Create party failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }
}
