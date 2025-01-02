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
// Name : PandeleteCustomer
// Type : Class
// Description :  고객 삭제 화면 패널을 구현한 Class
//                - 사용자로부터 이름, ID, 비밀번호를 입력받아 서버로 고객 삭제 요청 전송
//                - 서버의 응답에 따라 고객 삭제 성공 여부를 사용자에게 알리고 결과 처리
//*******************************************************************
public class PandeleteCustomer extends JPanel implements ActionListener {
    private JButton deleteButton, cancelButton;
    private JLabel titlelabel, namelabel, idlabel, pwlabel;
    private  JTextField Text_name, Text_id;
    private JPasswordField Text_pw;
    ManagerMain MainFrame;

    //*******************************************************************
    // Name : PandeleteCustomer()
    // Type : 생성자
    // Description :  PandeleteCustomer Class의 생성자 구현
    //*******************************************************************
    public PandeleteCustomer(ManagerMain parent) {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description : 고객 삭제 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI() {
        setLayout(null);
        setBounds(0,0,480,320);

        //제목
        titlelabel = new JLabel("고객 삭제");
        titlelabel.setFont(new Font("Sanserif", Font.BOLD, 20));
        titlelabel.setSize(getWidth(), 60);
        titlelabel.setLocation(0,0);
        titlelabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titlelabel);

        //이름
        namelabel = new JLabel("이름: ");
        namelabel.setBounds(70,80,70,20);
        namelabel.setHorizontalAlignment(JLabel.RIGHT);
        add(namelabel);

        Text_name = new JTextField();
        Text_name.setBounds(140,80,200,20);
        Text_name.setEditable(true);
        add(Text_name);

        //아이디부분
        idlabel = new JLabel("id: ");
        idlabel.setBounds(70,120,70,20);
        idlabel.setHorizontalAlignment(JLabel.RIGHT);
        add(idlabel);

        Text_id = new JTextField();
        Text_id.setBounds(140,120,200,20);
        Text_id.setEditable(true);
        add(Text_id);

        //패스워드
        pwlabel = new JLabel("pw: ");
        pwlabel.setBounds(70,160,70,20);
        pwlabel.setHorizontalAlignment(JLabel.RIGHT);
        add(pwlabel);

        Text_pw = new JPasswordField();
        Text_pw.setBounds(140,160,200,20);
        Text_pw.setEditable(true);
        add(Text_pw);

        //삭제버튼
        deleteButton = new JButton("삭제");
        deleteButton.setBounds(240,220,100,50);
        deleteButton.addActionListener(this);
        add(deleteButton);

        //취소버튼
        cancelButton = new JButton("취소");
        cancelButton.setBounds(350,220,100,50);
        cancelButton.addActionListener(this);
        add(cancelButton);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  삭제 버튼과 취소 버튼의 동작 구현
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == deleteButton) {
            deleteCustomer();
        } else if ( e.getSource() == cancelButton ) {
            Text_name.setText("");
            Text_id.setText("");
            Text_pw.setText("");
            this.setVisible(false);
            MainFrame.display("Main");
        }
    }

    //*******************************************************************
    // Name : deleteCustomer()
    // Type : Method
    // Description :  사용자가 입력한 정보를 확인하고 고객 삭제 요청을 서버로 전송
    //                - 입력값 검증 후 CommandDTO 객체를 생성하고 서버로 요청 전송
    //                - 입력값 없을 경우 사용자에게 알림메시지 전송
    //*******************************************************************
    public void deleteCustomer(){
        String name = Text_name.getText();
        String id = Text_id.getText();
        String pw = new String(Text_pw.getPassword());

        if ( name.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "이름을 입력하세요.");
            return;
        }

        if ( id.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "ID를 입력하세요.");
            return;
        }

        if ( pw.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "비밀번호를 입력하세요.");
            return;
        }

        CommandDTO command = createCommand();
        sendRequestToServer(command);
    }

    //*******************************************************************
    // Name : createCommand()
    // Type : Method
    // Description :  고객 삭제 요청에 필요한 데이터를 CommandDTO 객체에 설정하고 반환
    //*******************************************************************
    private CommandDTO createCommand() {
        CommandDTO command = new CommandDTO();
        command.setRequestType(RequestType.DELETE_CUSTOMER);
        command.setName(Text_name.getText());
        command.setId(Text_id.getText());
        command.setPassword(new String(Text_pw.getPassword()));
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
                return;
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(responseBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);

            CommandDTO response = (CommandDTO) ois.readObject();

            if (response.getResponseType() == ResponseType.SUCCESS) {
                System.out.println("[클라이언트] " + response.getName()+ " 님의 계정 삭제 롼료: id=" + response.getId());
                JOptionPane.showMessageDialog(MainFrame, response.getName()+ " 님의 계정이 삭제되었습니다.");
            } else if (response.getResponseType() == ResponseType.WRONG_ID) {
                System.out.println("[클라이언트] 존재하지 않는 id="+response.getId());
                JOptionPane.showMessageDialog(MainFrame, "존재하지 않는 id입니다.");
                MainFrame.display("deleteCustomer");
                this.setVisible(true);
                return;
            } else if (response.getResponseType() == ResponseType.WRONG_PASSWORD) {
                System.out.println("[클라이언트] 비밀번호 불일치: id=" + response.getId());
                JOptionPane.showMessageDialog(MainFrame, "비밀번호 불일치");
                MainFrame.display("deleteCustomer");
                this.setVisible(true);
                return;
            } else if (response.getResponseType() == ResponseType.WRONG_NAME) {
                System.out.println("[클라이언트] 고객명 불일치: id="+response.getId()+", 이름="+response.getName());
                JOptionPane.showMessageDialog(MainFrame, "고객명이 id 정보와 일치하지 않습니다.");
                MainFrame.display("deleteCustomer");
                this.setVisible(true);
                return;
            } else {
                JOptionPane.showMessageDialog(MainFrame, "고객 삭제에 실패했습니다.");
            }
            Text_name.setText("");
            Text_id.setText("");
            Text_pw.setText("");
            this.setVisible(false);
            MainFrame.display("Main");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame, "응답 처리 중 오류 발생.");
        }
    }
}
