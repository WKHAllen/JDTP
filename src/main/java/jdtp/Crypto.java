package jdtp;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.util.Arrays;

/**
 * Crypto utilities.
 */
class Crypto {
    /**
     * The RSA key size.
     */
    private static final int rsaKeySize = 4096;

    /**
     * The RSA key generation algorithm.
     */
    private static final String rsaKeyGenAlgorithm = "RSA";

    /**
     * The RSA cipher algorithm.
     */
    private static final String rsaCipherAlgorithm = "RSA";

    /**
     * The AES key size.
     */
    private static final int aesKeySize = 32;

    /**
     * The AES IV size.
     */
    private static final int aesIVSize = 16;

    /**
     * The AES key generation algorithm.
     */
    private static final String aesKeyGenAlgorithm = "AES";

    /**
     * The AES cipher algorithm.
     */
    private static final String aesCipherAlgorithm = "AES/CBC/PKCS5Padding";

    /**
     * Generate a pair of RSA keys.
     *
     * @return The generated key pair.
     * @throws NoSuchAlgorithmException When the key generation algorithm is invalid.
     */
    public static KeyPair newRSAKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(rsaKeyGenAlgorithm);
        keyGen.initialize(rsaKeySize);
        return keyGen.generateKeyPair();
    }

    /**
     * Encrypt data with RSA.
     *
     * @param publicKey The RSA public key.
     * @param plaintext The data to encrypt.
     * @return The encrypted data.
     * @throws NoSuchAlgorithmException  When the cipher algorithm is invalid.
     * @throws NoSuchPaddingException    When the cipher padding parameter is invalid.
     * @throws InvalidKeyException       When the RSA public key is invalid.
     * @throws IllegalBlockSizeException When the block size is invalid.
     * @throws BadPaddingException       When the padding is invalid.
     */
    public static byte[] rsaEncrypt(PublicKey publicKey, byte[] plaintext)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance(rsaCipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plaintext);
    }

    /**
     * Decrypt data with RSA.
     *
     * @param privateKey The RSA private key.
     * @param ciphertext The data to decrypt.
     * @return The decrypted data.
     * @throws NoSuchAlgorithmException  When the cipher algorithm is invalid.
     * @throws NoSuchPaddingException    When the cipher padding parameter is invalid.
     * @throws InvalidKeyException       When the RSA private key is invalid.
     * @throws IllegalBlockSizeException When the block size is invalid.
     * @throws BadPaddingException       When the padding is invalid.
     */
    public static byte[] rsaDecrypt(PrivateKey privateKey, byte[] ciphertext)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance(rsaCipherAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(ciphertext);
    }

    /**
     * Generate a new AES key.
     *
     * @return The generated AES key.
     * @throws NoSuchAlgorithmException When the key generation algorithm is invalid.
     */
    public static Key newAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(aesKeyGenAlgorithm);
        keyGen.init(aesKeySize * 8);
        return keyGen.generateKey();
    }

    /**
     * Encrypt data with AES.
     *
     * @param key       The AES key.
     * @param plaintext The data to encrypt.
     * @return The encrypted data.
     * @throws NoSuchAlgorithmException           When the cipher algorithm is invalid.
     * @throws NoSuchPaddingException             When the cipher padding parameter is invalid.
     * @throws InvalidKeyException                When the AES key is invalid.
     * @throws InvalidAlgorithmParameterException When the data cannot be encrypted by the cipher algorithm.
     * @throws IllegalBlockSizeException          When the block size is invalid.
     * @throws BadPaddingException                When the padding is invalid.
     */
    public static byte[] aesEncrypt(Key key, byte[] plaintext)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] ivBytes = new byte[aesIVSize];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(aesCipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] ciphertext = cipher.doFinal(plaintext);

        byte[] ciphertextWithIV = new byte[ivBytes.length + ciphertext.length];
        System.arraycopy(ivBytes, 0, ciphertextWithIV, 0, ivBytes.length);
        System.arraycopy(ciphertext, 0, ciphertextWithIV, ivBytes.length, ciphertext.length);

        return ciphertextWithIV;
    }

    /**
     * Decrypt data with AES.
     *
     * @param key              The AES key.
     * @param ciphertextWithIV The data to decrypt.
     * @return The decrypted data.
     * @throws NoSuchAlgorithmException           When the cipher algorithm is invalid.
     * @throws NoSuchPaddingException             When the cipher padding parameter is invalid.
     * @throws InvalidKeyException                When the AES key is invalid.
     * @throws InvalidAlgorithmParameterException When the data cannot be decrypted by the cipher algorithm.
     * @throws IllegalBlockSizeException          When the block size is invalid.
     * @throws BadPaddingException                When the padding is invalid.
     */
    public static byte[] aesDecrypt(Key key, byte[] ciphertextWithIV)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] ivBytes = Arrays.copyOfRange(ciphertextWithIV, 0, aesIVSize);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        byte[] ciphertext = Arrays.copyOfRange(ciphertextWithIV, aesIVSize, ciphertextWithIV.length);

        Cipher cipher = Cipher.getInstance(aesCipherAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(ciphertext);
    }
}
