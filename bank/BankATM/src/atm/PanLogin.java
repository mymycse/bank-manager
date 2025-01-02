package atm;

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
// Description :  로그인 화면 패널을 구현한 Class 이다.
//*******************************************************************
public class PanLogin extends JPanel implements ActionListener {
    private JLabel Label_Title;

    private JLabel Label_ID;
    private JTextField Text_ID;

    private JLabel Label_Password;
    private JPasswordField Text_Password;

    private JButton Btn_Login;
    private JButton Btn_Close;

    ATMMain MainFrame;
    private BankServiceHandler handler;

    //*******************************************************************
    // Name : PanLogin()
    // Type : 생성자
    // Description :  PanLogin Class의 생성자 구현
    //*******************************************************************
    public PanLogin(ATMMain parent) {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description :  로그인 화면 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI() {
        setLayout(null);
        setBounds(0, 0, 480, 320);

        Label_Title = new JLabel("로그인");
        Label_Title.setFont(new Font("Sanserif", Font.BOLD, 30));
        Label_Title.setSize(getWidth(), 60);
        Label_Title.setLocation(0,20);
        Label_Title.setHorizontalAlignment(SwingConstants.CENTER);
        add(Label_Title);

        Label_ID = new JLabel("id: ");
        Label_ID.setBounds(70,100,100,20);
        Label_ID.setHorizontalAlignment(JLabel.CENTER);
        add(Label_ID);

        Text_ID = new JTextField();
        Text_ID.setBounds(140,100,200,20);
        Text_ID.setEditable(true);
        add(Text_ID);

        Label_Password = new JLabel("pw: ");
        Label_Password.setBounds(70,140,100,20);
        Label_Password.setHorizontalAlignment(JLabel.CENTER);
        add(Label_Password);

        Text_Password = new JPasswordField();
        Text_Password.setBounds(140,140,200,20);
        Text_Password.setEditable(true);
        add(Text_Password);

        Btn_Login=new JButton("로그인");
        Btn_Login.setBounds(150,200,70,30);
        Btn_Login.addActionListener(this);
        add(Btn_Login);

        Btn_Close = new JButton("취소");
        Btn_Close.setBounds(230,200,70,30);
        Btn_Close.addActionListener(this);
        add(Btn_Close);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  로그인 버튼, 취소 버튼의 동작을 구현
    //                로그인, 취소 동작 후 메인 화면으로 변경되도록 구현
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == Btn_Login) {
            Login();
        }
        if (e.getSource() == Btn_Close) {
            this.setVisible(false);
            MainFrame.display("Main");
        }
    }

    //*******************************************************************
    // Name : Login()
    // Type : Method
    // Description :  로그인 화면의 데이터를 가지고 있는 CommandDTO를 생성하고,
    //                ATMMain의 Send 기능을 호출하여 서버에 로그인 요청 메시지를 전달 하는 기능.
    //*******************************************************************
    public void Login() {
        String id = Text_ID.getText();
        String password = new String(Text_Password.getPassword());

        if ( id.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "id를 입력하세요.");
            this.setVisible(true);
            return;
        }

        if ( password.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "비밀번호를 입력하세요.");
            this.setVisible(true);
            return;
        }

        MainFrame.userId = id;

        CommandDTO loginCommand = new CommandDTO(RequestType.LOGIN, id, password);
        MainFrame.send(loginCommand, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result == -1) {
                    return;
                }

                // Flip the buffer for reading mode
//                attachment.flip();
                try {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(attachment.array(), 0, attachment.limit());
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    CommandDTO command = (CommandDTO) objectInputStream.readObject();

                    SwingUtilities.invokeLater(() -> {
                        String contentText = null;
                        if (command.getResponseType() == ResponseType.SUCCESS) {
                            MainFrame.userId = id;
                            contentText = "로그인되었습니다.";
                            Text_ID.setText("");
                            Text_Password.setText("");
                            JOptionPane.showMessageDialog(MainFrame, contentText, "SUCCESS_MESSAGE", JOptionPane.PLAIN_MESSAGE);
                            setVisible(false);
                            MainFrame.display("Main");
                        } else if (command.getResponseType() == ResponseType.FAILURE) {
                            contentText = "아이디 또는 비밀번호가 일치하지 않습니다.";
                            JOptionPane.showMessageDialog(MainFrame, contentText, "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                            MainFrame.userId = null;
                        } else {
                            contentText = "ERROR.";
                            JOptionPane.showMessageDialog(MainFrame, contentText, "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    // Clear buffer for next write operation
                    attachment.clear();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                JOptionPane.showMessageDialog(null, "서버 통신 실패: " + exc.getMessage(), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
