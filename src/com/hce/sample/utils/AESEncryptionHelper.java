package com.hce.sample.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

// Encrypt and Decrypt for data.

public class AESEncryptionHelper {
	private static byte[] keyBytes = new byte[] { 0x0f, 0x01, 0x07, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
		0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x00, 0x10, 0x11, 0x12, 0x09, 0x14, 0x15, 0x11, 0x17 };


	// encrypt the clearText, base64 encode the cipher text and return it.
	public static byte[] encrypt(String clearText) {
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

		// init cipher
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] cipherText = null;
		try {
			cipherText = cipher.doFinal(clearText.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Base64.encode(cipherText, 10);
	}

	public static String decrypt(byte[] cipherText) {
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

		// init cipher
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			cipher.init(Cipher.DECRYPT_MODE, keySpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] clearText = null;
		try {
			clearText = cipher.doFinal(Base64.decode(new String(cipherText), 10));
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new String(clearText);
	}
}
