package cn.guye.bitshares.models;

import com.google.common.primitives.UnsignedInteger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import cn.guye.bitshares.fc.bitutil;
import cn.guye.bitshares.fc.crypto.ripemd160_object;
import cn.guye.bitshares.fc.crypto.sha256_object;
import cn.guye.bitshares.fc.io.RawType;
import cn.guye.bitshares.models.chain.Operations;
import cn.guye.bitshares.wallet.types;

public class transaction {
    public class required_authorities {
        public List<String> active;
        public List<String> owner;
        public List<Authority> other;
    }

    /**
     * Least significant 16 bits from the reference block number. If @ref relative_expiration is zero, this field
     * must be zero as well.
     */
    public BigInteger ref_block_num    = BigInteger.ZERO;
    /**
     * The first non-block-number 32-bits of the reference block ID. Recall that block IDs have 32 bits of block
     * number followed by the actual block hash, so this field should be set using the second 32 bits in the
     * @ref block_id_type
     */
    public UnsignedInteger ref_block_prefix = UnsignedInteger.ZERO;

    /**
     * This field specifies the absolute expiration for this transaction.
     */
    public Date expiration;
    public List<Operations> operations;
    public Set extensions;

    public ripemd160_object id() {
        return null;
    }

    public void set_reference_block(ripemd160_object reference_block) {
        ref_block_num = new BigInteger(String.valueOf((short) bitutil.endian_reverse_u32(reference_block.hash[0])));

        //ref_block_prefix = new UnsignedInteger(reference_block.hash[1]);
        ref_block_prefix = UnsignedInteger.fromIntBits(reference_block.hash[1]);
    }

    public void set_expiration(Date expiration_time) {
        expiration = expiration_time;
    }

    public required_authorities get_required_authorities() {
        required_authorities requiredAuthorities = new required_authorities();
        requiredAuthorities.active = new ArrayList<>();
        requiredAuthorities.owner = new ArrayList<>();
        requiredAuthorities.other = new ArrayList<>();

        for (Operations operationType : operations) {
            Operations.base_operation baseOperation = (Operations.base_operation) operationType;
            requiredAuthorities.active.addAll(baseOperation.get_required_active_authorities());
            requiredAuthorities.owner.addAll(baseOperation.get_required_owner_authorities());
            requiredAuthorities.other.addAll(baseOperation.get_required_authorities());
        }

        return requiredAuthorities;
    }

    public sha256_object sig_digest(sha256_object chain_id) {
        // // TODO: 07/09/2017 这里还未处理
        sha256_object.encoder enc = new sha256_object.encoder();

        enc.write(chain_id.hash, 0, chain_id.hash.length);
        RawType rawTypeObject = new RawType();
        enc.write(rawTypeObject.get_byte_array(ref_block_num.shortValue()));
        enc.write(rawTypeObject.get_byte_array(ref_block_prefix.intValue()));
        enc.write(rawTypeObject.get_byte_array(expiration));

        //enc.write(rawTypeObject.get_byte_array(operations.size()));
        rawTypeObject.pack(enc, BigDecimal.valueOf(operations.size()));
        for (Operations operationType : operations) {
            //enc.write(rawTypeObject.get_byte_array(operationType.nOperationType));
            rawTypeObject.pack(enc, BigDecimal.valueOf(operationType.type));
            Operations.base_operation baseOperation = (Operations.base_operation) operationType;
            baseOperation.write_to_encoder(enc);
        }
        //enc.write(rawTypeObject.get_byte_array(extensions.size()));
        rawTypeObject.pack(enc, BigDecimal.valueOf(extensions.size()));



        return enc.result();
    }
}
