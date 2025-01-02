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
import java.util.List;
import java.util.Vector;

//*******************************************************************
// Name : PanViewAccount
// Type : Class
// Description :  계좌조회 화면 패널을 구현한 Class 이다.
//*******************************************************************
public class PanViewAccount extends JPanel implements ActionListener
{
    private JLabel titlelabel;

    private JLabel Label_Account;
    private  JTextArea Text_Account;
    private JLabel Label_balance;
    private  JTextArea Text_balance;

    private  JLabel Label_Select;
    private JComboBox Com_Account;

    private JButton Btn_Close;
    private JButton Btn_Load;


    ATMMain MainFrame;

    //*******************************************************************
    // Name : PanViewAccount()
    // Type : 생성자
    // Description :  PanViewAccount Class의 생성자 구현
    //*******************************************************************
    public PanViewAccount(ATMMain parent)
    {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description :  계좌조회 화면 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI()
    {
        setLayout(null);
        setBounds(0,0,480,320);

        // 제목
        titlelabel = new JLabel("계좌 조회");
        titlelabel.setFont(new Font("Sanserif", Font.BOLD, 20));
        titlelabel.setSize(getWidth(), 60);
        titlelabel.setLocation(0,20);
        titlelabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titlelabel);

        Label_Select=new JLabel("계좌 선택");
        Label_Select.setBounds(30,80,100,20);
        Label_Select.setHorizontalAlignment(JLabel.CENTER);
        add(Label_Select);

        Label_Account = new JLabel("계좌 번호");
        Label_Account.setBounds(30,120,100,20);
        Label_Account.setHorizontalAlignment(JLabel.CENTER);
        add(Label_Account);

        Label_balance = new JLabel("잔액");
        Label_balance.setBounds(30,160,100,20);
        Label_balance.setHorizontalAlignment(JLabel.CENTER);
        add(Label_balance);

        Com_Account=new JComboBox();
        Com_Account.setBounds(150,80,200,20);
        add(Com_Account);

        Text_Account = new JTextArea();
        Text_Account.setBounds(150,120,200,20);
        Text_Account.setEditable(false);
        add(Text_Account);

        Text_balance = new JTextArea();
        Text_balance.setBounds(150,160,200,20);
        Text_balance.setEditable(false);
        add(Text_balance);

        Btn_Load=new JButton("조회");
        Btn_Load.setBounds(150,210,70,30);
        Btn_Load.addActionListener(this);
        add(Btn_Load);

        Btn_Close = new JButton("닫기");
        Btn_Close.setBounds(230,210,70,30);
        Btn_Close.addActionListener(this);
        add(Btn_Close);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  취소 버튼의 동작을 구현
    //                취소 동작 후 메인 화면으로 변경되도록 구현
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == Btn_Close) {
            this.setVisible(false);
            MainFrame.display("Main");
        }
        else if(e.getSource()==Btn_Load);{
            GetBalance();
        }
    }
    public void LoadAccount()
    {
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
    // Name : GetBalance()
    // Type : Method
    // Description :  ATMMain의 Send 기능을 호출하여 서버에 계좌조회 요청 메시지를 전달 하는 기능.
    //*******************************************************************
    public void GetBalance() {
        String account=BankUtils.removeHyphens(Com_Account.getSelectedItem().toString());
        CommandDTO commandDTO=new CommandDTO(RequestType.VIEW, account);
        MainFrame.send(commandDTO, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result == -1) {
                    return;
                }
                attachment.flip();
                try {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(attachment.array());
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    CommandDTO command = (CommandDTO) objectInputStream.readObject();
                    SwingUtilities.invokeLater(() -> {
                        String accountNumber = BankUtils.displayAccountNo(command.getUserAccountNo());
                        Text_Account.setText(accountNumber);
                        String balance = BankUtils.displayBalance(command.getBalance());
                        Text_balance.setText(balance + "원");
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
            }
        });
    }
}
