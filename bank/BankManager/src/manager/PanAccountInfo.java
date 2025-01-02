package manager;


import common.CommandDTO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

//*******************************************************************
// Name : PanAccountInfo
// Type : Class
// Description :  계좌 정보를 조회하여 사용자에게 표시하는 패널을 구현한 클래스
//                - 계좌번호, 고객명, 예금 종류, 잔액, 가입일자를 표시
//                - 서버에서 받은 CommandDTO 객체를 통해 계좌 정보를 업데이트
//                - 메인화면으로 돌아가는 버튼을 제공
//*******************************************************************
public class PanAccountInfo extends JPanel implements ActionListener {
    private JLabel accountNolabel, Text_AccountNo;
    private JLabel ownerlabel, Text_Owner;
    private JLabel typelabel, Text_Type;
    private JLabel balanceLabel, Text_Balance;
    private JLabel datelabel, Text_OpenDate;

    private JButton mainButton;
    ManagerMain MainFrame;

    //*******************************************************************
    // Name : PanAccountInfo()
    // Type : 생성자
    // Description :  PanAccountInfo Class의 생성자 구현
    //*******************************************************************
    public PanAccountInfo(ManagerMain parent) {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description :  계좌 정보 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI() {
        setLayout(null);
        setBounds(0,0,480,320);

        // 계좌번호
        accountNolabel = new JLabel("계좌번호: ");
        accountNolabel.setBounds(100, 20, 100, 20);
        accountNolabel.setHorizontalAlignment(JLabel.RIGHT);
        add(accountNolabel);

        Text_AccountNo = new JLabel();
        Text_AccountNo.setBounds(210, 20, 200, 20);
        add(Text_AccountNo);

        // 소유주
        ownerlabel = new JLabel("고객명: ");
        ownerlabel.setBounds(100, 50, 100, 20);
        ownerlabel.setHorizontalAlignment(JLabel.RIGHT);
        add(ownerlabel);

        Text_Owner = new JLabel();
        Text_Owner.setBounds(210, 50, 200, 20);
        add(Text_Owner);

        // 예금 종류
        typelabel = new JLabel("예금종류: ");
        typelabel.setBounds(100, 80, 100, 20);
        typelabel.setHorizontalAlignment(JLabel.RIGHT);
        add(typelabel);

        Text_Type = new JLabel();
        Text_Type.setBounds(210, 80, 200, 20);
        add(Text_Type);

        // 잔액
        balanceLabel = new JLabel("잔액: ");
        balanceLabel.setBounds(100, 110, 100, 20);
        balanceLabel.setHorizontalAlignment(JLabel.RIGHT);
        add(balanceLabel);

        Text_Balance = new JLabel();
        Text_Balance.setBounds(210, 110, 300, 20);
        add(Text_Balance);

        // 가입일자
        datelabel = new JLabel("가입일자: ");
        datelabel.setBounds(100, 140, 100, 20);
        datelabel.setHorizontalAlignment(JLabel.RIGHT);
        add(datelabel);

        Text_OpenDate = new JLabel();
        Text_OpenDate.setBounds(210, 140, 300, 20);
        add(Text_OpenDate);

        // 확인 버튼
        mainButton = new JButton("확인");
        mainButton.setBounds(190, 220, 100, 30);
        mainButton.addActionListener(this);
        add(mainButton);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  확인 버튼 구현
    //                계좌 정보 열람을 마치고 메인 화면으로 돌아가도록 구현
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == mainButton) {
            this.setVisible(false);
            MainFrame.display("Main");
        }
    }

    //*******************************************************************
    // Name : setAccountData()
    // Type : Method
    // Description :  서버에서 전달된 CommandDTO 객체를 기반으로 계좌 정보를 화면에 표시
    //                - 계좌번호, 고객명, 예금 종류, 잔액, 가입일자 등의 데이터를 각 라벨에 설정
    //                - 데이터가 없는 경우 '정보 없음' 이라는 기본 메시지 표시
    //                - 화면을 다시 그려 계좌 정보를 실시간으로 반영
    //*******************************************************************
    public void setAccountData(CommandDTO command) {
        String accountNo = command.getUserAccountNo();
        String name = command.getName();
        String type = command.getAccountType().toString();
        long balance = command.getBalance();
        Date date = command.getOpenDate();
        String defaultMsg = "정보 없음";

        Text_AccountNo.setText(accountNo != null ? BankUtils.displayAccountNo(accountNo) : defaultMsg);
        Text_Owner.setText(name != null ? name : defaultMsg);
        Text_Type.setText(type != null ? type.equals("CHECKING") ? "당좌예금계좌" : "저축예금계좌" : defaultMsg);
        Text_Balance.setText(BankUtils.displayBalance(balance) + " 원");
        Text_OpenDate.setText(date != null ? date.toString() : defaultMsg);

        repaint();
        revalidate();
    }
}
