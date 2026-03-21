package org.example.canton.vault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.Base64;

/**
 * HashiCorp Vault Transit Engine client.
 * Private key NEVER leaves Vault — all signing happens inside Vault.
 *
 * Used for User2 and User3 EOA operations:
 *   User2 signs Transfer transaction
 *   User3 signs Redeem transaction
 */
public class VaultClient {

    private static final MediaType JSON =
            MediaType.get("application/json");

    private final OkHttpClient http   = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String addr;
    private final String token;

    public VaultClient(String addr, String token) {
        this.addr  = addr;
        this.token = token;
    }

    public void healthCheck() throws Exception {
        Request req = new Request.Builder()
                .url(addr + "/v1/sys/health")
                .get()
                .build();

        try (Response resp = http.newCall(req).execute()) {
            String body = resp.body().string();
            if (resp.code() != 200 && resp.code() != 429) {
                throw new RuntimeException(
                        "Vault unhealthy [" + resp.code()
                                + "]: " + body);
            }
            JsonNode json = mapper.readTree(body);
            System.out.println("[Vault] Healthy — sealed: "
                    + json.get("sealed").asBoolean());
        }
    }

    public void createKey(String keyName) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("type",                   "ecdsa-p256");
        body.put("exportable",             false);
        body.put("allow_plaintext_backup", false);

        Request req = new Request.Builder()
                .url(addr + "/v1/transit/keys/" + keyName)
                .header("X-Vault-Token", token)
                .post(RequestBody.create(
                        mapper.writeValueAsString(body), JSON))
                .build();

        try (Response resp = http.newCall(req).execute()) {
            String respBody = resp.body().string();
            if (!resp.isSuccessful()) {
                if (respBody.contains("already exists")) {
                    System.out.println(
                            "[Vault] Key already exists: " + keyName);
                    return;
                }
                throw new RuntimeException(
                        "Create key failed [" + resp.code()
                                + "]: " + respBody);
            }
        }
        System.out.println("[Vault] Key created: " + keyName);
    }

    public String getPublicKey(String keyName) throws Exception {
        Request req = new Request.Builder()
                .url(addr + "/v1/transit/keys/" + keyName)
                .header("X-Vault-Token", token)
                .get()
                .build();

        try (Response resp = http.newCall(req).execute()) {
            String body = resp.body().string();
            if (!resp.isSuccessful()) {
                throw new RuntimeException(
                        "Get public key failed [" + resp.code()
                                + "]: " + body);
            }
            JsonNode json    = mapper.readTree(body);
            JsonNode keys    = json.get("data").get("keys");
            String   version = keys.fieldNames().next();
            return keys.get(version).get("public_key").asText();
        }
    }

    public String sign(String keyName, String hashHex) throws Exception {
        byte[] hashBytes  = hexToBytes(hashHex);
        String hashBase64 = Base64.getEncoder()
                .encodeToString(hashBytes);
        return signBase64Hash(keyName, hashBase64);
    }

    public String signBase64Hash(String keyName, String hashBase64) throws Exception {

        ObjectNode body = mapper.createObjectNode();
        body.put("input",     hashBase64);
        body.put("prehashed", true);
        body.put("marshaling_algorithm", "asn1");

        Request req = new Request.Builder()
                .url(addr + "/v1/transit/sign/" + keyName + "/sha2-256")
                .header("X-Vault-Token", token)
                .post(RequestBody.create(
                        mapper.writeValueAsString(body), JSON))
                .build();

        try (Response resp = http.newCall(req).execute()) {
            String respBody = resp.body().string();
            if (!resp.isSuccessful()) {
                throw new RuntimeException(
                        "Vault signing failed [" + resp.code()
                                + "]: " + respBody);
            }
            JsonNode result   = mapper.readTree(respBody);
            String   vaultSig = result.get("data")
                    .get("signature").asText();
            String base64Sig  = vaultSig.substring(
                    vaultSig.lastIndexOf(":") + 1);
            System.out.println(
                    "[Vault] Signed — key never left Vault ✓");
            return base64Sig;
        }
    }

    public String signBase64Data(String keyName, String dataBase64) throws Exception {

        ObjectNode body = mapper.createObjectNode();
        body.put("input", dataBase64);
        body.put("marshaling_algorithm", "asn1");

        Request req = new Request.Builder()
                .url(addr + "/v1/transit/sign/" + keyName + "/sha2-256")
                .header("X-Vault-Token", token)
                .post(RequestBody.create(
                        mapper.writeValueAsString(body), JSON))
                .build();

        try (Response resp = http.newCall(req).execute()) {
            String respBody = resp.body().string();
            if (!resp.isSuccessful()) {
                throw new RuntimeException(
                        "Vault signing failed [" + resp.code()
                                + "]: " + respBody);
            }
            JsonNode result   = mapper.readTree(respBody);
            String   vaultSig = result.get("data")
                    .get("signature").asText();
            String base64Sig  = vaultSig.substring(
                    vaultSig.lastIndexOf(":") + 1);
            System.out.println(
                    "[Vault] Signed — key never left Vault ✓");
            return base64Sig;
        }
    }

    public String getPublicKeyBase64(String keyName) throws Exception {
        String publicKeyPem = getPublicKey(keyName);
        return publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }

    private static byte[] hexToBytes(String hex) {
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        if (hex.length() % 2 != 0) hex = "0" + hex;
        int    len  = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (
                    (Character.digit(hex.charAt(i),     16) << 4)
                            +  Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
