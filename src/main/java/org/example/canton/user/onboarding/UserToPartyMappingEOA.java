package org.example.canton.user.onboarding;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.canton.util.TokenGenerator;

import java.io.IOException;

public class UserToPartyMappingEOA {
    private static final OkHttpClient HTTP = new OkHttpClient();

    public String grantActAs(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String userId,
            String partyId
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/users/" + userId + "/rights"
                : jsonApiBaseUrl + "/v2/users/" + userId + "/rights";

        String json = """
        {
          "userId": "%s",
          "identityProviderId": "",
          "rights": [
            {
              "kind": {
                "CanActAs": {
                  "value": {
                    "party": "%s"
                  }
                }
              }
            }
          ]
        }
        """.formatted(userId, partyId);

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
                throw new IOException("Grant actAs failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }

    public String grantReadAs(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String userId,
            String partyId
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/users/" + userId + "/rights"
                : jsonApiBaseUrl + "/v2/users/" + userId + "/rights";

        String json = """
        {
          "userId": "%s",
          "identityProviderId": "",
          "rights": [
            {
              "kind": {
                "CanReadAs": {
                  "value": {
                    "party": "%s"
                  }
                }
              }
            }
          ]
        }
        """.formatted(userId, partyId);

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
                throw new IOException("Grant readAs failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }

    public String grantExecuteAs(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String userId,
            String partyId
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/users/" + userId + "/rights"
                : jsonApiBaseUrl + "/v2/users/" + userId + "/rights";

        String json = """
        {
          "userId": "%s",
          "identityProviderId": "",
          "rights": [
            {
              "kind": {
                "CanExecuteAs": {
                  "value": {
                    "party": "%s"
                  }
                }
              }
            }
          ]
        }
        """.formatted(userId, partyId);

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
                throw new IOException("Grant executeAs failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }
}
