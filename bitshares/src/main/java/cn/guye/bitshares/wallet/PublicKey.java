package cn.guye.bitshares.wallet;

import org.spongycastle.math.ec.ECPoint;

import java.io.Serializable;

import cn.guye.bitshares.crypto.ECKey;
import cn.guye.bitshares.models.Address;
import cn.guye.bitshares.models.ByteSerializable;

/**
 * Created by nelson on 11/30/16.
 */
public class PublicKey implements ByteSerializable, Serializable {
    private ECKey publicKey;

    private byte[] key_data = new byte[33];

    public PublicKey(byte[] key) {
        System.arraycopy(key, 0, key_data, 0, key_data.length);
        publicKey = ECKey.fromPublicOnly(key);
    }

    public PublicKey(ECKey key) {
        if(key.hasPrivKey()){
            throw new IllegalStateException("Passing a private key to PublicKey constructor");
        }
        this.publicKey = key;
        key_data = publicKey.getPrivKeyBytes();
    }

    public byte[] getKeyByte() {
        return key_data;
    }

    public static boolean is_canonical(compact_signature c) {
        /*return !(c.data[1] & 0x80)
                && !(c.data[1] == 0 && !(c.data[2] & 0x80))
                && !(c.data[33] & 0x80)
                && !(c.data[33] == 0 && !(c.data[34] & 0x80));*/

        boolean bCompareOne = ((c.data[1] & 0x80) == 0);
        boolean bCompareTwo = ((c.data[1] == 0) && ((c.data[2] & 0x80) == 0)) == false;
        boolean bCompareThree = ((c.data[33] & 0x80) == 0);
        boolean bCompareFour = ((c.data[33] == 0) && ((c.data[34] & 0x80) ==0)) == false;

        return bCompareOne && bCompareTwo && bCompareThree && bCompareFour;
    }

    public ECKey getKey(){
        return publicKey;
    }

    @Override
    public byte[] toBytes() {
        if(publicKey.isCompressed()) {
            return publicKey.getPubKey();
        }else{
            publicKey = ECKey.fromPublicOnly(ECKey.compressPoint(publicKey.getPubKeyPoint()));
            return publicKey.getPubKey();
        }
    }

    public String getAddress(){
        ECKey pk = ECKey.fromPublicOnly(publicKey.getPubKey());
        if(!pk.isCompressed()){
            ECPoint point = ECKey.compressPoint(pk.getPubKeyPoint());
            pk = ECKey.fromPublicOnly(point);
        }
        return new Address(pk).toString();
    }

    @Override
    public int hashCode() {
        return publicKey.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        PublicKey other = (PublicKey) obj;
        return this.publicKey.equals(other.getKey());
    }

    public boolean compare(PublicKey publicKeyType) {
        return equals(publicKeyType);
    }
}