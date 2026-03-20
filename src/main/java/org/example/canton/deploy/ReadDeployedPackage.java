package org.example.canton.deploy;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.canton.util.TokenGenerator;
import org.example.dto.PackagesResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public class ReadDeployedPackage {

    private static final OkHttpClient HTTP = new OkHttpClient();

    public String getPackages(String jsonApiBaseUrl, String realm, String client_id, String secret, String packageId) throws Exception {
        //String computedPackageId = computePackageId(darPath);
        String token = TokenGenerator.generateToken(realm, client_id, secret);

        String url = jsonApiBaseUrl.endsWith("/")
                ? jsonApiBaseUrl + "v2/packages"
                : jsonApiBaseUrl + "/v2/packages";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = HTTP.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();

            PackagesResponse parsed =
                    new Gson().fromJson(respBody, PackagesResponse.class);

            boolean deployed = parsed.getPackageIds().contains(packageId);
            System.out.println("Package deployed = " + deployed);
            if(deployed) {
                return packageId;
            }
            return null;


        }
    }

    public String computePackageId(String darPath) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(darPath));

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }
}
