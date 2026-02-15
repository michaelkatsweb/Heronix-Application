package com.heronix.security;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

/**
 * Local Key Store
 *
 * Manages Ed25519 private keys stored locally on the user's workstation.
 * Keys are encrypted with the user's password using AES-256-GCM before
 * being written to disk at %APPDATA%/Heronix/keys/.
 *
 * File naming convention: {username}_{fingerprint}.key.enc
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2026-02 - HSTP Implementation
 */
@Slf4j
public class LocalKeyStore {

    private static final String KEY_DIR_NAME = "Heronix/keys";
    private static final String KEY_FILE_SUFFIX = ".key.enc";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int PBKDF2_ITERATIONS = 100_000;
    private static final byte[] SALT = "HeronixLocalKey-AES-Salt".getBytes(StandardCharsets.UTF_8);

    private final Path keyDirectory;
    private final SecureRandom secureRandom = new SecureRandom();

    public LocalKeyStore() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            appData = System.getProperty("user.home");
        }
        this.keyDirectory = Paths.get(appData, KEY_DIR_NAME);
    }

    // ========================================================================
    // STORE KEY
    // ========================================================================

    /**
     * Store a private key encrypted with the user's password.
     *
     * @param username       the user's username
     * @param fingerprint    the key fingerprint (for identification)
     * @param privateKeyBase64 the Ed25519 private key in Base64
     * @param password       the user's password (used to derive encryption key)
     */
    public void storeKey(String username, String fingerprint, String privateKeyBase64, String password)
            throws IOException {
        ensureDirectoryExists();

        byte[] plaintext = privateKeyBase64.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = encrypt(plaintext, password);

        String filename = username + "_" + fingerprint.substring(0, 16) + KEY_FILE_SUFFIX;
        Path keyFile = keyDirectory.resolve(filename);
        Files.write(keyFile, encrypted);

        log.info("Stored encrypted key for {} fingerprint {} at {}", username, fingerprint.substring(0, 16), keyFile);
    }

    // ========================================================================
    // RETRIEVE KEY
    // ========================================================================

    /**
     * Retrieve and decrypt a private key using the user's password.
     *
     * @param username    the user's username
     * @param fingerprint the key fingerprint
     * @param password    the user's password
     * @return the decrypted Ed25519 private key in Base64
     */
    public String retrieveKey(String username, String fingerprint, String password)
            throws IOException {
        String filename = username + "_" + fingerprint.substring(0, 16) + KEY_FILE_SUFFIX;
        Path keyFile = keyDirectory.resolve(filename);

        if (!Files.exists(keyFile)) {
            throw new IOException("Key file not found: " + keyFile);
        }

        byte[] encrypted = Files.readAllBytes(keyFile);
        byte[] decrypted = decrypt(encrypted, password);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    // ========================================================================
    // KEY LISTING
    // ========================================================================

    /**
     * List all stored key fingerprint prefixes for a user.
     */
    public List<String> listKeys(String username) throws IOException {
        ensureDirectoryExists();
        List<String> keys = new ArrayList<>();

        String prefix = username + "_";
        try (Stream<Path> files = Files.list(keyDirectory)) {
            files.filter(p -> p.getFileName().toString().startsWith(prefix) &&
                             p.getFileName().toString().endsWith(KEY_FILE_SUFFIX))
                 .forEach(p -> {
                     String name = p.getFileName().toString();
                     String fp = name.substring(prefix.length(), name.length() - KEY_FILE_SUFFIX.length());
                     keys.add(fp);
                 });
        }

        return keys;
    }

    /**
     * Check if a key exists locally for the given user and fingerprint.
     */
    public boolean hasKey(String username, String fingerprint) {
        String filename = username + "_" + fingerprint.substring(0, 16) + KEY_FILE_SUFFIX;
        Path keyFile = keyDirectory.resolve(filename);
        return Files.exists(keyFile);
    }

    /**
     * Delete a stored key.
     */
    public void deleteKey(String username, String fingerprint) throws IOException {
        String filename = username + "_" + fingerprint.substring(0, 16) + KEY_FILE_SUFFIX;
        Path keyFile = keyDirectory.resolve(filename);
        Files.deleteIfExists(keyFile);
        log.info("Deleted key {} for user {}", fingerprint.substring(0, 16), username);
    }

    // ========================================================================
    // ENCRYPTION HELPERS
    // ========================================================================

    private byte[] encrypt(byte[] plaintext, String password) {
        try {
            SecretKey key = deriveKey(password);
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);

            byte[] result = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, result, GCM_IV_LENGTH, ciphertext.length);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt key data", e);
        }
    }

    private byte[] decrypt(byte[] data, String password) {
        try {
            SecretKey key = deriveKey(password);
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(data, 0, iv, 0, GCM_IV_LENGTH);

            byte[] ciphertext = new byte[data.length - GCM_IV_LENGTH];
            System.arraycopy(data, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt key — wrong password or corrupted data", e);
        }
    }

    private SecretKey deriveKey(String password) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, PBKDF2_ITERATIONS, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private void ensureDirectoryExists() throws IOException {
        if (!Files.exists(keyDirectory)) {
            Files.createDirectories(keyDirectory);
        }
    }
}
