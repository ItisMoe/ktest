package com.lau.edu.passGen;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

public class ImageEncryptor {

    private static final Scanner scanner = new Scanner(System.in);
    private static long seed = System.nanoTime();
    private static Random random = new Random(seed);

    public static String MainFunction(String input,int size) throws Exception {
        // Load the input image (to hash)
        File inputFile = new File("fingerprintImage.jpg");
        BufferedImage inputImage = ImageIO.read(inputFile);

        // Convert image to byte array
        byte[] imageBytes = convertImageToByteArray(inputImage);

        // Generate SHA-256 hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(imageBytes);

        // Generate a random 256-bit key
        byte[] randomKey = new byte[32]; // 256 bits are 32 bytes
        new SecureRandom().nextBytes(randomKey);

        // XOR the hashed image bytes with the random key
        byte[] xorKey = new byte[32];
        for (int i = 0; i < 32; i++) {
            xorKey[i] = (byte) (hashedBytes[i] ^ randomKey[i]);
        }

        // Load the second image to encrypt
        File secondImageFile = new File("secretImage.jpg");
        BufferedImage secondImage = ImageIO.read(secondImageFile);
        byte[] secondImageBytes = convertImageToByteArray(secondImage);

        // Encrypt the second image using AES with the XORed key
        SecretKeySpec secretKeySpec = new SecretKeySpec(xorKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(secondImageBytes);

        // Encode the encrypted bytes to Base64 for easier visualization/storage
        String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);
        System.out.println("Encrypted Image Base64: " + encryptedString);

        return handlePasswordModification(encryptedString,input,size);
    }

    private static byte[] convertImageToByteArray(BufferedImage image) throws Exception {
        File tempFile = File.createTempFile("image", ".tmp");
        ImageIO.write(image, "jpg", tempFile);
        byte[] imageBytes = java.nio.file.Files.readAllBytes(tempFile.toPath());
        tempFile.delete();
        return imageBytes;
    }

    private static String handlePasswordModification(String encryptedString,String input,int size) {
        System.out.print("Enter the length of your password (between 10 and 16): ");
        int passwordLength = size;
        int replaceOrAddLength = (int) Math.ceil((double) passwordLength / 3);

        System.out.print("Enter your password (up to " + passwordLength + " characters): ");
        String userPassword = input;

        int charactersMissing = passwordLength - userPassword.length();
        userPassword = modifyPassword(userPassword, replaceOrAddLength, charactersMissing, encryptedString);

        return userPassword;
    }

    private static String modifyPassword(String password, int modifications, int charactersMissing, String encryptedString) {
        if (charactersMissing > modifications) {
            // Scenario 1: Add all missing characters if more than modifications
            for (int i = 0; i < charactersMissing; i++) {
                int method = i % 3;
                password = addCharacter(password, encryptedString, method);
            }
        } else if (charactersMissing == modifications) {
            // Scenario 2: Add exactly the number of missing characters equal to modifications
            for (int i = 0; i < charactersMissing; i++) {
                int method = i % 3;
                password = addCharacter(password, encryptedString, method);
            }
        } else if (charactersMissing < modifications) {
            // Scenario 3: Add remaining missing characters, then replace the rest
            for (int i = 0; i < charactersMissing; i++) {
                int method = i % 3;
                password = addCharacter(password, encryptedString, method);
            }
            int replace = modifications - charactersMissing; // Number of characters to replace
            for (int i = 0; i < replace; i++) {
                int method = (charactersMissing + i) % 3;
                password = replaceCharacter(password, encryptedString, method);
            }
        } else {
            // Scenario 4: No characters are missing, replace characters (modification times) in the password
            for (int i = 0; i < modifications; i++) {
                int method = i % 3;
                password = replaceCharacter(password, encryptedString, method);
            }
        }
        return password;
    }

    private static String addCharacter(String password, String encryptedString, int method) {
        int index = getRandomIndex(encryptedString.length(), method);
        int asciiBase = encryptedString.charAt(index);
        int finalAscii = 32 + (Math.abs(index - asciiBase) % 95);
        return password + (char) finalAscii;
    }

    private static String replaceCharacter(String password, String encryptedString, int method) {
        if (password.isEmpty()) return password;
        int index = getRandomIndex(encryptedString.length(), method);
        int charIndex = random.nextInt(password.length());
        int asciiBase = encryptedString.charAt(index);
        int finalAscii = 32 + (Math.abs(index - asciiBase) % 95);
        return password.substring(0, charIndex) + (char) finalAscii + password.substring(charIndex + 1);
    }
    private static int getRandomIndex(int length, int method) {
        switch (method) {
            case 0: return generateRandomIndex(length);
            case 1: return middleSquareMethod(length);
            case 2: return xorShift(length);
            default: return 0;  // Fallback, should not happen
        }
    }

    private static int generateRandomIndex(int length) {
        long result = (1664525L * seed + 1013904223L) % length;
        if (result < 0) result += length;
        return (int) result;
    }

    private static int middleSquareMethod(int length) {
        long square = seed * seed;
        String squareStr = String.valueOf(square);
        int mid = squareStr.length() / 2;
        int number = Integer.parseInt(squareStr.substring(mid - 1, mid + 1));
        seed = number;
        return number % length;
    }

    private static int xorShift(int length) {
        seed ^= (seed << 13);
        seed ^= (seed >>> 17);
        seed ^= (seed << 5);
        long result = seed % length;
        if (result < 0) result += length;
        return (int) result;
    }
}
