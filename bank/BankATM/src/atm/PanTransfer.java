package atm;

import bank.AccountVO;
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
// Name : PanTransfer
// Type : Class
// Description :  계좌 이체 패널을 구현한 Class 이다.
//*******************************************************************
public class PanTransfer extends JPanel implements ActionListener
{
    private JLabel titlelabel;

    private JLabel Label_RecvAccount;
    private  JTextField Text_RecvAccount;

    private JLabel Label_Amount;
    private JTextField Text_Amount;

    private JLabel Label_Password;
    private JPasswordField Text_Password;

    private JLabel Label_MyAccounts;
    private JComboBox Com_Account;

    private JButton Btn_Transfer;
    private JButton Btn_Close;

    ATMMain MainFrame;

    //*******************************************************************
    // Name : PanTransfer()
    // Type : 생성자
    // Description :  PanTransfer Class의 생성자 구현
    //*******************************************************************
    public PanTransfer(ATMMain parent)
    {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description :  계좌 이체 화면 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI()
    {
        setLayout(null);
        setBounds(0,0,480,320);

        // 제목
        titlelabel = new JLabel("계좌이체");
        titlelabel.setFont(new Font("Sanserif", Font.BOLD, 20));
        titlelabel.setSize(getWidth(), 60);
        titlelabel.setLocation(0,20);
        titlelabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titlelabel);

        Label_RecvAccount = new JLabel("받는 분 계좌번호");
        Label_RecvAccount.setBounds(50,80,100,20);
        Label_RecvAccount.setHorizontalAlignment(JLabel.CENTER);
        add(Label_RecvAccount);

        Text_RecvAccount = new JTextField();
        Text_RecvAccount.setBounds(150,80,200,20);
        Text_RecvAccount.setEditable(true);
        Text_RecvAccount.setToolTipText("숫자만 입력");
        add(Text_RecvAccount);

        Label_Amount = new JLabel("이체금액");
        Label_Amount.setBounds(50,110,100,20);
        Label_Amount.setHorizontalAlignment(JLabel.CENTER);
        add(Label_Amount);

        Text_Amount = new JTextField();
        Text_Amount.setBounds(150,110,200,20);
        Text_Amount.setEditable(true);
        Text_Amount.setToolTipText("숫자만 입력");
        add(Text_Amount);

        Label_Password = new JLabel("비밀번호");
        Label_Password.setBounds(50,140,100,20);
        Label_Password.setHorizontalAlignment(JLabel.CENTER);
        add(Label_Password);

        Text_Password = new JPasswordField();
        Text_Password.setBounds(150,140,200,20);
        Text_Password.setEditable(true);
        add(Text_Password);

        Label_MyAccounts=new JLabel("내 계좌");
        Label_MyAccounts.setBounds(50,170,100,20);
        Label_MyAccounts.setHorizontalAlignment(JLabel.CENTER);
        add(Label_MyAccounts);

        Com_Account=new JComboBox();
        Com_Account.setBounds(150,170,200,20);
        add(Com_Account);

        Btn_Transfer = new JButton("이체");
        Btn_Transfer.setBounds(150,220,70,30);
        Btn_Transfer.addActionListener(this);
        add(Btn_Transfer);

        Btn_Close = new JButton("닫기");
        Btn_Close.setBounds(230,220,70,30);
        Btn_Close.addActionListener(this);
        add(Btn_Close);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  이체 버튼, 취소 버튼의 동작을 구현
    //                이체, 취소 동작 후 메인 화면으로 변경되도록 구현
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == Btn_Transfer) {
            Transfer();
        }
        if (e.getSource() == Btn_Close) {
            Text_RecvAccount.setText("");
            Text_Amount.setText("");
            Text_Password.setText("");
            this.setVisible(false);
            MainFrame.display("Main");
        }
    }

    public void LoadAccount(){
        MainFrame.send(new CommandDTO(RequestType.LOAD_ACCOUNT),new CompletionHandler<Integer, ByteBuffer>(){
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result == -1) {
                    return;
                }
                attachment.flip();
                try{
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(attachment.array());
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    CommandDTO command = (CommandDTO) objectInputStream.readObject();
                    SwingUtilities.invokeLater(() -> {
                        // 기존 항목 제거

                        String contentText = null;
                        if (command.getResponseType() == ResponseType.SUCCESS) {
                            MainFrame.useraccont=true;
                            Com_Account.removeAllItems();

                            // 하이픈 형식으로 계좌번호를 추가
                            for (AccountVO account : command.getAccountlist()) {
                                String formattedAccountNo = BankUtils.displayAccountNo(account.getAccountNo());
                                Com_Account.addItem(formattedAccountNo);
                            }

                            Com_Account.revalidate(); // 추가된 부분
                            Com_Account.repaint();    // 추가된 부분
                        }
                        else {
                            MainFrame.useraccont=false;

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
            public void failed(Throwable exc, ByteBuffer attachment) {
            }
        });
    }

    //*******************************************************************
    // Name : Transfer()
    // Type : Method
    // Description :  계좌 이체 화면의 데이터를 가지고 있는 CommandDTO를 생성하고,
    //                ATMMain의 Send 기능을 호출하여 서버에 계좌이체 요청 메시지를 전달 하는 기능.
    //*******************************************************************
    public void Transfer() {
        try {
            if (Text_RecvAccount.getText().isEmpty()) {
                JOptionPane.showMessageDialog(MainFrame, "받는 분 계좌번호를 입력하세요.");
                this.setVisible(true);
                return;
            }
            if (Text_Amount.getText().isEmpty()) {
                JOptionPane.showMessageDialog(MainFrame, "이체금액을 입력하세요.");
                this.setVisible(true);
                return;
            }
            String pw = new String(Text_Password.getPassword());
            if (pw.isEmpty()) {
                JOptionPane.showMessageDialog(MainFrame, "비밀번호를 입력하세요.");
                this.setVisible(true);
                return;
            }

            String receiveAccountNo = Text_RecvAccount.getText();
            String useraccount = BankUtils.removeHyphens(Com_Account.getSelectedItem().toString());
            long amount = Long.parseLong(Text_Amount.getText());
            String password = new String(Text_Password.getPassword());

            if ( amount <= 0 ) {
                JOptionPane.showMessageDialog(MainFrame, "금액은 0보다 커야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                this.setVisible(true);
                return;
            }

            CommandDTO commandDTO = new CommandDTO(RequestType.TRANSFER, password, useraccount, receiveAccountNo, amount);
            MainFrame.send(commandDTO, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    processServerResponse(attachment);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                }
            });
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(MainFrame, "유효한 숫자를 입력하세요.");
            this.setVisible(true);
        }
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
                JOptionPane.showMessageDialog(MainFrame, "서버 응답 없음");
                return;
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(responseBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);

            CommandDTO response = (CommandDTO) ois.readObject();
            String contentText;

            if (response.getResponseType() == ResponseType.INSUFFICIENT) {
                contentText = "잔액이 부족합니다.";
                JOptionPane.showMessageDialog(MainFrame, contentText, "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                setVisible(true);
                return;
            }
            else if (response.getResponseType() == ResponseType.WRONG_ACCOUNT_NO) {
                contentText = "계좌번호가 존재하지 않습니다.";
                JOptionPane.showMessageDialog(MainFrame, contentText, "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                setVisible(true);
                return;
            }
            else if (response.getResponseType() == ResponseType.WRONG_PASSWORD) {
                contentText = "비밀번호가 일치하지 않습니다.";
                JOptionPane.showMessageDialog(MainFrame, contentText, "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                setVisible(true);
                return;
            }
            else {
                contentText = "이체 되었습니다.";
                JOptionPane.showMessageDialog(MainFrame, contentText, "SUCCESS_MESSAGE", JOptionPane.PLAIN_MESSAGE);
            }

            Text_RecvAccount.setText("");
            Text_Amount.setText("");
            Text_Password.setText("");
            this.setVisible(false);
            MainFrame.display("Main");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame, "응답 처리 중 오류 발생.");
        }
    }
}
