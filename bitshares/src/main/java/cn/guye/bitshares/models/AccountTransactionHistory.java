package cn.guye.bitshares.models;

/**
 * Created by nieyu2 on 18/1/18.
 */

//  ex:    {
//         "id": "2.9.121825729",
//         "account": "1.2.643261",
//         "operation_id": "1.11.119742831",
//         "sequence": 104,
//         "next": "2.9.121824907"
//         },

public class    AccountTransactionHistory extends  GrapheneObject{

    public String account;
    public String operation_id;
    public long sequence;
    public String next;

    public AccountTransactionHistory(String id) {
        super(id);
    }
}
