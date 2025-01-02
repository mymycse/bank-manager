package manager;

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
// Name : PandeleteAccount
// Type : Class
// Description :  계좌를 삭제하는 기능을 제공하는 패널을 구현한 클래스
//                - 사용자로부터 ID와 계좌번호를 입력받아 서버에 삭제 요청을 전송
//                - 서버 응답 상태에 따라 성공 또는 실패 메시지를 사용자에게 표시
//                - 삭제 완료 후 입력 필드를 초기화하고 메인 화면으로 전환
//*******************************************************************
public class PandeleteAccount extends JPanel implements ActionListener {
    private JButton deleteButton, cancelButton;
    private JLabel titlelabel, accountlabel, idlabel;
    private  JTextField Text_account, Text_id;
    ManagerMain MainFrame;

    //*******************************************************************
    // Name : PandeleteAccount()
    // Type : 생성자
    // Description :  PandeleteAccount Class의 생성자 구현
    //*******************************************************************
    public PandeleteAccount(ManagerMain parent) {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description :  계좌 삭제 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI() {
        setLayout(null);
        setBounds(0,0,480,320);

        // 제목
        titlelabel = new JLabel("계좌 삭제");
        titlelabel.setFont(new Font("Sanserif", Font.BOLD, 20));
        titlelabel.setSize(getWidth(), 60);
        titlelabel.setLocation(0,0);
        titlelabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titlelabel);

        // 아이디
        idlabel = new JLabel("id:  ");
        idlabel.setBounds(60,80,80,20);
        idlabel.setHorizontalAlignment(JLabel.RIGHT);
        add(idlabel);

        Text_id = new JTextField();
        Text_id.setBounds(140,80,200,20);
        Text_id.setEditable(true);
        add(Text_id);

        // 계좌번호
        accountlabel = new JLabel("계좌번호:  ");
        accountlabel.setBounds(60,120,80,20);
        accountlabel.setHorizontalAlignment(JLabel.RIGHT);
        accountlabel.setToolTipText("숫자만 입력 가능");
        add(accountlabel);

        Text_account = new JTextField();
        Text_account.setBounds(140,120,200,20);
        Text_account.setEditable(true);
        add(Text_account);

        // 삭제 버튼
        deleteButton = new JButton("삭제");
        deleteButton.setBounds(240,220,100,50);
        deleteButton.addActionListener(this);
        add(deleteButton);

        // 취소 버튼
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
        if (e.getSource() == deleteButton) {
            deleteAccount();
        } else if (e.getSource() == cancelButton) {
            Text_id.setText("");
            Text_account.setText("");
            this.setVisible(false);
            MainFrame.display("Main");
        }
    }

    //*******************************************************************
    // Name : deleteAccount()
    // Type : Method
    // Description : 사용자로부터 입력받은 ID와 계좌번호를 검증하고, 계좌 삭제 요청을 서버에 전송
    //               - 필수 입력값을 확인하고, 누락된 경우 사용자에게 경고 메시지 표시
    //               - 입력값이 유효한 경우 CommandDTO 객체를 생성하여 서버로 요청 전송
    //*******************************************************************
    public void deleteAccount() {
        String id=Text_id.getText();
        String account=Text_account.getText();

        if ( id.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "ID를 입력하세요.");
            this.setVisible(true);
            return;
        }

        if ( account.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "계좌번호를 입력하세요.");
            this.setVisible(true);
            return;
        }

        CommandDTO command = createCommand();
        sendRequestToServer(command);
    }

    //*******************************************************************
    // Name : createCommand()
    // Type : Method
    // Description :  계좌 삭제 요청에 필요한 데이터를 CommandDTO 객체에 설정하고 반환
    //*******************************************************************
    private CommandDTO createCommand() {
        CommandDTO command = new CommandDTO();
        command.setRequestType(RequestType.DELETE_ACCOUNT);
        command.setId(Text_id.getText());
        command.setUserAccountNo(Text_account.getText());
        return command;
    }

    //*******************************************************************
    // Name : sendRequestToServer()
    // Type : Method
    // Description :  서버로 계좌 삭제 요청을 비동기적(CompletionHandler)으로 전송
    //                - 서버의 응답이 성공적으로 처리되면 processServerResponse() 호출하여 응답 처리
    //                - 서버 연결 실패시 사용자에게 실패 메시지 전송
    // Parameters : CommandDTO - 서버로 전송할 계좌 삭제 요청 데이터 객체
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
                return;
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(responseBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);

            CommandDTO response = (CommandDTO) ois.readObject();
            System.out.println("[클라이언트] 응답 데이터: " + response);

            if (response.getResponseType() == ResponseType.SUCCESS) {
                System.out.println("[클라이언트] " + response.getName()+ " 님의 계좌가 삭제되었습니다. 계좌번호: " + response.getUserAccountNo());
                JOptionPane.showMessageDialog(MainFrame, "계좌가 삭제되었습니다.");
            }
            else if (response.getResponseType() == ResponseType.WRONG_ID) {
                System.out.println("[클라이언트] 존재하지 않는 고객입니다. id: " + response.getId());
                JOptionPane.showMessageDialog(MainFrame, "고객 정보를 찾을 수 없습니다.");
                this.setVisible(true);
                MainFrame.display("deleteAccount");
                return;
            }
            else if (response.getResponseType() == ResponseType.WRONG_ACCOUNT_NO) {
                System.out.println("[클라이언트] 존재하지 않는 계좌입니다.");
                JOptionPane.showMessageDialog(MainFrame, "존재하지 않는 계좌입니다.");
                this.setVisible(true);
                MainFrame.display("deleteAccount");
                return;
            }
            else {
                JOptionPane.showMessageDialog(MainFrame, "고객 정보를 찾을 수 없습니다.");
            }

            Text_id.setText("");
            Text_account.setText("");
            this.setVisible(false);
            MainFrame.display("Main");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame, "응답 처리 중 오류 발생.");
        }
    }
}
