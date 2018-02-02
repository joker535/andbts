package cn.guye.bitshares.wallet;


import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.spongycastle.jce.spec.*;
import org.spongycastle.math.ec.ECPoint;

import cn.guye.bitshares.crypto.Base58;
import cn.guye.bitshares.crypto.ECKey;
import cn.guye.bitshares.crypto.EcTools;
import cn.guye.bitshares.crypto.HmacPRNG;
import cn.guye.bitshares.crypto.Parameters;

import cn.guye.bitshares.crypto.Point;
import cn.guye.bitshares.crypto.RandomSource;
import cn.guye.bitshares.errors.MalformedAddressException;
import cn.guye.bitshares.fc.crypto.sha256_object;
import cn.guye.bitshares.fc.crypto.sha512_object;

import static cn.guye.bitshares.crypto.ByteUtil.bigIntegerToBytes;

public class PrivateKey {
    private byte[] key_data = new byte[32];
    public PrivateKey(byte key[]) {
        System.arraycopy(key, 0, key_data, 0, key_data.length);
    }

    public PrivateKey(String strBase58) {
        byte wif_bytes[] = new byte[0];
        try {
            wif_bytes = Base58.decode(strBase58);
        } catch (MalformedAddressException e) {
            e.printStackTrace();
        }
        if (wif_bytes.length < 5) {
            throw new RuntimeException("Private key is not valid");
        }

        System.arraycopy(wif_bytes, 1, key_data, 0, key_data.length);

        SHA256Digest digest = new SHA256Digest();
        digest.update(wif_bytes, 0, wif_bytes.length - 4);
        byte[] hashCheck = new byte[32];
        digest.doFinal(hashCheck, 0);

        byte[] hashCheck2 = new byte[32];
        digest.update(hashCheck, 0, hashCheck.length);
        digest.doFinal(hashCheck2, 0);

        byte check[] = new byte[4];
        System.arraycopy(wif_bytes, wif_bytes.length - check.length, check, 0, check.length);

        byte[] check1 = new byte[4];
        byte[] check2 = new byte[4];
        System.arraycopy(hashCheck, 0, check1, 0, check1.length);
        System.arraycopy(hashCheck2, 0, check2, 0, check2.length);

        if (Arrays.equals(check1, check) == false &&
                Arrays.equals(check2, check) == false) {
            throw new RuntimeException("Private key is not valid");
        }
    }


    public byte[] get_secret() {
        return key_data;
    }

    public static PrivateKey generate() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDsA", "SC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            keyGen.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            return new PrivateKey(keyPair);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }

    public PublicKey get_public_key() {

        byte[] keyBytes = new byte[33];
        System.arraycopy(key_data, 0, keyBytes, 1, 32);

        ECKey ecKey = ECKey.fromPrivate(keyBytes);

        return new PublicKey(ecKey.getPubKey());

    }

    private PrivateKey(KeyPair ecKey){
        BCECPrivateKey privateKey = (BCECPrivateKey) ecKey.getPrivate();
        byte[] privateKeyGenerate = privateKey.getD().toByteArray();
        if (privateKeyGenerate.length == 33) {
            System.arraycopy(privateKeyGenerate, 1, key_data, 0, key_data.length);
        } else {
            System.arraycopy(privateKeyGenerate, 0, key_data, 0, key_data.length);
        }
    }

    public compact_signature sign_compact(sha256_object digest, boolean require_canonical ) {
        compact_signature signature = null;

        byte[] keyBytes = new byte[33];
        System.arraycopy(key_data, 0, keyBytes, 1, 32);

        ECKey ecKey = ECKey.fromPrivate(keyBytes);

        signature = new compact_signature(derEncode(ecKey.sign(digest.hash)));
        return signature;
    }

    public byte[] derEncode(ECKey.ECDSASignature s) { //todo emit Subclass instead, with cached encoding
        byte[] sigData = new byte[65];  // 1 header + 32 bytes for R + 32 bytes for S
        sigData[0] = s.v;
        System.arraycopy(bigIntegerToBytes(s.r, 32), 0, sigData, 1, 32);
        System.arraycopy(bigIntegerToBytes(s.s, 32), 0, sigData, 33, 32);
        return sigData;
    }

    public static PrivateKey from_seed(String strSeed) {
        sha256_object.encoder encoder = new sha256_object.encoder();

        encoder.write(strSeed.getBytes(Charset.forName("UTF-8")));
        PrivateKey privateKey = new PrivateKey(encoder.result().hash);

        return privateKey;
    }

    public sha512_object get_shared_secret(PublicKey publicKey) {
        ECKey ecPublicKey = ECKey.fromPublicOnly(publicKey.getKeyByte());
        ECKey ecPrivateKey = ECKey.fromPrivate(key_data);

        byte[] secret = ecPublicKey.getPubKeyPoint().multiply(ecPrivateKey.getPrivKey())
                .normalize().getXCoord().getEncoded();

        return sha512_object.create_from_byte_array(secret, 0, secret.length);
    }

}
