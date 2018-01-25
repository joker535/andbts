package cn.guye.bitshares.wallet;


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

import cn.guye.bitshares.crypto.Base58;
import cn.guye.bitshares.crypto.ECKey;
import cn.guye.bitshares.crypto.EcTools;
import cn.guye.bitshares.crypto.Parameters;
import cn.guye.bitshares.crypto.Point;
import cn.guye.bitshares.crypto.RandomSource;
import cn.guye.bitshares.errors.MalformedAddressException;
import cn.guye.bitshares.fc.crypto.sha256_object;
import cn.guye.bitshares.fc.crypto.sha512_object;

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
        try {
            ECNamedCurveParameterSpec secp256k1 = org.spongycastle.jce.ECNamedCurveTable.getParameterSpec("secp256k1");
            org.spongycastle.jce.spec.ECPrivateKeySpec privSpec = new org.spongycastle.jce.spec.ECPrivateKeySpec(new BigInteger(1, key_data), secp256k1);
            KeyFactory keyFactory = KeyFactory.getInstance("EC","SC");

            byte[] keyBytes = new byte[33];
            System.arraycopy(key_data, 0, keyBytes, 1, 32);
            BigInteger privateKeys = new BigInteger(keyBytes);
            BCECPrivateKey privateKey = (BCECPrivateKey) keyFactory.generatePrivate(privSpec);

            Point Q = EcTools.multiply(Parameters.G, privateKeys);

            //ECPoint ecPoint = ECKey.CURVE.getG().multiply(privateKeys);
            org.spongycastle.math.ec.ECPoint ecpubPoint = new org.spongycastle.math.ec.custom.sec.SecP256K1Curve().createPoint(Q.getX().toBigInteger(), Q.getY().toBigInteger());
            java.security.PublicKey publicKey = keyFactory.generatePublic(new org.spongycastle.jce.spec.ECPublicKeySpec(ecpubPoint, secp256k1));

            BCECPublicKey bcecPublicKey = (BCECPublicKey)publicKey;
            byte bytePublic[] = bcecPublicKey.getQ().getEncoded(true);

            return new PublicKey(bytePublic);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
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
//        try {
//            final HmacPRNG prng = new HmacPRNG(key_data);
//            RandomSource randomSource = new RandomSource() {
//                @Override
//                public void nextBytes(byte[] bytes) {
//                    prng.nextBytes(bytes);
//                }
//            };
//
//            while (true) {
//                InMemoryPrivateKey inMemoryPrivateKey = new InMemoryPrivateKey(key_data);
//                SignedMessage signedMessage = inMemoryPrivateKey.signHash(new Sha256Hash(digest.hash), randomSource);
//                byte[] byteCompact = signedMessage.bitcoinEncodingOfSignature();
//                signature = new compact_signature(byteCompact);
//
//                boolean bResult = PublicKey.is_canonical(signature);
//                if (bResult == true) {
//                    break;
//                }
//            }
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }

        return signature;
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
