package jdtp;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.util.Arrays;

class Crypto {
    private static final int rsaKeySize = 4096;
    private static final String rsaKeyGenAlgorithm = "RSA";
    private static final String rsaCipherAlgorithm = "RSA";
    private static final int aesKeySize = 32;
    private static final int aesIVSize = 16;
    private static final String aesKeyGenAlgorithm = "AES";
    private static final String aesCipherAlgorithm = "AES/CBC/PKCS5Padding";

    public static KeyPair newRSAKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(rsaKeyGenAlgorithm);
        keyGen.initialize(rsaKeySize);
        return keyGen.generateKeyPair();
    }

    public static byte[] rsaEncrypt(PublicKey publicKey, byte[] plaintext)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance(rsaCipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plaintext);
    }

    public static byte[] rsaDecrypt(PrivateKey privateKey, byte[] ciphertext)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance(rsaCipherAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(ciphertext);
    }

    public static Key newAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(aesKeyGenAlgorithm);
        keyGen.init(aesKeySize * 8);
        return keyGen.generateKey();
    }

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
