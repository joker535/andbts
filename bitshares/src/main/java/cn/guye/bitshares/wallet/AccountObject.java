package cn.guye.bitshares.wallet;

import java.util.List;

import cn.guye.bitshares.models.AccountOptions;
import cn.guye.bitshares.models.Authority;
import cn.guye.bitshares.models.GrapheneObject;

public class AccountObject extends GrapheneObject{
    public String membership_expiration_date;
    public String registrar;
    public String referrer;
    public String lifetime_referrer;
    public int network_fee_percentage;
    public int lifetime_referrer_fee_percentage;
    public int referrer_rewards_percentage;
    public String name;
    public Authority owner;
    public Authority active;
    public AccountOptions options;
    public String statistics;
    public List<String> whitelisting_accounts;
    public List<String> whitelisted_accounts;
    public List<String> blacklisted_accounts;
    public List<String> blacklisting_accounts;
    public List<Object> owner_special_authority;
    public List<Object> active_special_authority;
    public Integer top_n_control_flags;

    public AccountObject(String id) {
        super(id);
    }
}