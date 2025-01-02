package common;


//*******************************************************************
// Name : RequestType
// Type : Enum
// Description :  ATM 이 Server에 요청할 기능을 Enum으로 나타낸 열거형 데이터를 구현
//                생성자와, 오브젝트 내부 데이터 get, set 동작이 구현되어 있다.
//*******************************************************************
public enum RequestType {
    VIEW("계좌조회", 10),
    TRANSFER("계좌이체", 20),
    DEPOSIT("입금", 30),
    WITHDRAW("출금", 40),
    LOGIN("로그인", 50),
    ADD_CUSTOMER("고객 추가",60),
    DELETE_CUSTOMER("고객 삭제", 70),
    ADD_ACCOUNT("계좌 추가",80),
    LOAD_ACCOUNT("계좌 불러오기",85),
    DELETE_ACCOUNT("계좌 삭제",90),
    VIEW_CUSTOMER("고객 조회",95),
    VIEW_ACCOUNT("계좌 조회", 97),
    BANK_INFO("은행 정보", 99),
    VIEW_CUSTOMER_LIST("고객 명단", 100),
    VIEW_ACCOUNT_LIST("계좌 리스트", 110),
    MANAGER_LOGIN("매니저 로그인",120);

    private String name;
    private int number;

    RequestType(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
