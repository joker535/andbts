package cn.guye.bts.contorl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import cn.guye.bitshares.ErrorCode;
import cn.guye.bitshares.Util;
import cn.guye.bitshares.crypto.ECKey;
import cn.guye.bitshares.models.FullAccountObject;
import cn.guye.bitshares.models.backup.PrivateKeyBackup;
import cn.guye.bitshares.wallet.BrainKey;
import cn.guye.bitshares.wallet.PrivateKey;
import cn.guye.bitshares.wallet.PublicKey;
import cn.guye.bts.app.BtsApp;


/**
 * Created by nieyu2 on 18/1/24.
 */

public class MyWallet {

    private Map<String, PrivateKey> pub2pri = new HashMap<>();
    private Map<String, String> pub2epri = new HashMap<>();
    private List<String> pubs;

    public List<FullAccountObject> getAccountObject() {
        return accountObject;
    }

    public void setAccountObject(List<FullAccountObject> accountObject) {
        this.accountObject = accountObject;
    }

    private List<FullAccountObject> accountObject;

    public List<String> getAccounts() {
        return accounts;
    }

    private List<String> accounts = new ArrayList<>();

    private static MyWallet instance;

    private MyWallet(){};

    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            lock();
        }
    };

    public static MyWallet import_brain_key(String strBrainKey ,List<PrivateKeyBackup> pks,List<String> as , String pwd) {
        MyWallet myWallet = new MyWallet();

        Map<String, PrivateKey> mapPublic2Private = new HashMap<>();
        myWallet.pubs = new ArrayList<>();
        for (int i = 0 ; i < pks.size() ; i++){
            int sq = pks.get(i).brainkey_sequence;
            BrainKey brainKey = new BrainKey(strBrainKey, sq);
            ECKey ecKey = brainKey.getPrivateKey();
            PrivateKey privateKey = new PrivateKey(ecKey.getPrivKeyBytes());
            PublicKey publicKeyType = privateKey.get_public_key();
            mapPublic2Private.put(publicKeyType.getAddress(), privateKey);
            myWallet.pubs.add(publicKeyType.getAddress());
        }
        
        if (mapPublic2Private.isEmpty() == true) {
            return null;
        }
        myWallet.accounts.addAll(as);
        myWallet.pub2pri.putAll(mapPublic2Private);
        myWallet.encrypt_keys(pwd);
        myWallet.timer.schedule(myWallet.timerTask,30 * 1000);
        myWallet.save();
        return myWallet;
    }



    public static MyWallet import_key(String account_name_or_id,
                          String wif_key , String pwd)  {
        MyWallet myWallet = new MyWallet();
        PrivateKey privateKeyType = new PrivateKey(wif_key);

        PublicKey publicKey = privateKeyType.get_public_key();

        myWallet.pubs = new ArrayList<>();
        myWallet.pubs.add(publicKey.getAddress());
        myWallet.accounts.add(account_name_or_id);
        myWallet.pub2pri.put(publicKey.getAddress(), privateKeyType);
        myWallet.encrypt_keys(pwd);
        myWallet.timer.schedule(myWallet.timerTask,30 * 1000);
        myWallet.save();
        return myWallet;
    }

    public static MyWallet import_keys(String account_name_or_id,
                           String wif_key_1,
                           String wif_key_2,String pwd)  {
        MyWallet myWallet = new MyWallet();

        PrivateKey privateKeyType1 = new PrivateKey(wif_key_1);
        PrivateKey privateKeyType2 = new PrivateKey(wif_key_2);

        PublicKey publicKey1 = privateKeyType1.get_public_key();
        PublicKey publicKey2 = privateKeyType1.get_public_key();


        myWallet.pubs = new ArrayList<>();
        myWallet.pubs.add(publicKey1.getAddress());
        myWallet.pubs.add(publicKey2.getAddress());
        myWallet.accounts.add(account_name_or_id);
        myWallet.pub2pri.put(publicKey1.getAddress(), privateKeyType1);
        myWallet.pub2pri.put(publicKey2.getAddress(), privateKeyType2);

        myWallet.encrypt_keys(pwd);
        myWallet.timer.schedule(myWallet.timerTask,30 * 1000);

        myWallet.save();
        return myWallet;
    }

    public static MyWallet import_account_password(String strAccountName,
                                       String strPassword) {
        MyWallet myWallet = new MyWallet();

        PrivateKey privateActiveKey = PrivateKey.from_seed(strAccountName + "active" + strPassword);
        PrivateKey privateOwnerKey = PrivateKey.from_seed(strAccountName + "owner" + strPassword);

        PublicKey publicActiveKeyType = privateActiveKey.get_public_key();
        PublicKey publicOwnerKeyType = privateOwnerKey.get_public_key();

        myWallet.pubs = new ArrayList<>();
        myWallet.pubs.add(publicActiveKeyType.getAddress());
        myWallet.pubs.add(publicOwnerKeyType.getAddress());

        myWallet.pub2pri.put(publicActiveKeyType.getAddress(), privateActiveKey);
        myWallet.pub2pri.put(publicOwnerKeyType.getAddress(), privateOwnerKey);
        myWallet.accounts.add(strAccountName);
        myWallet.encrypt_keys(strPassword);
        myWallet.timer.schedule(myWallet.timerTask,30 * 1000);

        myWallet.save();

        return myWallet;
    }

    public static void setInstance(MyWallet instance) {
        MyWallet.instance = instance;
    }

    public static MyWallet getInstance() {
        return MyWallet.instance;
    }

    private void save() {
        File dir = BtsApp.instance.getDataDir();
        dir = new File(dir,"stor");
        BufferedWriter bufferedWriter = null;
        try {
            FileWriter fileWriter = new FileWriter(dir);
            bufferedWriter = new BufferedWriter(fileWriter);
            String json = toJson();
            bufferedWriter.write(json);
        } catch (FileNotFoundException e) {
            return ;
        } catch (IOException e) {
            return ;
        }finally {
            if(bufferedWriter != null){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public boolean is_locked() {
        if (pub2pri.size() == 0) {
            return true;
        }
        return false;
    }

    public void unlock(String strPassword) {
        Set<String> set = pub2epri.keySet();
        for (String k :set){
            byte[] p = strPassword.getBytes();
            byte[] pk = Util.hexToBytes(pub2epri.get(k));
            PrivateKey ppk = new PrivateKey(Util.decryptAES(pk,p));
            pub2pri.put(k,ppk);
        }
    }

    private void encrypt_keys(String pwd) {
        Set<String> set = pub2pri.keySet();
        for (String k :set){
            byte[] p = pwd.getBytes();
            byte[] pk = pub2pri.get(k).get_secret();
            pub2epri.put(k,Util.bytesToHex(Util.encryptAES(pk,p)));
        }
    }

    public int lock() {
        pub2pri.clear();
        return 0;
    }

    private String toJson(){
        Gson gson = new Gson();
        JsonObject object = new JsonObject();

        JsonElement jsonElement = gson.toJsonTree(pub2epri);
        object.add("pub2epri",jsonElement);
        jsonElement = gson.toJsonTree(pubs);
        object.add("pubs",jsonElement);
        jsonElement = gson.toJsonTree(accounts);
        object.add("accounts",jsonElement);
        return gson.toJson(object);
    }

    private static MyWallet fromJson(String json){
        Gson gson = new Gson();
        return gson.fromJson(json,MyWallet.class);
    }

    public static MyWallet load(){
        File dir = BtsApp.instance.getDataDir();
        dir = new File(dir,"stor");
        BufferedReader bufferedReader = null;
        try {
            FileReader fileReader = new FileReader(dir);
            bufferedReader = new BufferedReader(fileReader);
            String json = bufferedReader.readLine();
            return fromJson(json);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }finally {
            if(bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public boolean hasAccount(String seller) {

        for (FullAccountObject id:
                accountObject) {
            if(id.account.getObjectId().equals(seller)){
                return true;
            }
        }
        return false;
    }

    public ECKey getKey(String seller) {
        FullAccountObject object = null;
        for (FullAccountObject id:
                accountObject) {
            if(id.account.getObjectId().equals(seller)){
               object = id;
            }
        }

        if(object != null){
            List<PublicKey> pusk = object.account.active.getKeyAuthList();

            String address = pusk.get(0).getAddress();//TODO only first key

            PrivateKey p = pub2pri.get(address);
            if(p != null){
                return p.getEC();
            }else{
                return null;
            }
        }else{
            return null;
        }
    }
}
