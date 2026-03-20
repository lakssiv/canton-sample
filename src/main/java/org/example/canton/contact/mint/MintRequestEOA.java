package org.example.canton.contact.mint;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class MintRequestEOA {
    private static final OkHttpClient HTTP = new OkHttpClient();
    public String prepareMintToUser(
            String jsonApiBaseUrl,
            String bearerToken,
            String packageId,
            String issuerParty,
            String mintAuthorityCid,
            String ownerParty,
            String amount
    ) throws IOException {
        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/external-signing/prepare"
                : jsonApiBaseUrl + "/v2/external-signing/prepare";

        String templateId = packageId + ":MyToken:MintAuthority";

        String json = """
        {
          "workflowId": "mint-token",
          "applicationId": "my-cip56-app",
          "commandId": "mint-%s",
          "actAs": ["%s"],
          "commands": [
            {
              "ExerciseCommand": {
                "templateId": "%s",
                "contractId": "%s",
                "choice": "Mint",
                "choiceArgument": {
                  "owner": "%s",
                  "amount": "%s"
                }
              }
            }
          ]
        }
        """.formatted(
                System.currentTimeMillis(),
                issuerParty,
                templateId,
                mintAuthorityCid,
                ownerParty,
                amount
        );

        RequestBody body = RequestBody.create(
                json,
                okhttp3.MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + bearerToken)
                .build();

        try (Response response = HTTP.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Prepare mint failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }


    public String submitSignedMint(
            String jsonApiBaseUrl,
            String bearerToken,
            String preparedTransaction,
            String signature
    ) throws IOException {
        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/external-signing/submit"
                : jsonApiBaseUrl + "/v2/external-signing/submit";

        String json = """
        {
          "preparedTransaction": "%s",
          "signatures": ["%s"]
        }
        """.formatted(preparedTransaction, signature);

        RequestBody body = RequestBody.create(
                json,
                okhttp3.MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + bearerToken)
                .build();

        try (Response response = HTTP.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Submit signed mint failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }
}
