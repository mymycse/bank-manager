package manager;

import bank.AccountVO;
import common.CommandDTO;
import common.RequestType;
import common.ResponseType;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

//*******************************************************************
// Name : PanViewAccountList
// Type : Class
// Description :  은행의 계좌 목록을 표시하는 패널을 구현한 Class
//                - 계좌 목록을 서버에서 받아와 JTable에 출력
//                - 총 보유 잔고를 계산하고 화면에 표시
//                - 사용자 인터페이스를 통해 계좌 목록을 불러오는 기능 제공
//                - 불러온 계좌 목록은 보유 잔고 기준으로 내림차순 정렬하여 표시
//*******************************************************************
public class PanViewAccountList extends JPanel implements ActionListener {
    private JTable accountTable;
    private JScrollPane accountScroller;
    private JLabel titlelabel, totallabel;
    private JButton loadButton, mainButton;
    ManagerMain MainFrame;

    //*******************************************************************
    // Name : PanViewAccountList()
    // Type : 생성자
    // Description :  PanViewAccountList Class의 생성자 구현
    //*******************************************************************
    public PanViewAccountList(ManagerMain parent) {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description : 계좌 목록을 표시하는 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI() {
        setLayout(null);
        setBounds(0,0,480,320);

        // 제목
        titlelabel = new JLabel("계좌 목록");
        titlelabel.setFont(new Font("Sanserif", Font.BOLD, 20));
        titlelabel.setSize(getWidth(), 60);
        titlelabel.setLocation(120,0);
        add(titlelabel);

        // 총 보유 잔고
        totallabel = new JLabel("총 보유 잔고: ");
        totallabel.setBounds(50,240,270,20);
        add(totallabel);

        // 계좌 정보
        accountTable = new JTable();
        accountScroller = new JScrollPane(accountTable);
        accountScroller.setBounds(50,70,350,150);
        add(accountScroller);
        resetTable();

        // 로드 버튼
        loadButton = new JButton("불러오기");
        loadButton.setBounds(270,15,100,30);
        loadButton.addActionListener(this);
        add(loadButton);

        // 확인 버튼
        mainButton = new JButton("돌아가기");
        mainButton.setBounds(320,240,100,30);
        mainButton.addActionListener(this);
        add(mainButton);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  불러오기 버튼 클릭 시 계좌 목록 불러오기
    //                돌아가기 버튼 클릭 시 메인 화면으로 전환
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadButton) {
            loadAccountList();
        }
        else if (e.getSource() == mainButton) {
            resetTable();
            this.setVisible(false);
            MainFrame.display("Main");
        }
    }

    //*******************************************************************
    // Name : loadAccountList()
    // Type : Method
    // Description :  사용자가 입력한 정보를 확인하고 계좌 목록 요청을 서버로 전송
    //                - CommandDTO 객체를 생성하고 서버로 요청 전송
    //                - 서버로 계좌 목록 요청을 비동기적(CompletionHandler)으로 전송
    //                - 서버의 응답이 성공적으로 처리되면 processServerResponse() 호출해 응답 처리
    //                - 서버 연결 실패시 사용자에게 실패 메시지 전송
    //*******************************************************************
    private void loadAccountList() {
        CommandDTO command = new CommandDTO();
        command.setRequestType(RequestType.VIEW_ACCOUNT_LIST);

        MainFrame.send(command, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer responseBuffer) {
                processServerResponse(responseBuffer);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                JOptionPane.showMessageDialog(MainFrame, "계좌 목록 불러오기 실패");
            }
        });
    }

    //*******************************************************************
    // Name : processServerResponse()
    // Type : Method
    // Description :  서버로부터 받은 응답 데이터를 처리하여 계좌 목록을 업데이트
    //                - 성공 시 계좌 정보를 JTable에 추가하고 실패 시 오류 메시지를 표시
    // Parameters - ByteBuffer : 서버에서 전달된 응답 데이터를 포함하는 버퍼
    //*******************************************************************
    private void processServerResponse(ByteBuffer responseBuffer) {
        try {
            if (responseBuffer.remaining() == 0) {
                System.out.println("[클라이언트] 서버 응답 없음");
                JOptionPane.showMessageDialog(MainFrame, "서버 응답 없음");
                return;
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(responseBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);

            CommandDTO response = (CommandDTO) ois.readObject();

            if (response.getResponseType() == ResponseType.SUCCESS) {
                List<AccountVO> accounts = response.getAccountlist();
                System.out.println("[클라이언트] 수신된 계좌 목록: " + accounts);
                updateTable(accounts);
            } else {
                JOptionPane.showMessageDialog(MainFrame, "계좌 목록을 불러오지 못했습니다.");
            }

        } catch (IOException | ClassNotFoundException e ) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame, "응답 처리 중 오류 발생");
        }
    }

    //*******************************************************************
    // Name : updateTable()
    // Type : Method
    // Description :  서버에서 받은 계좌 목록 데이터를 JTable에 업데이트
    //                - 보유 잔고를 내림차순으로 정렬하여 표시
    //                - 총 보유 잔고를 계산하고 화면에 표시
    // Parameters - List<AccountVO> accounts : 서버 응답에서 받은 계좌 목록 데이터
    //*******************************************************************
    private void updateTable(List<AccountVO> accounts) {
        String[] columns = {"계좌번호", "잔액 (원)", "고객명"};
        DefaultTableModel model = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        long totalBankBalance = 0;

        // 보유 잔고 내림차순 정렬
        accounts.sort(Comparator.comparing(AccountVO::getBalance).reversed());

        for (AccountVO account : accounts) {
            model.addRow(new Object[]{BankUtils.displayAccountNo(account.getAccountNo()),
                    BankUtils.displayBalance(account.getBalance())+" ", account.getOwner()});
            totalBankBalance += account.getBalance();
        }

        accountTable.setModel(model);
        totallabel.setText("총 보유 잔고: " + BankUtils.displayBalance(totalBankBalance) + "원");

        // 중앙 정렬 렌더러 설정
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // 우측 정렬 렌더러 설정
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        accountTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        accountTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        accountTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
    }

    //*******************************************************************
    // Name : resetTable()
    // Type : Method
    // Description : JTable을 초기 상태로 되돌리고, 총 보유 잔고를 초기화하는 메소드
    //*******************************************************************
    private void resetTable() {
        String[] columns = {"계좌번호", "잔액 (원)", "고객명"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        accountTable.setModel(model);
        totallabel.setText("총 보유 잔고:");
    }
}
