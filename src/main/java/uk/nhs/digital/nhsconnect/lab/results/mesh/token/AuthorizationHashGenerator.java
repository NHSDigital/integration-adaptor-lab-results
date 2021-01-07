package uk.nhs.digital.nhsconnect.lab.results.mesh.token;

import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

class AuthorizationHashGenerator {

    private final static String HMAC_SHA256_ALGORITHM_NAME = "HmacSHA256";

    @SneakyThrows
    public String computeHash(MeshConfig meshConfig, Nonce nonce, String timestamp) {
        String hashInput = String.join(":", meshConfig.getMailboxId(), nonce.value, nonce.count, meshConfig.getMailboxPassword(), timestamp);

        Mac sha256_HMAC = Mac.getInstance(HMAC_SHA256_ALGORITHM_NAME);
        SecretKeySpec secret_key = new SecretKeySpec(meshConfig.getSharedKey().getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM_NAME);
        sha256_HMAC.init(secret_key);

        return Hex.encodeHexString(sha256_HMAC.doFinal(hashInput.getBytes(StandardCharsets.UTF_8)));
    }
}
