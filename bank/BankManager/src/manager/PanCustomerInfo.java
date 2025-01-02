package manager;


import bank.AccountVO;
import common.AccountType;
import common.CommandDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

//*******************************************************************
// Name : PanCustomerInfo
// Type : Class
// Description :  고객 정보를 조회하여 사용자에게 표시하는 패널을 구현한 클래스
//                - 고객 이름, ID, 연락처, 주소와 함께 보유 계좌 목록을 표시
//                - 계좌 목록은 잔액 기준으로 내림차순 정렬되며, 각 계좌의 잔액, 예금 종류, 개설일 정보를 포함
//                - 서버에서 전달받은 CommandDTO 객체를 통해 고객 정보를 업데이트
//*******************************************************************
public class PanCustomerInfo extends JPanel implements ActionListener {
    private JLabel namelabel;
    private  JLabel Customer_name;
    private JLabel idlabel;
    private  JLabel Customer_id;
    private JLabel addresslabel;
    private  JLabel Customer_address;
    private JLabel phonelabel;
    private  JLabel Customer_phone;
    private JScrollPane scrollpane;
    private JTable accountsTable;
    private JButton mainButton;
    ManagerMain MainFrame;

    //*******************************************************************
    // Name : PanCustomerInfo()
    // Type : 생성자
    // Description :  PanCustomerInfo Class의 생성자 구현
    //*******************************************************************
    public PanCustomerInfo(ManagerMain parent) {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description : 고객 정보 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI() {
        setLayout(null);
        setBounds(0, 0, 480, 320);

        // 이름
        namelabel = new JLabel("이름: ");
        namelabel.setBounds(50, 20, 100, 20);
        namelabel.setHorizontalAlignment(JLabel.RIGHT);
        add(namelabel);

        Customer_name = new JLabel();
        Customer_name.setBounds(160, 20, 200, 20);
        add(Customer_name);

        // 아이디
        idlabel = new JLabel("id: ");
        idlabel.setBounds(50, 45, 100, 20);
        idlabel.setHorizontalAlignment(JLabel.RIGHT);
        add(idlabel);

        Customer_id = new JLabel();
        Customer_id.setBounds(160, 45, 200, 20);
        add(Customer_id);

        // 연락처
        phonelabel = new JLabel("phone: ");
        phonelabel.setBounds(50, 70, 100, 20);
        phonelabel.setHorizontalAlignment(JLabel.RIGHT);
        add(phonelabel);

        Customer_phone = new JLabel();
        Customer_phone.setBounds(160, 70, 200, 20);
        add(Customer_phone);

        // 주소
        addresslabel = new JLabel("address: ");
        addresslabel.setBounds(50, 95, 100, 20);
        addresslabel.setHorizontalAlignment(JLabel.RIGHT);
        add(addresslabel);

        Customer_address = new JLabel();
        Customer_address.setBounds(160, 95, 300, 20);
        add(Customer_address);

        // 계좌 리스트
        accountsTable = new JTable();
        scrollpane = new JScrollPane(accountsTable);
        scrollpane.setBounds(50, 120, 380, 100);
        add(scrollpane);

        // 확인 버튼
        mainButton = new JButton("확인");
        mainButton.setBounds(190, 240, 100, 30);
        mainButton.addActionListener(this);
        add(mainButton);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  확인 버튼 구현
    //                고객 정보 열람을 마치고 메인 화면으로 돌아가도록 구현
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == mainButton) {
            this.setVisible(false);
            MainFrame.display("Main");
        }
    }

    //*******************************************************************
    // Name : setCustomerData
    // Type : Method
    // Description :  서버에서 전달된 CommandDTO 객체를 기반으로 고객 정보를 화면에 표시
    //                - 보유 계좌 목록을 테이블 형식으로 표시하며, 잔액 기준으로 내림차순 정렬
    //                - 계좌 정보가 없을 경우 기본 테이블 형식을 표시
    //*******************************************************************
    public void setCustomerData(CommandDTO command) {
        String id = command.getId();
        String name = command.getName();
        String address = command.getAddress();
        String phone = command.getPhone();
        List<AccountVO> accounts = command.getAccountlist();
        String defaultMsg = "정보 없음";

        Customer_name.setText(name != null ? name : defaultMsg);
        Customer_id.setText(id != null ? id : defaultMsg);
        Customer_address.setText(address != null ? address : defaultMsg);
        Customer_phone.setText(phone != null ? phone : defaultMsg);

        if (accounts != null && !accounts.isEmpty()) {
            String[] column = {"계좌번호", "잔액 (원)", "예금종류", "개설일"};
            DefaultTableModel model = new DefaultTableModel(column, 0){
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            accounts.sort(Comparator.comparing(AccountVO::getBalance).reversed());

            for (AccountVO account : accounts) {
                String Type = "";
                if ( account.getType() == AccountType.CHECKING )
                    Type = "당좌예금";
                else if ( account.getType() == AccountType.SAVINGS )
                    Type = "저축예금";
                else if ( account.getType() == null )
                    Type = "정보없음";
                Object[] row = {BankUtils.displayAccountNo(account.getAccountNo()), BankUtils.displayBalance(account.getBalance()), Type, account.getOpenDate()};
                model.addRow(row);
            }

            accountsTable.setModel(model);

            // 열 중앙 정렬
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < accountsTable.getColumnCount(); i++) {
                accountsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        } else {
            accountsTable.setModel(new DefaultTableModel(new String[]{"계좌번호", "잔액 (원)", "예금종류", "개설일"}, 0){
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
        }

        repaint(); // UI를 다시 렌더링
        revalidate(); // 레이아웃을 갱신
    }
}