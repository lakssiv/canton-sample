package org.example.canton.ledger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.canton.util.TokenGenerator;

import java.io.IOException;

public class LedgerOffsetClient {

    private static final OkHttpClient HTTP = new OkHttpClient();

    public long getLedgerEnd(
            String jsonApiBaseUrl,
            String realm,
            String clientId,
            String secret
    ) throws IOException {

        String token = TokenGenerator.generateToken(realm, clientId, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/state/ledger-end"
                : jsonApiBaseUrl + "/v2/state/ledger-end";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = HTTP.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Get ledger end failed HTTP " + response.code() + " - " + respBody);
            }

            JsonObject json = JsonParser.parseString(respBody).getAsJsonObject();
            if (json.get("offset") == null || json.get("offset").isJsonNull()) {
                throw new IOException("Ledger end response missing offset: " + respBody);
            }
            return json.get("offset").getAsLong();
        }
    }
}
