package am.ik.demo.facebootifier;

import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.Ciphertext;
import org.springframework.vault.support.Plaintext;

import java.util.LinkedHashMap;
import java.util.Map;

public class VaultTransitUtil {

    VaultOperations vaultOps = BeanUtil.getBean(VaultOperations.class);
    String keyname = "springdemo";


    public String encryptData(String ptext) {
        Plaintext plaintext = Plaintext.of(ptext);
        String cipherText = vaultOps.opsForTransit().encrypt(keyname, plaintext).getCiphertext();
        return cipherText;
    }

    public String decryptData(String ctext) {
        Ciphertext ciphertext = Ciphertext.of(ctext);
        String plaintext = vaultOps.opsForTransit().decrypt(keyname, ciphertext).asString();
        return plaintext;
    }

    public String rewrapData(String ctext) {
        Ciphertext cipherext = Ciphertext.of(ctext);
        String rewrappedtext = vaultOps.opsForTransit().rewrap(keyname, cipherext.getCiphertext());
        return rewrappedtext;
    }

    public Object getKeyStatus() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", vaultOps.opsForTransit().getKeys());
        body.put("type", vaultOps.opsForTransit().getKey(keyname).getType());
        body.put("latest_version", vaultOps.opsForTransit().getKey(keyname).getLatestVersion());
        body.put("min_decrypt_version", vaultOps.opsForTransit().getKey(keyname).getMinDecryptionVersion());
        return body;
    }

}
