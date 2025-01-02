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
// Name : PanLogin
// Type : Class
// Description :  은행 관리자 시스템의 로그인 화면을 구현한 Class
//                - 사용자가 ID와 비밀번호를 입력하여 로그인을 수행할 수 있도록 제공
//                - 서버와의 통신을 통해 로그인 성공/실패 여부를 확인
//                - 성공 시 메인 화면으로 이동하며, 실패 시 에러 메시지를 표시
//                - 프로그램 종료 기능을 포함
//*******************************************************************
public class PanLogin extends JPanel implements ActionListener {
    private JLabel titlelabel, idlabel, pwlabel;
    private JTextField Text_id;
    private JPasswordField Text_pw;
    private JButton loginButton, exitButton;
    ManagerMain MainFrame;
    private BankServiceHandler handler;

    //*******************************************************************
    // Name : PanLogin()
    // Type : 생성자
    // Description :  PanLogin Class의 생성자 구현
    //*******************************************************************
    public PanLogin(ManagerMain parent) {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description : 매니저 로그인 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI() {
        setLayout(null);
        setBounds(0, 0, 480, 320);

        // 제목
        titlelabel = new JLabel("CNU BANK MANAGER");
        titlelabel.setFont(new Font("Sanserif", Font.BOLD, 30));
        titlelabel.setSize(getWidth(), 60);
        titlelabel.setLocation(0,20);
        titlelabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titlelabel);

        // 아이디
        idlabel = new JLabel("id: ");
        idlabel.setBounds(70,100,100,20);
        idlabel.setHorizontalAlignment(JLabel.CENTER);
        add(idlabel);

        Text_id = new JTextField();
        Text_id.setBounds(140,100,200,20);
        Text_id.setEditable(true);
        add(Text_id);

        // 패스워드
        pwlabel = new JLabel("pw: ");
        pwlabel.setBounds(70,140,100,20);
        pwlabel.setHorizontalAlignment(JLabel.CENTER);
        add(pwlabel);

        Text_pw = new JPasswordField();
        Text_pw.setBounds(140,140,200,20);
        Text_pw.setEditable(true);
        add(Text_pw);

        // 로그인 버튼
        loginButton=new JButton("로그인");
        loginButton.setBounds(150,200,70,30);
        loginButton.addActionListener(this);
        add(loginButton);

        // 종료 버튼
        exitButton = new JButton("종료");
        exitButton.setBounds(230,200,70,30);
        exitButton.setBackground(new Color(255,173,169));
        exitButton.addActionListener(this);
        add(exitButton);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  로그인 버튼 클릭 시 Login() 메서드 호출
    //                종료 버튼 클릭 시 프로그램 종료 기능 수행
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            Login();
        }
        if (e.getSource() == exitButton) {
            MainFrame.stopClient();
            System.out.println("프로그램 종료");
            MainFrame.dispose();
        }
    }

    //*******************************************************************
    // Name : Login()
    // Type : Method
    // Description :  사용자가 입력한 정보를 확인하고 CommandDTO 객체 생성
    //                - 입력값 검증 후 CommandDTO 객체를 생성하고 서버로 요청 전송
    //                - 입력값 없을 경우 사용자에게 알림메시지 전송
    //*******************************************************************
    public void Login() {
        String id = Text_id.getText();
        String password = new String(Text_pw.getPassword());
        MainFrame.ManagerId = id;

        if ( id.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "ID를 입력하세요.");
            this.setVisible(true);
            return;
        }

        if ( password.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "비밀번호를 입력하세요.");
            this.setVisible(true);
            return;
        }

        CommandDTO command = createCommand();
        sendRequestToServer(command);
    }

    //*******************************************************************
    // Name : createCommand()
    // Type : Method
    // Description :  매니저 로그인 요청에 필요한 데이터를 CommandDTO 객체에 설정하고 반환
    //*******************************************************************
    private CommandDTO createCommand() {
        CommandDTO command = new CommandDTO();
        command.setRequestType(RequestType.MANAGER_LOGIN);
        command.setId(Text_id.getText());
        command.setPassword(Text_pw.getText());
        return command;
    }

    //*******************************************************************
    // Name : sendRequestToServer()
    // Type : Method
    // Description :  서버로 매니저 로그인 요청을 비동기적(CompletionHandler)으로 전송
    //                - 서버의 응답이 성공적으로 처리되면 processServerResponse() 호출해 응답 처리
    //                - 서버 연결 실패시 사용자에게 실패 메시지 전송
    //*******************************************************************
    private void sendRequestToServer(CommandDTO command) {
        MainFrame.send(command, new CompletionHandler<>() {
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
                MainFrame.ManagerId = response.getId();
                JOptionPane.showMessageDialog(MainFrame, response.getId()+" 님 환영합니다.");
                Text_id.setText("");
                Text_pw.setText("");
                setVisible(false);
                MainFrame.display("Main");
            } else if (response.getResponseType() == ResponseType.FAILURE) {
                JOptionPane.showMessageDialog(MainFrame, "아이디 또는 비밀번호가 일치하지 않습니다.");
            } else {
                JOptionPane.showMessageDialog(MainFrame, "ERROR");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame, "응답 처리 중 오류 발생.");
        }
    }
}
