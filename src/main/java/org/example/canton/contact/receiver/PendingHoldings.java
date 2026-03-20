package org.example.canton.contact.receiver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.canton.util.TokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PendingHoldings {

    private static final OkHttpClient HTTP = new OkHttpClient();
    public String findPendingHoldingContractIds(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String packageId,
            String receiverParty,
            long activeAtOffset
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/state/active-contracts"
                : jsonApiBaseUrl + "/v2/state/active-contracts";

        String templateId = packageId + ":MyToken:PendingHolding";

        String json = """
        {
          "activeAtOffset": %d,
          "filter": {
            "filtersByParty": {
              "%s": {
                "cumulative": [
                  {
                    "identifierFilter": {
                      "TemplateFilter": {
                        "value": {
                          "templateId": "%s",
                          "includeCreatedEventBlob": false
                        }
                      }
                    }
                  }
                ]
              }
            }
          },
          "verbose": true
        }
        """.formatted(activeAtOffset, receiverParty, templateId);

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
                throw new IOException("Find PendingHolding failed HTTP " + response.code() + " - " + respBody);
            }

            return respBody;
        }
    }


    public String findHoldingContractIds(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String packageId,
            String receiverParty,
            long activeAtOffset
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/state/active-contracts"
                : jsonApiBaseUrl + "/v2/state/active-contracts";

        String templateId = packageId + ":MyToken:Holding";

        String json = """
        {
          "activeAtOffset": %d,
          "filter": {
            "filtersByParty": {
              "%s": {
                "cumulative": [
                  {
                    "identifierFilter": {
                      "TemplateFilter": {
                        "value": {
                          "templateId": "%s",
                          "includeCreatedEventBlob": false
                        }
                      }
                    }
                  }
                ]
              }
            }
          },
          "verbose": true
        }
        """.formatted(activeAtOffset, receiverParty, templateId);

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
                throw new IOException("Find PendingHolding failed HTTP " + response.code() + " - " + respBody);
            }

            return respBody;
        }
    }


    public String acceptPendingHolding(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String packageId,
            String receiverParty,
            String pendingHoldingCid
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/commands/submit-and-wait"
                : jsonApiBaseUrl + "/v2/commands/submit-and-wait";

        String templateId = packageId + ":MyToken:PendingHolding";

        String json = """
        {
          "workflowId": "accept-pending-holding",
          "applicationId": "my-cip56-app",
          "commandId": "accept-%s",
          "actAs": ["%s"],
          "commands": [
            {
              "ExerciseCommand": {
                "templateId": "%s",
                "contractId": "%s",
                "choice": "Accept",
                "choiceArgument": {}
              }
            }
          ]
        }
        """.formatted(
                System.currentTimeMillis(),
                receiverParty,
                templateId,
                pendingHoldingCid
        );

        System.out.println("acceptPendingHolding request = " + json);

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
                throw new IOException("Accept PendingHolding failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }

    public List<String> extractPendingHoldingCids(String respBody) {

        List<String> cids = new ArrayList<>();

        JsonArray root = JsonParser.parseString(respBody).getAsJsonArray();

        for (JsonElement el : root) {
            JsonObject obj = el.getAsJsonObject();

            JsonObject contractEntry = obj
                    .getAsJsonObject("contractEntry")
                    .getAsJsonObject("JsActiveContract")
                    .getAsJsonObject("createdEvent");

            String contractId = contractEntry.get("contractId").getAsString();

            cids.add(contractId);
        }

        return cids;
    }
}
