package bank;

import java.io.Serializable;

//*******************************************************************
// Name : ManagerVO
// Type : Class
// Description :  관리자 정보를 정의 하기 위해 필요한 VO(ValueObject)이다.
//                생성자와, 오브젝트 내부 데이터 get, set 동작이 구현되어 있다.
//                오브젝트 형태로 txt에 저장할수 있도록 implements Serializable를 통해
//                직렬화 되어있다.
//*******************************************************************
public class ManagerVO implements Serializable {
    private String id;
    private String pw;

    public ManagerVO() {
    }

    public ManagerVO(String id, String pw){
        this.id=id;
        this.pw=pw;
    }

    public String getId(){return id;}

    public void setId(String id){this.id=id;}

    public String getPw(){return pw;}

    public void setPw(String pw){this.pw=pw;}


    @Override
    public String toString() {
        return "Manager{" +
                "id='" + id + '\'' +
                ", password='" + pw + '\'' +
                '}';
    }


}
