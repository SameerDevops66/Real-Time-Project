package com.nokia.esim.service;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionServiceImpl implements EncryptionService
{

    private final String ALGORITHM = "AES";
    private final String MODE = "AES/CBC/PKCS5Padding";

    // Replace with your own secret key (16, 24, or 32 bytes)
    private final String SECRET_KEY = "123456789012345612345678";

    // Generate a random IV (16 bytes) for CBC mode
    private final byte[] IV = new byte[16];

    public String encrypt(String data) throws Exception
    {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(MODE);
        SecureRandom random = new SecureRandom();
        random.nextBytes(IV); // Generate a random IV
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
        byte[] combined = new byte[IV.length + encryptedBytes.length];
        System.arraycopy(IV, 0, combined, 0, IV.length);
        System.arraycopy(encryptedBytes, 0, combined, IV.length, encryptedBytes.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public String decrypt(String encryptedData) throws Exception
    {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(MODE);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        IvParameterSpec ivSpec = new IvParameterSpec(decodedBytes, 0, 16); // Extract IV from the combined data
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes, 16, decodedBytes.length - 16);
        return new String(decryptedBytes, "UTF-8");
    }

    public static void main(String[] args) throws Exception
    {
        EncryptionServiceImpl impl = new EncryptionServiceImpl();
        String originalData = "Hello, AES in CBC mode!";
        String encryptedData = impl.encrypt(originalData);
        String decryptedData = impl.decrypt(encryptedData);

        System.out.println("Original Data: " + originalData);
        System.out.println("Encrypted Data: " + encryptedData);
        System.out.println("Decrypted Data: " + decryptedData);
    }

}
