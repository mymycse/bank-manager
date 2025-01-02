package manager;

import common.AccountType;
import common.CommandDTO;
import common.RequestType;
import common.ResponseType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

//*******************************************************************
// Name : PanaddAccount
// Type : Class
// Description :  신규 계좌 추가 화면 패널을 구현한 Class
//                - 사용자로부터 ID와 비밀번호를 입력받아 서버에 계좌 생성 요청을 전송
//                - 계좌 유형을 선택할 수 있는 옵션 제공
//                - 서버의 응답에 따라 계좌 생성 성공 여부를 사용자에게 알리고 결과 처리
//                - 메인화면으로 돌아가는 기능 제공
//*******************************************************************
public class PanaddAccount extends JPanel implements ActionListener {
    private JButton addButton, cancelButton;
    private JLabel titlelabel, idlabel, pwlabel;
    private  JTextField Text_id;
    private  JPasswordField Text_pw;
    private JRadioButton[] setAccountType;
    private ButtonGroup accountTypeGroup;
    ManagerMain MainFrame;

    //*******************************************************************
    // Name : PanaddAccount()
    // Type : 생성자
    // Description :  PanaddAccount Class의 생성자 구현
    //*******************************************************************
    public PanaddAccount(ManagerMain parent) {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description : 계좌 추가 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI() {
        setLayout(null);
        setBounds(0,0,480,320);

        //제목
        titlelabel = new JLabel("계좌 추가");
        titlelabel.setFont(new Font("Sanserif", Font.BOLD, 20));
        titlelabel.setSize(getWidth(), 60);
        titlelabel.setLocation(0,0);
        titlelabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titlelabel);

        //아이디
        idlabel = new JLabel("id: ");
        idlabel.setBounds(70,80,100,20);
        idlabel.setHorizontalAlignment(JLabel.CENTER);
        add(idlabel);

        Text_id = new JTextField();
        Text_id.setBounds(140,80,200,20);
        Text_id.setEditable(true);
        add(Text_id);

        //패스워드
        pwlabel = new JLabel("pw: ");
        pwlabel.setBounds(70,120,100,20);
        pwlabel.setHorizontalAlignment(JLabel.CENTER);
        add(pwlabel);

        Text_pw = new JPasswordField();
        Text_pw.setBounds(140,120,200,20);
        Text_pw.setEditable(true);
        add(Text_pw);

        // 계좌 종류 설정
        setAccountType = new JRadioButton[2];
        setAccountType[0] = new JRadioButton("당좌예금");
        setAccountType[1] = new JRadioButton("저축예금");
        setAccountType[0].addActionListener(this);
        setAccountType[1].addActionListener(this);
        setAccountType[0].setBounds(150,160,80,20);
        setAccountType[1].setBounds(230,160,80,20);

        // 그룹화
        accountTypeGroup = new ButtonGroup();
        accountTypeGroup.add(setAccountType[0]);
        accountTypeGroup.add(setAccountType[1]);
        add(setAccountType[0]);
        add(setAccountType[1]);

        //생성버튼
        addButton = new JButton("생성");
        addButton.setBounds(240,220,100,50);
        addButton.addActionListener(this);
        add(addButton);

        //취소버튼
        cancelButton = new JButton("취소");
        cancelButton.setBounds(350,220,100,50);
        cancelButton.addActionListener(this);
        add(cancelButton);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  확인 버튼 구현
    //                계좌 정보 열람을 마치고 메인 화면으로 돌아가도록 구현
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addAccount();
        } else if (e.getSource() == cancelButton) {
            Text_id.setText("");
            Text_pw.setText("");
            accountTypeGroup.clearSelection();
            this.setVisible(false);
            MainFrame.display("Main");
        }
    }

    //*******************************************************************
    // Name : addAccount()
    // Type : Method
    // Description :  사용자가 입력한 정보를 확인하고 계좌 생성 요청을 서버로 전송
    //                - 입력값 검증 후 CommandDTO 객체를 생성하고 서버로 요청 전송
    //                - 입력값 없을 경우 사용자에게 알림메시지 전송
    //*******************************************************************
    public void addAccount() {
        String id=Text_id.getText();
        String pw=new String(Text_pw.getPassword());

        if ( id.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame,"ID를 입력하세요.");
            this.setVisible(true);
            return;
        }

        if ( pw.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "비밀번호를 입력하세요.");
            this.setVisible(true);
            return;
        }

        if ( !setAccountType[0].isSelected() && !setAccountType[1].isSelected() ) {
            JOptionPane.showMessageDialog(MainFrame, "계좌 종류를 선택하세요.");
            this.setVisible(true);
            return;
        }

        CommandDTO command = createCommand();
        sendRequestToServer(command);
    }

    //*******************************************************************
    // Name : createCommand()
    // Type : Method
    // Description :  계좌 생성 요청에 필요한 데이터를 CommandDTO 객체에 설정하고 반환
    //*******************************************************************
    private CommandDTO createCommand() {
        CommandDTO command = new CommandDTO();
        command.setRequestType(RequestType.ADD_ACCOUNT);
        command.setId(Text_id.getText());
        command.setPassword(new String(Text_pw.getPassword()));

        if (setAccountType[0].isSelected()) {
            command.setAccountType(AccountType.CHECKING);
        } else if (setAccountType[1].isSelected()) {
            command.setAccountType(AccountType.SAVINGS);
        }
        return command;
    }

    //*******************************************************************
    // Name : sendRequestToServer()
    // Type : Method
    // Description :  서버로 계좌 생성 요청을 비동기적(CompletionHandler)으로 전송
    //                - 서버의 응답이 성공적으로 처리되면 processServerResponse() 호출해 응답 처리
    //                - 서버 연결 실패시 사용자에게 실패 메시지 전송
    //*******************************************************************
    private void sendRequestToServer(CommandDTO command) {
        MainFrame.send(command, new CompletionHandler<>(){
            @Override
            public void completed(Integer result, ByteBuffer responseBuffer) {
                processServerResponse(responseBuffer);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                JOptionPane.showMessageDialog(MainFrame, "서버 연결에 실패했습니다.");
            }
        });
    }

    //*******************************************************************
    // Name : processServerResponse()
    // Type : Method
    // Description :  서버로부터 받은 응답을 처리하여 사용자에게 결과 표시
    //                - 응답 데이터를 역직렬화하여 CommandDTO 객체로 반환
    //                - 서버의 응답 상태(ResponseType)에 따라 성공, 실패 메시지 표시
    //                - 작업 완료 후 입력 필드를 초기화하고 화면 전환
    // Parameters - ByteBuffer : 서버에서 전달된 응답 데이터를 포함하는 버퍼
    //*******************************************************************
    private void processServerResponse(ByteBuffer responseBuffer) {
        try {
            if (responseBuffer.remaining() == 0) {
                System.out.println("[클라이언트] 서버 응답 없음");
                JOptionPane.showMessageDialog(MainFrame, "서버 응답 없음");
                Text_id.setText("");
                Text_pw.setText("");
                accountTypeGroup.clearSelection();
                this.setVisible(false);
                MainFrame.display("Main");
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(responseBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);

            CommandDTO response = (CommandDTO) ois.readObject();
            System.out.println("[클라이언트] 응답 데이터: " + response);

            if (response.getResponseType() == ResponseType.SUCCESS) {
                System.out.println("[클라이언트] " + response.getName()+ " 님의 계좌가 추가되었습니다. 계좌번호: " + response.getUserAccountNo());
                JOptionPane.showMessageDialog(MainFrame, "신규 계좌: " + BankUtils.displayAccountNo(response.getUserAccountNo()));
            }
            else if (response.getResponseType() == ResponseType.WRONG_PASSWORD) {
                System.out.println("[클라이언트] 비밀번호가 일치하지 않습니다.");
                JOptionPane.showMessageDialog(MainFrame, "비밀번호 불일치");
                Text_pw.setText("");
                this.setVisible(true);
                MainFrame.display("addAccount");
                return;
            }
            else if (response.getResponseType() == ResponseType.WRONG_ID){
                JOptionPane.showMessageDialog(MainFrame, "고객 정보를 찾을 수 없습니다.");
                this.setVisible(true);
                MainFrame.display("addAccount");
                return;
            }
            else {
                JOptionPane.showMessageDialog(MainFrame,"계좌 추가에 실패했습니다.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame, "응답 처리 중 오류 발생.");
        }

        Text_id.setText("");
        Text_pw.setText("");
        accountTypeGroup.clearSelection();
        this.setVisible(false);
        MainFrame.display("Main");
    }
}