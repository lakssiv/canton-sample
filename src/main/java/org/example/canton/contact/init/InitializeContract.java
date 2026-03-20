package org.example.canton.contact.init;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.canton.util.TokenGenerator;

import java.io.IOException;

public class InitializeContract {
    private static final OkHttpClient HTTP = new OkHttpClient();


    public String createIssuerConfig(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String packageId,
            String adminParty,
            String issuerParty,
            String instrumentId,
            String displayName,
            String symbol
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/commands/submit-and-wait"
                : jsonApiBaseUrl + "/v2/commands/submit-and-wait";

        String templateId = packageId + ":MyToken:IssuerConfig";

        String json = """
        {
          "workflowId": "init-issuer-config",
          "applicationId": "my-cip56-app",
          "commandId": "create-issuer-config-%s",
          "actAs": ["%s"],
          "commands": [
            {
              "CreateCommand": {
                "templateId": "%s",
                "createArguments": {
                  "admin": "%s",
                  "issuer": "%s",
                  "instrumentId": "%s",
                  "displayName": "%s",
                  "symbol": "%s"
                }
              }
            }
          ]
        }
        """.formatted(
                System.currentTimeMillis(),
                adminParty,
                templateId,
                adminParty,
                issuerParty,
                instrumentId,
                displayName,
                symbol
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
                throw new IOException("Create IssuerConfig failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }

    public String createMintAuthority(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String packageId,
            String issuerParty,
            String instrumentId
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/commands/submit-and-wait"
                : jsonApiBaseUrl + "/v2/commands/submit-and-wait";

        String templateId = packageId + ":MyToken:MintAuthority";

        String json = """
        {
          "workflowId": "init-mint-authority",
          "applicationId": "my-cip56-app",
          "commandId": "create-mint-authority-%s",
          "actAs": ["%s"],
          "commands": [
            {
              "CreateCommand": {
                "templateId": "%s",
                "createArguments": {
                  "issuer": "%s",
                  "instrumentId": "%s"
                }
              }
            }
          ]
        }
        """.formatted(
                System.currentTimeMillis(),
                issuerParty,
                templateId,
                issuerParty,
                instrumentId
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
                throw new IOException("Create MintAuthority failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }

    public String findMintAuthorityContractId(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String packageId,
            String issuerParty,
            String instrumentId,
            long activeAtOffset
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/state/active-contracts"
                : jsonApiBaseUrl + "/v2/state/active-contracts";

        String templateId = packageId + ":MyToken:MintAuthority";

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
        """.formatted(activeAtOffset, issuerParty, templateId);

        System.out.println("findMintAuthority request = " + json);

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
                throw new IOException("Find MintAuthority failed HTTP " + response.code() + " - " + respBody);
            }

            System.out.println("findMintAuthority raw response = " + respBody);

            com.google.gson.JsonArray result =
                    com.google.gson.JsonParser.parseString(respBody).getAsJsonArray();

            if (result == null || result.isEmpty()) {
                throw new IOException("No active MintAuthority contracts found");
            }

            for (com.google.gson.JsonElement el : result) {
                com.google.gson.JsonObject row = el.getAsJsonObject();
                com.google.gson.JsonObject contractEntry = row.getAsJsonObject("contractEntry");
                com.google.gson.JsonObject jsActiveContract = contractEntry.getAsJsonObject("JsActiveContract");
                com.google.gson.JsonObject createdEvent = jsActiveContract.getAsJsonObject("createdEvent");

                String contractId = createdEvent.get("contractId").getAsString();
                com.google.gson.JsonObject createArgument = createdEvent.getAsJsonObject("createArgument");

                String payloadInstrumentId = createArgument.get("instrumentId").getAsString();
                if (instrumentId.equals(payloadInstrumentId)) {
                    return contractId;
                }
            }

            throw new IOException("MintAuthority found, but none matched instrumentId=" + instrumentId);
        }
    }


    public String listVisibleActiveContracts(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            long activeAtOffset
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/state/active-contracts"
                : jsonApiBaseUrl + "/v2/state/active-contracts";

        String json = """
        {
          "filter": {
            "filtersByParty": {},
            "filtersForAnyParty": {
              "cumulative": [
                {
                  "identifierFilter": {
                    "WildcardFilter": {
                      "value": {
                        "includeCreatedEventBlob": false
                      }
                    }
                  }
                }
              ]
            }
          },
          "verbose": true,
          "activeAtOffset": %d
        }
        """.formatted(activeAtOffset);

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
                throw new IOException("List visible active contracts failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }
}
