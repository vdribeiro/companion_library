package companion.support.v8.security;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import companion.support.v8.lang.ParsingUtils;
import companion.support.v8.util.LogHelper;

/**
 * This class handles signature, keys and other general cryptographic methods.
 * 
 * @author Vitor Ribeiro
 *
 */
public class Cryptography {

	/** Log tag. */
	private static final String TAG = Cryptography.class.getSimpleName();

	/** This prevents the class from being instantiated. 
	 */
	private Cryptography() {
	}

	/* PROVIDERS */
	/** Lists all available security providers in the smartphone.
	 * @return string array of all security providers.
	 */
	public static String[] getProviders() {
		Provider[] providers = Security.getProviders();
		int size = providers.length;

		String[] list = new String[size];
		for (int i = 0; i < size; i++) {
			list[i] = providers[i].getName();
		}

		return list;
	}

	/**
	 * Lists all available security providers and its details, like services and algorithms.
	 * */
	public static void printProviders() {
		for (Provider provider : Security.getProviders()) {
			LogHelper.i(TAG,"Provider: " + provider.getName());
			for (Provider.Service service : provider.getServices())	{
				LogHelper.i(TAG,"Algorithm: " + service.getAlgorithm());
			}
		}
	}

	/**
	 * Generate Checksum with a secure one-way hash function.
	 * 
	 * @param data bytes of data to generate hash.
	 * @param algorithm used (like MD or SHA).
	 * @return checksum bytes.
	 */
	public static byte[] hash(byte[] data, String algorithm) {
		byte[] checksum = null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(algorithm);
			md.reset();
			checksum = md.digest(data);
		} catch (NoSuchAlgorithmException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		return checksum;
	}

	/**
	 * Generate Checksum with a secure one-way hash function.
	 * 
	 * @param path of the file to generate hash.
	 * @param algorithm used (like MD or SHA).
	 * @return checksum bytes.
	 */
	public static byte[] hashFile(String path, String algorithm) {
		byte[] checksum = null;
		MessageDigest md = null;
		InputStream fis = null;
		try {
			md = MessageDigest.getInstance(algorithm);
			md.reset();

			fis = new FileInputStream(path);
			byte[] buffer = new byte[1024];
			int numRead = 0;

			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					md.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			checksum = md.digest();
		} catch (NoSuchAlgorithmException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (FileNotFoundException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (IOException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException ex) {
				LogHelper.e(TAG, "Unidentified Error", ex);
			}
		}

		return checksum;
	}

	/**
	 * Encrypt data given a key.
	 *
	 * @param data bytes of data to encrypt.
	 * @param key used for encryption.
	 * @param algorithm used for encryption.
	 * @return encrypted bytes. 
	 *
	 */
	@SuppressLint("TrulyRandom")
	public static byte[] encrypt(byte[] data, Key key, String algorithm) {

		// Construct Cipher Algorithm
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(algorithm,"BC");
		} catch (NoSuchAlgorithmException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (NoSuchPaddingException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (NoSuchProviderException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		byte[] cipherData = null;
		try {
			cipherData = cipher.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (BadPaddingException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		return cipherData;
	}

	/**
	 * Decrypt data given a key.
	 *
	 * @param data bytes of data to decrypt.
	 * @param key used for decryption.
	 * @param algorithm used for decryption.
	 * @return decrypted bytes.
	 *
	 */
	public static byte[] decrypt(byte[] data, Key key, String algorithm) {

		// Construct Cipher Algorithm
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(algorithm,"BC");
		} catch (NoSuchAlgorithmException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (NoSuchPaddingException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (NoSuchProviderException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		try {
			cipher.init(Cipher.DECRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		byte[] cipherData = null;
		try {
			cipherData = cipher.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (BadPaddingException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		return cipherData;
	}

	/* 3DES */

	/**
	 * Converts encoded bytes in a 3DES Key.
	 * 
	 * @param key string to convert to key.
	 * @return a 3DES Key. 
	 *
	 */
	public static SecretKey generateTripleDESkey(String key) {
		byte[] encryptKey = null;
		try {
			encryptKey = key.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}

		DESedeKeySpec keySpec = null;
		try {
			keySpec = new DESedeKeySpec(encryptKey);
		} catch (InvalidKeyException e) {
			return null;
		}

		SecretKeyFactory secretKeyFactory = null;
		try {
			secretKeyFactory = SecretKeyFactory.getInstance("DESede");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

		SecretKey secretKey = null;
		try {
			secretKey = secretKeyFactory.generateSecret(keySpec);
		} catch (InvalidKeySpecException e) {
			return null;
		}

		return secretKey;
	}

	/* AES */

	/**
	 * Creates a AES symmetric key.
	 * 
	 * @return a AES Key object. 
	 *
	 */
	public static SecretKey generateAESSecretKey() {
		KeyGenerator generator = null;
		SecretKey key = null;
		try {
			generator = KeyGenerator.getInstance("AES", "BC");
			generator.init(128);
			key = generator.generateKey();
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		return key;
	}

	/**
	 * Converts encoded bytes in a AES Key.
	 * 
	 * @param enckey byte array to convert to key.
	 * @return a AES Key. 
	 *
	 */
	public static SecretKey generateAESSecretKey(byte[] enckey) {

		// Generate key specifications assuming that the key 
		// eas encoded according to the standards
		SecretKey key = null;
		try {
			key = new SecretKeySpec(enckey, "AES");
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		return key;
	}

	/**
	 * Encrypt data given a AES key.
	 *
	 * @param data bytes of data to encrypt.
	 * @param key AES Key used for encryption.
	 * @return encrypted bytes. 
	 *
	 */
	public static byte[] AESEncrypt(byte[] data, Key key) {
		return encrypt(data, key, "AES/ECB/PKCS5Padding");
	}

	/**
	 * Decrypt data given a AES key.
	 *
	 * @param data bytes of data to decrypt.
	 * @param key AES Key used for decryption.
	 * @return decrypted bytes.
	 *
	 */
	public static byte[] AESDecrypt(byte[] data, Key key) {
		return decrypt(data, key, "AES/ECB/PKCS5Padding");
	}

	/* RSA */

	/**
	 * Creates a RSA public key given a modulus and an exponent.
	 *
	 * @param Modulus key modulus.
	 * @param Exponent key exponent.
	 * @return a RSA Public Key object. 
	 *
	 */
	public static RSAPublicKey generateRSAPublicKey(BigInteger Modulus, BigInteger Exponent) {

		// Generate key specification assuming that the key
		// was encoded according to the standard
		RSAPublicKeySpec pubKeySpec = null;
		try {
			pubKeySpec = new RSAPublicKeySpec(Modulus, Exponent);
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// A KeyFactory object is needed to do the conversion
		KeyFactory keyFactory = null;
		try {
			keyFactory = KeyFactory.getInstance("RSA","BC");
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// Use the KeyFactory object to generate the keys from the key specifications
		RSAPublicKey pk = null;
		try {
			pk = (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
		} catch (InvalidKeySpecException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		return pk;
	}

	/**
	 * Creates a RSA private key given a modulus and an exponent.
	 * 
	 * @param Modulus key modulus.
	 * @param Exponent key exponent.
	 * @return a RSA Private Key object. 
	 *
	 */
	public static RSAPrivateKey generateRSAPrivateKey(BigInteger Modulus, BigInteger Exponent) {

		// Generate key specification assuming that the key
		// was encoded according to the standard
		RSAPrivateKeySpec privKeySpec = null;
		try {
			privKeySpec = new RSAPrivateKeySpec(Modulus, Exponent);
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// A KeyFactory object is needed to do the conversion
		KeyFactory keyFactory = null;
		try {
			keyFactory = KeyFactory.getInstance("RSA","BC");
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// Use the KeyFactory object to generate the keys from the key specifications
		RSAPrivateKey pk = null;
		try {
			pk = (RSAPrivateKey) keyFactory.generatePublic(privKeySpec);
		} catch (InvalidKeySpecException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		return pk;
	}

	/**
	 * Encrypt data given a RSA public key.
	 *
	 * @param data bytes of data to encrypt.
	 * @param key RSA Public Key used for encryption.
	 * @return encrypted bytes. 
	 *
	 */
	public static byte[] RSAEncrypt(byte[] data, RSAPublicKey key) {
		byte[] encrypt = encrypt(data, key, "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		if (encrypt==null) {
			encrypt = encrypt(data, key, "RSA/ECB/PKCS1Padding");
		}
		return encrypt;
	}

	/**
	 * Decrypt data given a RSA private key.
	 *
	 * @param data bytes of data to decrypt.
	 * @param key RSA Private Key used for decryption.
	 * @return decrypted bytes.
	 *
	 */
	public static byte[] RSADecrypt(byte[] data, RSAPrivateKey key) {
		byte[] decrypt = decrypt(data, key, "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"); 
		if (decrypt==null) {
			decrypt = decrypt(data, key, "RSA/ECB/PKCS1Padding");
		}
		return decrypt;
	}

	/**
	 * Get RSA private key from file.
	 *
	 * @param keyFileName key file path.
	 * @return a RSA Private Key object. 
	 *
	 */
	public static synchronized RSAPrivateKey getRSAPrivateKey(String keyFileName) {

		ObjectInputStream oin = null;
		try {
			oin = new ObjectInputStream(new BufferedInputStream(new FileInputStream(keyFileName)));
		} catch (FileNotFoundException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (IOException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		RSAPrivateKey privKey = null;
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA","BC");
			privKey = (RSAPrivateKey) fact.generatePrivate(keySpec);
			oin.close();
		} catch (Exception e) {
			printProviders();
			throw new RuntimeException("Spurious serialisation error: " + e.getMessage(), e);
		}

		return privKey;
	}

	/**
	 * Get RSA public key from file.
	 *
	 * @param keyFileName key file path.
	 * @return a RSA Public Key object. 
	 *
	 */
	public static synchronized RSAPublicKey getRSAPublicKey(String keyFileName) {

		ObjectInputStream oin = null;
		try {
			oin = new ObjectInputStream(new BufferedInputStream(new FileInputStream(keyFileName)));
		} catch (FileNotFoundException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		} catch (IOException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		RSAPublicKey pubKey = null;
		try {
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA","BC");
			pubKey = (RSAPublicKey) fact.generatePublic(keySpec);
			oin.close();
		} catch (Exception e) {
			throw new RuntimeException("Spurious serialisation error", e);
		}

		return pubKey;
	}

	/* DIGITAL SIGNATURE */
	/**
	 * Generates a DSA Key Pair.
	 * 
	 * @return a SHA1PRNG DSA Key Pair object. 
	 *
	 */
	public static KeyPair generateDSAKeyPair() {
		// Create a Key Pair Generator
		KeyPairGenerator keyGen = null;
		SecureRandom random = null;
		try {
			keyGen = KeyPairGenerator.getInstance("DSA");
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// Initialise a generator 512 strong
		keyGen.initialize(512, random);

		// Generate the Pair of Keys
		return keyGen.generateKeyPair();
	}

	/**
	 * Converts encoded bytes in a DSA Private Key
	 * 
	 * @param encprivkey byte array to convert to key.
	 * @return a DSA Private Key. 
	 *
	 */
	public static PrivateKey generateDSAPrivateKey(byte[] encprivkey) {

		// Generate key specification assuming that the key 
		// was encoded according to the standards
		PKCS8EncodedKeySpec privKeySpec = null;
		try {
			privKeySpec = new PKCS8EncodedKeySpec(encprivkey);
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// A KeyFactory object is needed to do the conversion
		KeyFactory keyFactory = null;
		try {
			keyFactory = KeyFactory.getInstance("DSA","BC");
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// Use the KeyFactory object to generate the key from the key specification
		PrivateKey privKey = null;
		try {
			privKey = keyFactory.generatePrivate(privKeySpec);
		} catch (InvalidKeySpecException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		return privKey;
	}

	/**
	 * Converts encoded bytes in a DSA Public Key.
	 * 
	 * @param encpubkey byte array to convert to key.
	 * @return a DSA Public Key. 
	 *
	 */
	public static PublicKey generateDSAPublicKey(byte[] encpubkey) {

		// Generate key specification assuming that the key 
		// was encoded according to the standards
		X509EncodedKeySpec pubKeySpec = null;
		try {
			pubKeySpec = new X509EncodedKeySpec(encpubkey);
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// A KeyFactory object is needed to do the conversion
		KeyFactory keyFactory = null;
		try {
			keyFactory = KeyFactory.getInstance("DSA","BC");
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// Use the KeyFactory object to generate the key from the key specification
		PublicKey pubKey = null;
		try {
			pubKey = keyFactory.generatePublic(pubKeySpec);
		} catch (InvalidKeySpecException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		return pubKey;
	}

	/**
	 * Converts encoded bytes in a DSA Key Pair.
	 * 
	 * @param encprivkey byte array to convert to private key.
	 * @param encpubkey byte array to convert to public key.
	 * @return a DSA Key Pair. 
	 *
	 */
	public static KeyPair generateDSAKeyPair(byte[] encprivkey, byte[] encpubkey) {

		// Generate key specifications assuming that the keys 
		// were encoded according to the standards
		PKCS8EncodedKeySpec privKeySpec = null;
		X509EncodedKeySpec pubKeySpec = null;
		try {
			privKeySpec = new PKCS8EncodedKeySpec(encprivkey);
			pubKeySpec = new X509EncodedKeySpec(encpubkey);
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// A KeyFactory object is needed to do the conversion
		KeyFactory keyFactory = null;
		try {
			keyFactory = KeyFactory.getInstance("DSA","BC");
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// Use the KeyFactory object to generate the keys from the key specifications
		PrivateKey privKey = null;
		PublicKey pubKey = null;
		try {
			privKey = keyFactory.generatePrivate(privKeySpec);
			pubKey = keyFactory.generatePublic(pubKeySpec);
		} catch (InvalidKeySpecException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// Generate KeyPair
		return new KeyPair(pubKey, privKey);
	}

	/**
	 * Sign data.
	 * 
	 * @param privKey DSA Private Key for the signature.
	 * @param id the current id.
	 * @param data byte array to sign.
	 * @return signed data bytes array. 
	 *
	 */
	public static byte[] DSASign(PrivateKey privKey, int id, byte[] data) {
		// Get a Signature Object
		Signature dsa;
		try {
			dsa = Signature.getInstance("SHA1withDSA","BC");
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// Initialize the Signature Object
		try {
			dsa.initSign(privKey);
		} catch (InvalidKeyException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// Supply the Signature Object the Data to Be Signed
		try {
			dsa.update(ParsingUtils.intToBytes(id));
			dsa.update(data);
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		// Generate the Signature
		byte[] realSig;
		try {
			realSig = dsa.sign();
		} catch (SignatureException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return null;
		}

		return realSig;
	}

	/**
	 * Verify the data signature.
	 * 
	 * @param signature the signature to verify.
	 * @param pubKey the public key.
	 * @param id the current id.
	 * @param data the data to verify.
	 * @return true if the signature was verified, false otherwise.
	 *
	 */
	public static boolean DSAVerify(byte[] signature, PublicKey pubKey, int id, byte[] data) {

		// Get a Signature Object
		Signature dsa;
		try {
			dsa = Signature.getInstance("SHA1withDSA","BC");
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return false;
		}

		// Initialise the Signature Object
		try {
			dsa.initVerify(pubKey);
		} catch (InvalidKeyException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return false;
		}

		// Supply the Signature Object the Data to Be Verified
		try {
			dsa.update(ParsingUtils.intToBytes(id));
			dsa.update(data);
		} catch (Exception e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return false;
		}

		// Verify the Signature
		boolean verify = false;
		try {
			verify = dsa.verify(signature);
		} catch (SignatureException e) {
			LogHelper.e(TAG, "Unidentified Error", e);
			return false;
		}

		return verify;
	}
}
