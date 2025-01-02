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
// Name : PanDeposite
// Type : Class
// Description :  입금 화면 패널을 구현한 Class 이다.
//*******************************************************************
public class PanDeposite extends JPanel implements ActionListener
{
    private JLabel Label_Title;

    private JLabel Label_Amount;
    private JTextField Text_Amount;

    private JButton Btn_Deposite;
    private JButton Btn_Close;

    private JLabel Label_Select;
    private JComboBox Com_Account;

    ATMMain MainFrame;


    //*******************************************************************
    // Name : PanDeposite()
    // Type : 생성자
    // Description :  PanDeposite Class의 생성자 구현
    //*******************************************************************
    public PanDeposite(ATMMain parent)
    {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description :  입금 화면 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI()
    {
        setLayout(null);
        setBounds(0,0,480,320);


        Label_Title = new JLabel("입금");
        Label_Title.setFont(new Font("Sanserif", Font.BOLD, 20));
        Label_Title.setSize(getWidth(), 60);
        Label_Title.setLocation(0,20);
        Label_Title.setHorizontalAlignment(JLabel.CENTER);
        add(Label_Title);

        Label_Select=new JLabel("계좌 선택");
        Label_Select.setBounds(50,100,100,20);
        Label_Select.setHorizontalAlignment(JLabel.CENTER);
        add(Label_Select);

        Com_Account=new JComboBox();
        Com_Account.setBounds(140,100,200,20);
        add(Com_Account);

        Label_Amount = new JLabel("금액");
        Label_Amount.setBounds(50,140,100,20);
        Label_Amount.setHorizontalAlignment(JLabel.CENTER);
        add(Label_Amount);

        Text_Amount = new JTextField();
        Text_Amount.setBounds(140,140,200,20);
        Text_Amount.setEditable(true);
        add(Text_Amount);

        Btn_Deposite = new JButton("입금");
        Btn_Deposite.setBounds(150,200,70,30);
        Btn_Deposite.addActionListener(this);
        add(Btn_Deposite);

        Btn_Close = new JButton("취소");
        Btn_Close.setBounds(230,200,70,30);
        Btn_Close.addActionListener(this);
        add(Btn_Close);
    }


    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  입금 버튼, 취소 버튼의 동작을 구현
    //                입금, 취소 동작 후 메인 화면으로 변경되도록 구현
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == Btn_Deposite) {
            deposit();
            Text_Amount.setText("");
        }
        else if (e.getSource() == Btn_Close) {
            Text_Amount.setText("");
            this.setVisible(false);
            MainFrame.display("Main");
        }

    }

    public void LoadAccount() {
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
                        if (command.getResponseType() == ResponseType.SUCCESS)
                        {
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
                        else
                        {
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
    // Name : deposit()
    // Type : Method
    // Description :  입금 화면의 데이터를 가지고 있는 CommandDTO를 생성하고,
    //                ATMMain의 Send 기능을 호출하여 서버에 입금 요청 메시지를 전달 하는 기능.
    //*******************************************************************
    public void deposit() {
        try {
            if (Text_Amount.getText().isEmpty()) {
                JOptionPane.showMessageDialog(MainFrame, "금액을 입력하세요.");
                this.setVisible(true);
                return;
            }

            String account = BankUtils.removeHyphens(Com_Account.getSelectedItem().toString());
            long amount = Long.parseLong(Text_Amount.getText());

            if ( amount <= 0 ) {
                JOptionPane.showMessageDialog(MainFrame, "금액은 0보다 커야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                this.setVisible(true);
                return;
            }
            CommandDTO commandDTO = new CommandDTO(RequestType.DEPOSIT, account, amount);
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

            if (response.getResponseType() == ResponseType.SUCCESS) {
                contentText = "입금 되었습니다.";
                JOptionPane.showMessageDialog(MainFrame, contentText, "SUCCESS_MESSAGE", JOptionPane.PLAIN_MESSAGE);
            }
            else {
                contentText = "입금 오류! 관리자에게 문의하세요.";
                JOptionPane.showMessageDialog(MainFrame, contentText, "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            }

            this.setVisible(false);
            MainFrame.display("Main");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame, "응답 처리 중 오류 발생.");
        }
    }
}
