package common;

import bank.AccountVO;
import bank.CustomerVO;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

//*******************************************************************
// Name : CommandDTO
// Type : Class
// Description :  ATM 과 Sever 사이의 통신 프로토콜을 정의 하기 위해 필요한 DTO(DataTransferObject)이다.
//                생성자와, 오브젝트 내부 데이터 get, set 동작이 구현되어 있다.
//*******************************************************************

public class CommandDTO implements Serializable {
    private RequestType requestType;
    private String id;
    private String name;
    private String password;
    private String userAccountNo;
    private String address;
    private String phone;
    private List<CustomerVO> customerList;
    private List<AccountVO> Accountlist;
    private String receivedAccountNo;
    private long amount;
    private long balance;
    private ResponseType responseType;
    private AccountType accountType;
    private Date openDate;

    public CommandDTO() {
    }

    public CommandDTO(RequestType requestType) {
        this.requestType = requestType;
    }

    public CommandDTO(ResponseType responseType) {
        this.responseType = responseType;
    }

    public CommandDTO(RequestType requestType, String userAccountNo) {
        this.requestType = requestType;
        this.userAccountNo = userAccountNo;
    }

    public CommandDTO(RequestType requestType, String userAccountNo, long amount) {
        this.requestType = requestType;
        this.userAccountNo = userAccountNo;
        this.amount = amount;
    }

    public CommandDTO(RequestType requestType, String id, String password) {
        this.requestType = requestType;
        this.id = id;
        this.password = password;
    }

    public CommandDTO(RequestType requestType, String id, String password, String name) {
        this.requestType = requestType;
        this.id = id;
        this.password = password;
        this.name=name;
    }

    public CommandDTO(RequestType requestType, String id, String password, String name, String phone, String address) {
        this.requestType = requestType;
        this.id = id;
        this.password = password;
        this.name=name;
        this.phone=phone;
        this.address=address;
    }

    public CommandDTO(RequestType requestType, String password, String userAccountNo, String receivedAccountNo, long amount) {
        this.requestType = requestType;
        this.password = password;
        this.userAccountNo = userAccountNo;
        this.receivedAccountNo = receivedAccountNo;
        this.amount = amount;
    }

    public CommandDTO(RequestType requestType, String userAccountNo, String receivedAccountNo, long amount, long balance) {
        this.requestType = requestType;
        this.userAccountNo = userAccountNo;
        this.receivedAccountNo = receivedAccountNo;
        this.amount = amount;
        this.balance = balance;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone(){return phone;}

    public void setPhone(String phone){this.phone=phone;}

    public String getAddress(){return address;}

    public void setAddress(String address){this.address=address;}

    public List<AccountVO> getAccountlist() {
        return Accountlist;
    }

    public void setAccountlist(List<AccountVO> Accountlist){
        this.Accountlist=Accountlist;
    }

    public String getUserAccountNo() {
        return userAccountNo;
    }

    public void setUserAccountNo(String userAccountNo) {
        this.userAccountNo = userAccountNo;
    }

    public String getReceivedAccountNo() {
        return receivedAccountNo;
    }

    public void setReceivedAccountNo(String receivedAccountNo) {
        this.receivedAccountNo = receivedAccountNo;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public Date getOpenDate() { return openDate;}
    public void setOpenDate(Date openDate) { this.openDate = openDate;}

    public List<CustomerVO> getCustomerList() { return customerList;}
    public void setCustomerList(List<CustomerVO> customerList) { this.customerList = customerList;}

    @Override
    public String toString() {
        return "CommandDTO{" +
                "requestType=" + requestType +
                ", responseType=" + responseType +
                ", id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", userAccountNo='" + userAccountNo + '\'' +
                ", receivedAccountNo='" + receivedAccountNo + '\'' +
                ", amount=" + amount +
                ", balance=" + balance +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", accountList=" + Accountlist +
                '}';
    }
}
