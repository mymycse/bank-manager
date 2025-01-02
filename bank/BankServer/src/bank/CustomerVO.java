package bank;

import common.AccountType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//*******************************************************************
// Name : CustomerVO
// Type : Class
// Description :  고객정보를 정의 하기 위해 필요한 VO(ValueObject)이다.
//                생성자와, 오브젝트 내부 데이터 get, set 동작이 구현되어 있다.
//                오브젝트 형태로 txt에 저장할수 있도록 implements Serializable를 통해
//                직렬화 되어있다.
//*******************************************************************
public class CustomerVO implements Serializable {
    private String id;
    private String name;
    private String password;
    private String address;
    private String phone;
    private long checking_balance;
    private long saving_balance;
    private long total_balance;
    private List<AccountVO> accounts;



    public CustomerVO() {
    }

    public CustomerVO(String id, String name, String password, String phone, String address, List<AccountVO> accounts) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.accounts = accounts;
        setChecking_balance(accounts);
        setSaving_balance(accounts);
        setSaving_balance(accounts);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<AccountVO> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountVO> accounts) {this.accounts = accounts;}

    public long getChecking_balance() {
        return checking_balance;
    }

    public void setChecking_balance(List<AccountVO> accounts){
        this.checking_balance=0;
        if(accounts.size()==0){
             this.checking_balance=0;
        }
        else {
            for (int i = 0; i < accounts.size(); i++) {
                if(accounts.get(i).getType()==AccountType.CHECKING){
                    this.checking_balance+=accounts.get(i).getBalance();
                }
                else{
                    this.checking_balance+=0;
                }
            }
        }
    }

    public long getSaving_balance() {
        return saving_balance;
    }

    public void setSaving_balance(List<AccountVO> accounts){
        this.saving_balance=0;
        if(accounts.size()==0){
            this.saving_balance=0;
        }
        else {
            for (int i = 0; i < accounts.size(); i++) {
                if(accounts.get(i).getType()==AccountType.SAVINGS){
                    this.saving_balance+=accounts.get(i).getBalance();
                }
                else{
                    this.checking_balance+=0;
                }
            }
        }
    }

    public long getTotal_balance() {
        setTotal_balance();
        return total_balance;
    }

    public void setTotal_balance() {
        this.total_balance=0;
        for ( AccountVO account : accounts) {
            this.total_balance += account.getBalance();
        }
    }

    @Override
    public String toString() {
        return "CustomerVO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", account=" + accounts +
                '}';
    }
}
