package org.example.canton.contact.init;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.canton.util.TokenGenerator;

import java.io.IOException;
import java.util.UUID;

public class InitializeContractEOA {

    private static final OkHttpClient HTTP = new OkHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String prepareCreateIssuerConfig(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String synchronizerId,
            String packageId,
            String adminParty,
            String issuerParty,
            String instrumentId,
            String displayName,
            String symbol
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/interactive-submission/prepare"
                : jsonApiBaseUrl + "/v2/interactive-submission/prepare";

        String templateId = packageId + ":MyToken:IssuerConfig";

        String json = """
        {
          "workflowId": "init-issuer-config",
          "applicationId": "my-cip56-app",
          "commandId": "create-issuer-config-%s",
          "actAs": ["%s"],
          "readAs": ["%s"],
          "synchronizerId": "%s",
          "packageIdSelectionPreference": ["%s"],
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
                adminParty,
                synchronizerId,
                packageId,
                templateId,
                adminParty,
                issuerParty,
                instrumentId,
                displayName,
                symbol
        );

        return postJson(url, json, token, "Prepare IssuerConfig failed");
    }

    public String prepareCreateMintAuthority(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String synchronizerId,
            String packageId,
            String issuerParty,
            String instrumentId
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/interactive-submission/prepare"
                : jsonApiBaseUrl + "/v2/interactive-submission/prepare";

        String templateId = packageId + ":MyToken:MintAuthority";

        String json = """
        {
          "workflowId": "init-mint-authority",
          "applicationId": "my-cip56-app",
          "commandId": "create-mint-authority-%s",
          "actAs": ["%s"],
          "readAs": ["%s"],
          "synchronizerId": "%s",
          "packageIdSelectionPreference": ["%s"],
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
                issuerParty,
                synchronizerId,
                packageId,
                templateId,
                issuerParty,
                instrumentId
        );

        return postJson(url, json, token, "Prepare MintAuthority failed");
    }

    public String submitPreparedCreate(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String preparedTransactionJson,
            String signatureBase64,
            String publicKeyFingerprint,
            String partyId
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/interactive-submission/execute"
                : jsonApiBaseUrl + "/v2/interactive-submission/execute";

        JsonNode prepareResponse = MAPPER.readTree(preparedTransactionJson);
        JsonNode preparedTransaction = prepareResponse.get("preparedTransaction");
        if (preparedTransaction == null || preparedTransaction.isNull()) {
            throw new IOException("Submit prepared create failed - prepare response missing preparedTransaction");
        }

        JsonNode hashingSchemeVersion = prepareResponse.get("hashingSchemeVersion");
        String hashingScheme = hashingSchemeVersion == null || hashingSchemeVersion.isNull()
                ? "HASHING_SCHEME_VERSION_V2"
                : hashingSchemeVersion.asText();

        String json = """
        {
          "preparedTransaction": %s,
          "partySignatures": {
            "signatures": [
              {
                "party": "%s",
                "signatures": [
                  {
                    "format": "SIGNATURE_FORMAT_DER",
                    "signature": "%s",
                    "signedBy": "%s",
                    "signingAlgorithmSpec": "SIGNING_ALGORITHM_SPEC_EC_DSA_SHA_256"
                  }
                ]
              }
            ]
          },
          "deduplicationPeriod": {
            "Empty": {}
          },
          "submissionId": "%s",
          "hashingSchemeVersion": "%s"
        }
        """.formatted(
                MAPPER.writeValueAsString(preparedTransaction),
                partyId,
                signatureBase64,
                publicKeyFingerprint,
                "create-" + UUID.randomUUID(),
                hashingScheme
        );

        return postJson(url, json, token, "Submit prepared create failed");
    }

    private String postJson(
            String url,
            String json,
            String token,
            String errorMessage
    ) throws IOException {

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
                throw new IOException(errorMessage + " HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }
}
