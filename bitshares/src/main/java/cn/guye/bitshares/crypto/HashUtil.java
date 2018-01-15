package cn.guye.bitshares.crypto;

import static java.util.Arrays.copyOfRange;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import org.spongycastle.util.encoders.Hex;


public class HashUtil {

    private static final int MAX_ENTRIES = 100; // Should contain most commonly hashed values 
    public static final byte[] EMPTY_DATA_HASH = Hex.decode("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470");

    private static final MessageDigest sha256digest;
    
    static {
        try {
            sha256digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }
    
    public static byte[] sha256(byte[] input) {
    	return sha256digest.digest(input);
    }

	public static byte[] sha3(byte[] input) {
            byte[] result = SHA3Helper.sha3(input);
            return result;
	}

    /**
     * Calculates RIGTMOST160(SHA3(input)). This is used in address calculations.
     */
    public static byte[] sha3omit12(byte[] input) {
    	byte[] hash = sha3(input);
    	return copyOfRange(hash, 12, hash.length);
    }

    /**
     * @see #doubleDigest(byte[], int, int)
     */
    public static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is
     * standard procedure in Bitcoin. The resulting hash is in big endian form.
     */
    public static byte[] doubleDigest(byte[] input, int offset, int length) {
        synchronized (sha256digest) {
            sha256digest.reset();
            sha256digest.update(input, offset, length);
            byte[] first = sha256digest.digest();
            return sha256digest.digest(first);
        }
    }

}
