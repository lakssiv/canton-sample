package org.example.canton.user.onboarding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.canton.util.TokenGenerator;

import java.io.IOException;

public class EOAtoPartyMapping {

    private static final OkHttpClient HTTP = new OkHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String getSynchronizers(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/state/connected-synchronizers"
                : jsonApiBaseUrl + "/v2/state/connected-synchronizers";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = HTTP.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Get synchronizers failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }

    public String generateTopology(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String synchronizerId,
            String partyHint,
            String publicKeyBase64
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/parties/external/generate-topology"
                : jsonApiBaseUrl + "/v2/parties/external/generate-topology";

        String json = """
        {
          "synchronizer": "%s",
          "partyHint": "%s",
          "publicKey": {
            "format": "CRYPTO_KEY_FORMAT_DER_X509_SUBJECT_PUBLIC_KEY_INFO",
            "keyData": "%s",
            "keySpec": "SIGNING_KEY_SPEC_EC_P256"
          },
          "otherConfirmingParticipantUids": []
        }
        """.formatted(
                synchronizerId,
                partyHint,
                publicKeyBase64
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
                throw new IOException("Generate topology failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }

    public String allocateParty(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret,
            String synchronizerId,
            String onboardingTransactionsJson,
            String signatureBase64,
            String publicKeyFingerprint
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/parties/external/allocate"
                : jsonApiBaseUrl + "/v2/parties/external/allocate";

        JsonNode onboardingTransactions = MAPPER.readTree(onboardingTransactionsJson);
        if (!onboardingTransactions.isArray()) {
            throw new IOException("Allocate external party failed - onboardingTransactions must be a JSON array");
        }

        String signedTransactionsJson = buildSignedTransactionsJson(onboardingTransactions);

        String json = """
        {
          "synchronizer": "%s",
          "onboardingTransactions": %s,
          "multiHashSignatures": [
            {
              "format": "SIGNATURE_FORMAT_DER",
              "signature": "%s",
              "signedBy": "%s",
              "signingAlgorithmSpec": "SIGNING_ALGORITHM_SPEC_EC_DSA_SHA_256"
            }
          ]
        }
        """.formatted(
                synchronizerId,
                signedTransactionsJson,
                signatureBase64,
                publicKeyFingerprint
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
                throw new IOException("Allocate external party failed HTTP " + response.code() + " - " + respBody);
            }
            return respBody;
        }
    }

    private String buildSignedTransactionsJson(JsonNode onboardingTransactions)
            throws IOException {

        for (JsonNode onboardingTransaction : onboardingTransactions) {
            if (onboardingTransaction.isObject()
                    && onboardingTransaction.get("transaction") != null) {
                return MAPPER.writeValueAsString(onboardingTransactions);
            }
        }

        com.fasterxml.jackson.databind.node.ArrayNode signedTransactions =
                MAPPER.createArrayNode();

        for (JsonNode onboardingTransaction : onboardingTransactions) {
            signedTransactions.add(
                    MAPPER.createObjectNode()
                            .set("transaction", onboardingTransaction)
            );
        }

        return MAPPER.writeValueAsString(signedTransactions);
    }

    public String extractTopologyData(
            String generateTopologyResponseJson
    ) throws IOException {

        JsonNode response = MAPPER.readTree(generateTopologyResponseJson);

        JsonNode multiHash = response.get("multiHash");
        JsonNode publicKeyFingerprint = response.get("publicKeyFingerprint");
        JsonNode topologyTransactions = response.get("topologyTransactions");

        if (multiHash == null || multiHash.isNull()) {
            throw new IOException("Generate topology response missing multiHash");
        }
        if (publicKeyFingerprint == null || publicKeyFingerprint.isNull()) {
            throw new IOException("Generate topology response missing publicKeyFingerprint");
        }
        if (topologyTransactions == null || topologyTransactions.isNull()) {
            throw new IOException("Generate topology response missing topologyTransactions");
        }

        return """
        {
          "multiHash": "%s",
          "publicKeyFingerprint": "%s",
          "topologyTransactions": %s
        }
        """.formatted(
                multiHash.asText(),
                publicKeyFingerprint.asText(),
                MAPPER.writeValueAsString(topologyTransactions)
        );
    }
}
