package cn.guye.bitshares.models.chain;

import java.util.ArrayList;
import java.util.List;

import cn.guye.bitshares.fc.crypto.sha256_object;
import cn.guye.bitshares.models.transaction;
import cn.guye.bitshares.wallet.PrivateKey;
import cn.guye.bitshares.wallet.compact_signature;
import cn.guye.bitshares.wallet.types;

public class signed_transaction extends transaction {
    List<compact_signature> signatures = new ArrayList<>();

    public void sign(PrivateKey privateKeyType, sha256_object chain_id) {
        sha256_object digest = sig_digest(chain_id);
        signatures.add(privateKeyType.sign_compact(digest, true));
    }
}
