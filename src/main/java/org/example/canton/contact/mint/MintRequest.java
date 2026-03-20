package org.example.canton.contact.mint;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.canton.util.TokenGenerator;

import java.io.IOException;

public class MintRequest {

    private static final OkHttpClient HTTP = new OkHttpClient();
    public String mintToUser(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String packageId,
            String issuerParty,
            String mintAuthorityCid,
            String ownerParty,
            String amount
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/commands/submit-and-wait"
                : jsonApiBaseUrl + "/v2/commands/submit-and-wait";

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
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = HTTP.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Mint failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }
}
