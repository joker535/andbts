package cn.guye.bitshares.models.chain;

import com.google.common.primitives.UnsignedInteger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.guye.bitshares.fc.UnsignedShort;
import cn.guye.bitshares.fc.bitutil;
import cn.guye.bitshares.fc.crypto.ripemd160_object;
import cn.guye.bitshares.fc.crypto.sha256_object;
import cn.guye.bitshares.fc.io.RawType;
import cn.guye.bitshares.models.Authority;
import cn.guye.bitshares.models.chain.Operations;
import cn.guye.bitshares.wallet.types;

public class transaction {

    private static final int REF_BLOCK_NUM_BYTES = 2;
    private static final int REF_BLOCK_PREFIX_BYTES = 4;
    private static final int REF_BLOCK_EXPIRATION_BYTES = 4;

    public class required_authorities {
        public List<String> active;
        public List<String> owner;
        public List<Authority> other;
    }

    private int refBlockNum;
    private long refBlockPrefix;
    private Date expiration;
    public List<Operations> operations = new ArrayList<>();
    public Set extensions = new HashSet();


    public void set_reference_block(long head_block_number, String head_block_id, long relative_expiration) {
        String hashData = head_block_id.substring(8, 16);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < 8; i = i + 2){
            builder.append(hashData.substring(6 - i, 8 - i));
        }
        this.setRefBlockNum(head_block_number);
        this.setRefBlockPrefix(head_block_id);
        this.expiration = new Date(relative_expiration);
    }

    /**
     * Plain setter for the ref_block_num field, assuming its value is exactly the one passed in the argument.
     * @param refBlockNum: The 'ref_block_num' field.
     */
    public void setRefBlockNum(int refBlockNum) {
        this.refBlockNum = refBlockNum;
    }

    /**
     * Setter that receives the block number, and takes the 16 lower bits of it to obtain the
     * 'ref_block_num' value.
     * @param blockNumber: The block number.
     */
    public void setRefBlockNum(long blockNumber){
        this.refBlockNum = ((int) blockNumber ) & 0xFFFF;
    }

    /**
     * Plain setter fot the 'ref_block_prefix' field, assumint its value is exactly the one passed in the argument.
     * @param refBlockPrefix
     */
    public void setRefBlockPrefix(long refBlockPrefix) {
        this.refBlockPrefix = refBlockPrefix;
    }

    /**
     * Setter that receives the head block id, and turns it into the little format required for the
     * 'ref_block_prefix' field.
     * @param headBlockId: The head block id as obtained from the network updates.
     */
    public void setRefBlockPrefix(String headBlockId){
        String hashData = headBlockId.substring(8, 16);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < 8; i = i + 2){
            builder.append(hashData.substring(6 - i, 8 - i));
        }
        this.refBlockPrefix = Long.parseLong(builder.toString(), 16);
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

    public byte[] getBlockBytes() {
        // Allocating a fixed length byte array, since we will always need
        // 2 bytes for the ref_block_num value
        // 4 bytes for the ref_block_prefix value
        // 4 bytes for the relative_expiration
        long expiration = this.expiration.getTime();
        byte[] result = new byte[REF_BLOCK_NUM_BYTES + REF_BLOCK_PREFIX_BYTES + REF_BLOCK_EXPIRATION_BYTES];
        for(int i = 0; i < result.length; i++){
            if(i < REF_BLOCK_NUM_BYTES){
                result[i] = (byte) (this.refBlockNum >> 8 * i);
            }else if(i >= REF_BLOCK_NUM_BYTES && i < REF_BLOCK_NUM_BYTES + REF_BLOCK_PREFIX_BYTES){
                result[i] = (byte) (this.refBlockPrefix >> 8 * (i - REF_BLOCK_NUM_BYTES));
            }else{
                result[i] = (byte) (expiration >> 8 * (i - REF_BLOCK_NUM_BYTES + REF_BLOCK_PREFIX_BYTES));
            }
        }
        return result;
    }

    public sha256_object sig_digest(sha256_object chain_id) {
        // // TODO: 07/09/2017 这里还未处理
        sha256_object.encoder enc = new sha256_object.encoder();

        enc.write(chain_id.hash, 0, chain_id.hash.length);
        RawType rawTypeObject = new RawType();
        enc.write(getBlockBytes());
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
