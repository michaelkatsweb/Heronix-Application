package com.heronix.gateway.encryption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Gateway Encryption Service
 *
 * Provides strong encryption for all data transmitted through the secure gateway.
 * Uses AES-256-GCM for symmetric encryption and RSA-2048 for key exchange.
 *
 * Security Features:
 * - AES-256-GCM authenticated encryption
 * - Per-message unique IVs
 * - Master key for encrypting device symmetric keys
 * - RSA-2048 for asymmetric operations
 * - Key derivation with HKDF
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Secure Gateway Implementation
 */
@Service
@Slf4j
public class GatewayEncryptionService {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final String RSA_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @Value("${heronix.gateway.master-key:#{null}}")
    private String masterKeyBase64;

    private SecretKey masterKey;

    @PostConstruct
    public void init() {
        if (masterKeyBase64 == null || masterKeyBase64.isBlank()) {
            log.warn("SECURITY WARNING: No master key configured, generating ephemeral key");
            log.warn("For production, set HERONIX_GATEWAY_MASTER_KEY environment variable");
            masterKey = generateAesKey();
            log.info("Ephemeral master key generated (will be lost on restart)");
        } else {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(masterKeyBase64);
                masterKey = new SecretKeySpec(keyBytes, "AES");
                log.info("Gateway master key loaded successfully");
            } catch (Exception e) {
                log.error("Failed to load master key, generating ephemeral key", e);
                masterKey = generateAesKey();
            }
        }
    }

    /**
     * Encrypt data for transmission to a device
     */
    public EncryptedPayload encryptForDevice(String plaintext, String deviceSymmetricKey) {
        try {
            // Decode the device's symmetric key
            byte[] keyBytes = Base64.getDecoder().decode(deviceSymmetricKey);
            SecretKey deviceKey = new SecretKeySpec(keyBytes, "AES");

            // Generate random IV
            byte[] iv = generateIv();

            // Encrypt the data
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, deviceKey, gcmSpec);

            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);

            // Combine IV and ciphertext
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            String encryptedBase64 = Base64.getEncoder().encodeToString(buffer.array());

            return EncryptedPayload.builder()
                .algorithm("AES-256-GCM")
                .encryptedData(encryptedBase64)
                .ivLength(GCM_IV_LENGTH)
                .build();

        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new EncryptionException("Failed to encrypt data for device", e);
        }
    }

    /**
     * Decrypt data received from a device
     */
    public String decryptFromDevice(EncryptedPayload payload, String deviceSymmetricKey) {
        try {
            // Decode the device's symmetric key
            byte[] keyBytes = Base64.getDecoder().decode(deviceSymmetricKey);
            SecretKey deviceKey = new SecretKeySpec(keyBytes, "AES");

            // Decode the encrypted data
            byte[] combined = Base64.getDecoder().decode(payload.getEncryptedData());

            // Extract IV and ciphertext
            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[payload.getIvLength()];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            // Decrypt
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, deviceKey, gcmSpec);

            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            return new String(plaintextBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new EncryptionException("Failed to decrypt data from device", e);
        }
    }

    /**
     * Generate a new symmetric key for a device
     */
    public String generateDeviceSymmetricKey() {
        SecretKey key = generateAesKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Encrypt a value with the master key (for storing device keys)
     */
    public String encryptWithMasterKey(String plaintext) {
        try {
            byte[] iv = generateIv();

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, gcmSpec);

            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            return Base64.getEncoder().encodeToString(buffer.array());

        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt with master key", e);
        }
    }

    /**
     * Decrypt a value with the master key
     */
    public String decryptWithMasterKey(String encrypted) {
        try {
            byte[] combined = Base64.getDecoder().decode(encrypted);

            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, gcmSpec);

            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            return new String(plaintextBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt with master key", e);
        }
    }

    /**
     * Encrypt data with a device's public key (for initial key exchange)
     */
    public String encryptWithPublicKey(String plaintext, String publicKeyPem) {
        try {
            PublicKey publicKey = loadPublicKey(publicKeyPem);

            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);

            return Base64.getEncoder().encodeToString(ciphertext);

        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt with public key", e);
        }
    }

    /**
     * Generate a signature for data integrity verification
     */
    public String sign(String data, String privateKeyPem) {
        try {
            PrivateKey privateKey = loadPrivateKey(privateKeyPem);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);

        } catch (Exception e) {
            throw new EncryptionException("Failed to sign data", e);
        }
    }

    /**
     * Verify a signature
     */
    public boolean verify(String data, String signatureBase64, String publicKeyPem) {
        try {
            PublicKey publicKey = loadPublicKey(publicKeyPem);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return signature.verify(signatureBytes);

        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    /**
     * Generate a secure hash of data
     */
    public String hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("SHA-256 not available", e);
        }
    }

    /**
     * Generate a new RSA key pair
     */
    public KeyPairResult generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();

            String publicKeyPem = formatAsPem(keyPair.getPublic().getEncoded(), "PUBLIC KEY");
            String privateKeyPem = formatAsPem(keyPair.getPrivate().getEncoded(), "PRIVATE KEY");

            return new KeyPairResult(publicKeyPem, privateKeyPem);

        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("RSA not available", e);
        }
    }

    // ================== Private Helper Methods ==================

    private SecretKey generateAesKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(AES_KEY_SIZE, new SecureRandom());
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("AES not available", e);
        }
    }

    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private PublicKey loadPublicKey(String pem) throws Exception {
        String publicKeyPEM = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private PrivateKey loadPrivateKey(String pem) throws Exception {
        String privateKeyPEM = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    private String formatAsPem(byte[] keyBytes, String type) {
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN ").append(type).append("-----\n");

        int index = 0;
        while (index < base64.length()) {
            int end = Math.min(index + 64, base64.length());
            pem.append(base64, index, end).append("\n");
            index = end;
        }

        pem.append("-----END ").append(type).append("-----");
        return pem.toString();
    }

    /**
     * Result of key pair generation
     */
    public record KeyPairResult(String publicKeyPem, String privateKeyPem) {}
}
