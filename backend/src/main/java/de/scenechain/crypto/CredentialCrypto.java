package de.scenechain.crypto;

import de.scenechain.config.SceneChainProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.stereotype.Component;

@Component
public class CredentialCrypto {
    private static final int ARGON_MEMORY_KIB = 19456;
    private static final int ARGON_ITERATIONS = 2;
    private static final int ARGON_PARALLELISM = 1;
    private final SecureRandom random = new SecureRandom();
    private final byte[] pepper;
    private final byte[] metadataKey;
    private final byte[] lookupKey;
    private final byte[] syntheticKey;
    private final java.util.concurrent.Semaphore argonCapacity = new java.util.concurrent.Semaphore(
        Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors())), true);

    public CredentialCrypto(SceneChainProperties properties) {
        if (properties.cookieSecure()) validateProductionKeys(properties);
        this.pepper = derive(properties.keys().pepper());
        this.metadataKey = derive(properties.keys().metadata());
        this.lookupKey = derive(properties.keys().lookup());
        this.syntheticKey = derive(properties.keys().synthetic());
    }

    public byte[] randomBytes(int length) {
        byte[] value = new byte[length];
        random.nextBytes(value);
        return value;
    }

    public SecureRandom random() { return random; }

    public byte[] verifier(byte[] credential, byte[] salt, UUID accountId) {
        boolean acquired = false;
        try {
            acquired = argonCapacity.tryAcquire(2, java.util.concurrent.TimeUnit.SECONDS);
            if (!acquired) throw new IllegalStateException("Password-verifier capacity is temporarily exhausted");
            return verifierWithinCapacity(credential, salt, accountId);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Password verification interrupted", error);
        } finally {
            if (acquired) argonCapacity.release();
        }
    }

    private byte[] verifierWithinCapacity(byte[] credential, byte[] salt, UUID accountId) {
        var params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(salt)
            .withMemoryAsKB(ARGON_MEMORY_KIB)
            .withIterations(ARGON_ITERATIONS)
            .withParallelism(ARGON_PARALLELISM)
            .build();
        var generator = new Argon2BytesGenerator();
        generator.init(params);
        byte[] argon = new byte[32];
        generator.generateBytes(credential, argon);
        byte[] context = ByteBuffer.allocate(argon.length + 16 + 2)
            .put(argon)
            .putLong(accountId.getMostSignificantBits())
            .putLong(accountId.getLeastSignificantBits())
            .putShort((short) 1)
            .array();
        Arrays.fill(argon, (byte) 0);
        return hmac(pepper, context);
    }

    public boolean verify(byte[] candidate, byte[] salt, UUID accountId, byte[] expected) {
        return MessageDigest.isEqual(verifier(candidate, salt, accountId), expected);
    }

    public byte[] encrypt(byte[] plaintext, UUID accountId) {
        try {
            byte[] nonce = randomBytes(12);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(metadataKey, "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(aad(accountId));
            byte[] ciphertext = cipher.doFinal(plaintext);
            return ByteBuffer.allocate(1 + nonce.length + ciphertext.length)
                .put((byte) 1).put(nonce).put(ciphertext).array();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Metadata encryption failed", e);
        }
    }

    public byte[] decrypt(byte[] encoded, UUID accountId) {
        try {
            ByteBuffer in = ByteBuffer.wrap(encoded);
            if (in.get() != 1) throw new GeneralSecurityException("Unsupported key version");
            byte[] nonce = new byte[12];
            in.get(nonce);
            byte[] ciphertext = new byte[in.remaining()];
            in.get(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(metadataKey, "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(aad(accountId));
            return cipher.doFinal(ciphertext);
        } catch (GeneralSecurityException | java.nio.BufferUnderflowException e) {
            throw new IllegalArgumentException("Invalid protected metadata", e);
        }
    }

    public String keyedHandle(String normalizedHandle) {
        return java.util.HexFormat.of().formatHex(hmac(lookupKey, normalizedHandle.getBytes(StandardCharsets.US_ASCII)));
    }

    public byte[] syntheticSeed(String normalizedHandle) {
        return hmac(syntheticKey, normalizedHandle.getBytes(StandardCharsets.US_ASCII));
    }

    public String enrollmentConfirmationTag(byte[] canonical, UUID accountId) {
        byte[] context = ByteBuffer.allocate(16 + canonical.length)
            .putLong(accountId.getMostSignificantBits()).putLong(accountId.getLeastSignificantBits())
            .put(canonical).array();
        return java.util.HexFormat.of().formatHex(hmac(metadataKey, context));
    }

    private byte[] aad(UUID id) {
        return ByteBuffer.allocate(20)
            .putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits())
            .putShort((short) 1).putShort((short) 1).array();
    }

    private static byte[] derive(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void validateProductionKeys(SceneChainProperties properties) {
        var keys = properties.keys();
        var values = java.util.List.of(keys.pepper(), keys.metadata(), keys.lookup(), keys.synthetic());
        if (values.stream().anyMatch(value -> value == null || value.length() < 32
            || value.toLowerCase(java.util.Locale.ROOT).contains("development")
            || value.toLowerCase(java.util.Locale.ROOT).contains("change-me"))
            || new java.util.HashSet<>(values).size() != values.size()) {
            throw new IllegalStateException("Production cryptographic keys must be distinct, non-example secrets of at least 32 characters");
        }
    }

    private static byte[] hmac(byte[] key, byte[] value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(value);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
