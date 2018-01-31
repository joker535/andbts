package cn.guye.bts.contorl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.guye.bitshares.ErrorCode;
import cn.guye.bitshares.crypto.ECKey;
import cn.guye.bitshares.fc.crypto.aes;
import cn.guye.bitshares.fc.crypto.sha256_object;
import cn.guye.bitshares.fc.crypto.sha512_object;
import cn.guye.bitshares.fc.io.base_encoder;
import cn.guye.bitshares.fc.io.datastream_encoder;
import cn.guye.bitshares.fc.io.datastream_size_encoder;
import cn.guye.bitshares.fc.io.RawType;
import cn.guye.bitshares.wallet.AccountObject;
import cn.guye.bitshares.wallet.BrainKey;
import cn.guye.bitshares.wallet.PrivateKey;
import cn.guye.bitshares.wallet.PublicKey;


/**
 * Created by nieyu2 on 18/1/24.
 */

public class MyWallet {
    public boolean is_new() {
        return false;
    }

    class wallet_object {
        sha256_object chain_id;
        List<AccountObject> my_accounts = new ArrayList<>();
        ByteBuffer cipher_keys;
        HashMap<String, List<PublicKey>> extra_keys = new HashMap<>();
        String ws_server = "";
        String ws_user = "";
        String ws_password = "";

        public void update_account(AccountObject accountObject) {
            boolean bUpdated = false;
            for (int i = 0; i < my_accounts.size(); ++i) {
                if (my_accounts.get(i).getObjectId().equals(accountObject.getObjectId())) {
                    my_accounts.remove(i);
                    my_accounts.add(accountObject);
                    bUpdated = true;
                    break;
                }
            }

            if (bUpdated == false) {
                my_accounts.add(accountObject);
            }
        }
    }

    static class plain_keys {
        Map<PublicKey, String> keys;
        sha512_object checksum;

        public void write_to_encoder(base_encoder encoder) {
            RawType rawType = new RawType();

            rawType.pack(encoder, BigDecimal.valueOf(keys.size()));
            for (Map.Entry<PublicKey, String> entry : keys.entrySet()) {
                encoder.write(entry.getKey().getKeyByte());

                byte[] byteValue = entry.getValue().getBytes();
                rawType.pack(encoder, BigDecimal.valueOf(byteValue.length));
                encoder.write(byteValue);
            }
            encoder.write(checksum.hash);
        }

        public static plain_keys from_input_stream(InputStream inputStream) {
            plain_keys keysResult = new plain_keys();
            keysResult.keys = new HashMap<>();
            keysResult.checksum = new sha512_object();

            RawType rawType = new RawType();
            BigDecimal size = rawType.unpack(inputStream);
            try {
                for (int i = 0; i < size.longValue(); ++i) {
                    byte[] bytes = new byte[33];
                    inputStream.read(bytes);
                    PublicKey publicKeyType = new PublicKey(bytes);

                    BigDecimal strSize = rawType.unpack(inputStream);
                    byte[] byteBuffer = new byte[strSize.intValue()];
                    inputStream.read(byteBuffer);

                    String strPrivateKey = new String(byteBuffer);

                    keysResult.keys.put(publicKeyType, strPrivateKey);
                }

                inputStream.read(keysResult.checksum.hash);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return keysResult;
        }


    }

    private wallet_object mWalletObject;
    private boolean mbLogin = false;
    private HashMap<PublicKey, PrivateKey> mHashMapPub2Priv = new HashMap<>();
    private sha512_object mCheckSum = new sha512_object();

    public int import_brain_key(String strAccountNameOrId, String strBrainKey) {
        AccountObject accountObject = get_account(strAccountNameOrId);
        if (accountObject == null) {
            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
        }

        Map<PublicKey, PrivateKey> mapPublic2Private = new HashMap<>();
        for (int i = 0; i < 10; ++i) {
            BrainKey brainKey = new BrainKey(strBrainKey, i);
            ECKey ecKey = brainKey.getPrivateKey();
            PrivateKey privateKey = new PrivateKey(ecKey.getPrivKeyBytes());
            PublicKey publicKeyType = privateKey.get_public_key();

            if (accountObject.active.isExist(publicKeyType) == false &&
                    accountObject.owner.isExist(publicKeyType) == false &&
                    accountObject.options.getMemoKey().compare(publicKeyType) == false) {
                continue;
            }
            mapPublic2Private.put(publicKeyType, privateKey);
        }

        if (mapPublic2Private.isEmpty() == true) {
            return ErrorCode.ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY;
        }

        mWalletObject.update_account(accountObject);

        List<PublicKey> listPublicKeyType = new ArrayList<>();
        listPublicKeyType.addAll(mapPublic2Private.keySet());

        mWalletObject.extra_keys.put(accountObject.getObjectId(), listPublicKeyType);
        mHashMapPub2Priv.putAll(mapPublic2Private);

        encrypt_keys();

        return 0;
    }

    public int import_key(String account_name_or_id,
                          String wif_key)  {
        assert (is_locked() == false && is_new() == false);

        PrivateKey privateKeyType = new PrivateKey(wif_key);

        PublicKey publicKey = privateKeyType.get_public_key();

        AccountObject accountObject = get_account(account_name_or_id);
        if (accountObject == null) {
            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
        }

        /*List<account_object> listAccountObject = lookup_account_names(account_name_or_id);
        // 进行publicKey的比对
        if (listAccountObject.isEmpty()) {
            return -1;
        }

        account_object accountObject = listAccountObject.get(0);*/
        if (accountObject.active.isExist(publicKey) == false &&
                accountObject.owner.isExist(publicKey) == false &&
                accountObject.options.getMemoKey().compare(publicKey) == false) {
            return -1;
        }

        mWalletObject.update_account(accountObject);

        List<PublicKey> listPublicKeyType = new ArrayList<>();
        listPublicKeyType.add(publicKey);

        mWalletObject.extra_keys.put(accountObject.getObjectId(), listPublicKeyType);
        mHashMapPub2Priv.put(publicKey, privateKeyType);

        encrypt_keys();

        // 保存至文件

        return 0;
    }

    private AccountObject get_account(String account_name_or_id) {
        return null;
    }

    public int import_keys(String account_name_or_id,
                           String wif_key_1,
                           String wif_key_2)  {
        assert (is_locked() == false && is_new() == false);

        PrivateKey privateKeyType1 = new PrivateKey(wif_key_1);
        PrivateKey privateKeyType2 = new PrivateKey(wif_key_2);

        PublicKey publicKey1 = privateKeyType1.get_public_key();
        PublicKey publicKey2 = privateKeyType1.get_public_key();


        AccountObject accountObject = get_account(account_name_or_id);
        if (accountObject == null) {
            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
        }

        /*List<account_object> listAccountObject = lookup_account_names(account_name_or_id);
        // 进行publicKey的比对
        if (listAccountObject.isEmpty()) {
            return -1;
        }

        account_object accountObject = listAccountObject.get(0);*/
        if (accountObject.active.isExist(publicKey1) == false &&
                accountObject.owner.isExist(publicKey1) == false &&
                accountObject.options.getMemoKey().compare(publicKey1) == false) {
            return -1;
        }

        if (accountObject.active.isExist(publicKey2) == false &&
                accountObject.owner.isExist(publicKey2) == false &&
                accountObject.options.getMemoKey().compare(publicKey2) == false) {
            return -1;
        }



        mWalletObject.update_account(accountObject);

        List<PublicKey> listPublicKeyType = new ArrayList<>();
        listPublicKeyType.add(publicKey1);
        listPublicKeyType.add(publicKey2);

        mWalletObject.extra_keys.put(accountObject.getObjectId(), listPublicKeyType);
        mHashMapPub2Priv.put(publicKey1, privateKeyType1);
        mHashMapPub2Priv.put(publicKey2, privateKeyType2);

        encrypt_keys();

        // 保存至文件
        return 0;
    }

    public int import_account_password(String strAccountName,
                                       String strPassword) {

        // try the wif key at first time, then use password model. this is from the js code.
        /*int nRet = import_key(strAccountName, strPassword);
        if (nRet == 0) {
            return nRet;
        }*/

        PrivateKey privateActiveKey = PrivateKey.from_seed(strAccountName + "active" + strPassword);
        PrivateKey privateOwnerKey = PrivateKey.from_seed(strAccountName + "owner" + strPassword);

        PublicKey publicActiveKeyType = privateActiveKey.get_public_key();
        PublicKey publicOwnerKeyType = privateOwnerKey.get_public_key();

        AccountObject accountObject = get_account(strAccountName);
        if (accountObject == null) {
            return ErrorCode.ERROR_NO_ACCOUNT_OBJECT;
        }

        if (accountObject.active.isExist(publicActiveKeyType) == false &&
                accountObject.owner.isExist(publicActiveKeyType) == false &&
                accountObject.active.isExist(publicOwnerKeyType) == false &&
                accountObject.owner.isExist(publicOwnerKeyType) == false){
            return ErrorCode.ERROR_PASSWORD_INVALID;
        }

        List<PublicKey> listPublicKeyType = new ArrayList<>();
        listPublicKeyType.add(publicActiveKeyType);
        listPublicKeyType.add(publicOwnerKeyType);
        mWalletObject.update_account(accountObject);
        mWalletObject.extra_keys.put(accountObject.getObjectId(), listPublicKeyType);
        mHashMapPub2Priv.put(publicActiveKeyType, privateActiveKey);
        mHashMapPub2Priv.put(publicOwnerKeyType, privateOwnerKey);

        encrypt_keys();

        // 保存至文件

        return 0;
    }

    public boolean is_locked() {
        if (mWalletObject.cipher_keys.array().length > 0 &&
                mCheckSum.equals(new sha512_object())) {
            return true;
        }

        return false;
    }

    private void encrypt_keys() {
        plain_keys data = new plain_keys();
        data.keys = new HashMap<>();
        for (Map.Entry<PublicKey, PrivateKey> entry : mHashMapPub2Priv.entrySet()) {
            data.keys.put(entry.getKey(), entry.getValue().toString());
        }
        data.checksum = mCheckSum;

        datastream_size_encoder sizeEncoder = new datastream_size_encoder();
        data.write_to_encoder(sizeEncoder);
        datastream_encoder encoder = new datastream_encoder(sizeEncoder.getSize());
        data.write_to_encoder(encoder);

        byte[] byteKey = new byte[32];
        System.arraycopy(mCheckSum.hash, 0, byteKey, 0, byteKey.length);
        byte[] ivBytes = new byte[16];
        System.arraycopy(mCheckSum.hash, 32, ivBytes, 0, ivBytes.length);

        ByteBuffer byteResult = aes.encrypt(byteKey, ivBytes, encoder.getData());

        mWalletObject.cipher_keys = byteResult;

        return;

    }

    public int lock() {
        encrypt_keys();

        mCheckSum = new sha512_object();
        mHashMapPub2Priv.clear();

        return 0;
    }

    public int unlock(String strPassword) {
        assert(strPassword.length() > 0);
        sha512_object passwordHash = sha512_object.create_from_string(strPassword);
        byte[] byteKey = new byte[32];
        System.arraycopy(passwordHash.hash, 0, byteKey, 0, byteKey.length);
        byte[] ivBytes = new byte[16];
        System.arraycopy(passwordHash.hash, 32, ivBytes, 0, ivBytes.length);

        ByteBuffer byteDecrypt = aes.decrypt(byteKey, ivBytes, mWalletObject.cipher_keys.array());
        if (byteDecrypt == null || byteDecrypt.array().length == 0) {
            return -1;
        }

        plain_keys dataResult = plain_keys.from_input_stream(
                new ByteArrayInputStream(byteDecrypt.array())
        );

        for (Map.Entry<PublicKey, String> entry : dataResult.keys.entrySet()) {
            PrivateKey privateKeyType = new PrivateKey(entry.getValue());
            mHashMapPub2Priv.put(entry.getKey(), privateKeyType);
        }

        mCheckSum = passwordHash;
        if (passwordHash.equals(dataResult.checksum)) {
            return 0;
        } else {
            return -1;
        }
    }
}
