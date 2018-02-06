package cn.guye.bitshares.models.chain;

import java.math.BigInteger;
import java.util.Date;

import cn.guye.bitshares.fc.crypto.ripemd160_object;
import cn.guye.bitshares.models.GrapheneObject;

public class dynamic_global_property_object extends GrapheneObject{

    public int     head_block_number = 0;
    public String head_block_id;         //block_id_type     head_block_id;
    public Date time;                  //time_point_sec    time;
    public String current_witness;       // witness_id_type   current_witness;
    public String    next_maintenance_time; // time_point_sec    next_maintenance_time;
    public  String    last_budget_time;      // time_point_sec    last_budget_time;
    public  long      witness_budget;
    public  int       accounts_registered_this_interval = 0;
    /**
     *  Every time a block is missed this increases by
     *  RECENTLY_MISSED_COUNT_INCREMENT,
     *  every time a block is found it decreases by
     *  RECENTLY_MISSED_COUNT_DECREMENT.  It is
     *  never less than 0.
     *
     *  If the recently_missed_count hits 2*UNDO_HISTORY then no new blocks may be pushed.
     */
    public int          recently_missed_count = 0;

    /**
     * The current absolute slot number.  Equal to the total
     * number of slots since genesis.  Also equal to the total
     * number of missed slots plus head_block_number.
     */
    public  long         current_aslot = 0;

    /**
     * used to compute witness participation.
     */

    public  BigInteger recent_slots_filled;

    /**
     * dynamic_flags specifies chain state properties that can be
     * expressed in one bit.
     */
    public  int dynamic_flags = 0;

    public int last_irreversible_block_num = 0;

    public dynamic_global_property_object(String id) {
        super(id);
    }
}