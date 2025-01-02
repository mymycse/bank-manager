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
// Name : PanaddCustomer
// Type : Class
// Description :  신규 고객 등록 화면을 구현한 Class
//                - 사용자로부터 정보를 입력받아 서버에 고객 등록 요청 전송
//*******************************************************************
public class PanaddCustomer extends JPanel implements ActionListener {
    private JButton registerButton, cancelButton;
    private JLabel titlelabel, namelabel, idlabel, pwlabel, phonelabel, adresslabel;
    private  JTextField Text_name, Text_id, Text_pw, Text_phone;
    private JTextArea Text_adress;
    ManagerMain MainFrame;

    //*******************************************************************
    // Name : PanaddCustomer()
    // Type : 생성자
    // Description :  PanaddCustomer Class의 생성자 구현
    //*******************************************************************
    public PanaddCustomer(ManagerMain parent){
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description : 고객 추가 패널의 GUI를 초기화하는 메소드 구현
    //*******************************************************************
    private void InitGUI() {
        setLayout(null);
        setBounds(0, 0, 480, 400);

        //제목
        titlelabel = new JLabel("고객 추가");
        titlelabel.setFont(new Font("Sanserif", Font.BOLD, 20));
        titlelabel.setSize(getWidth(), 60);
        titlelabel.setLocation(0,0);
        titlelabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titlelabel);

        //이름
        namelabel = new JLabel("이름: ");
        namelabel.setBounds(70,60,70,20);
        namelabel.setHorizontalAlignment(JLabel.RIGHT);
        add(namelabel);

        Text_name = new JTextField();
        Text_name.setBounds(140,60,200,20);
        Text_name.setEditable(true);
        add(Text_name);

        //아이디
        idlabel = new JLabel("id: ");
        idlabel.setBounds(70,90,70,20);
        idlabel.setHorizontalAlignment(JLabel.RIGHT);
        add(idlabel);

        Text_id = new JTextField();
        Text_id.setBounds(140,90,200,20);
        Text_id.setEditable(true);
        add(Text_id);

        //패스워드
        pwlabel = new JLabel("pw: ");
        pwlabel.setBounds(70,120,70,20);
        pwlabel.setHorizontalAlignment(JLabel.RIGHT);
        add(pwlabel);

        Text_pw = new JTextField();
        Text_pw.setBounds(140,120,200,20);
        Text_pw.setEditable(true);
        add(Text_pw);

        //연락처
        phonelabel = new JLabel("연락처: ");
        phonelabel.setBounds(70,150,70,20);
        phonelabel.setHorizontalAlignment(JLabel.RIGHT);
        add(phonelabel);

        Text_phone = new JTextField();
        Text_phone.setBounds(140,150,200,20);
        Text_phone.setToolTipText("하이픈 입력 권장");
        Text_phone.setEditable(true);
        add(Text_phone);

        //주소
        adresslabel = new JLabel("주소: ");
        adresslabel.setBounds(70,180,70,20);
        adresslabel.setHorizontalAlignment(JLabel.RIGHT);
        add(adresslabel);

        Text_adress = new JTextArea();
        Text_adress.setBounds(140,180,200,40);
        Text_adress.setEditable(true);
        Text_adress.setLineWrap(true);
        add(Text_adress);

        //등록버튼
        registerButton = new JButton("등록");
        registerButton.setBounds(240,230,100,40);
        registerButton.addActionListener(this);
        add(registerButton);

        //취소버튼
        cancelButton = new JButton("취소");
        cancelButton.setBounds(350,230,100,40);
        cancelButton.addActionListener(this);
        add(cancelButton);

    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  등록 버튼, 취소 버튼의 동작을 구현
    //                등록, 취소 동작 후 메인 화면으로 변경되도록 구현
    //*******************************************************************
    public void actionPerformed(ActionEvent e){
        if(e.getSource()==registerButton){
            addCustomer();
        } else if (e.getSource()==cancelButton){
            Text_name.setText("");
            Text_id.setText("");
            Text_pw.setText("");
            Text_phone.setText("");
            Text_adress.setText("");
            this.setVisible(false);
            MainFrame.display("Main");
        }
    }

    //*******************************************************************
    // Name : addCustomer()
    // Type : Method
    // Description : 신규 고객 등록을 위해 사용자로부터 입력받은 정보를 검증하고 서버로 전송
    //                등록, 취소 동작 후 메인 화면으로 변경되도록 구현
    //*******************************************************************
    public void addCustomer(){
        String newid=Text_id.getText();
        String newname=Text_name.getText();
        String newpw=Text_pw.getText();
        String newph=Text_phone.getText();
        String newad=Text_adress.getText();

        if ( newname.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "이름을 입력하세요.");
            return;
        }

        if ( newid.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "ID를 입력하세요.");
            return;
        }

        if ( newpw.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "비밀번호를 입력하세요.");
            return;
        }

        if ( newph.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "연락처를 입력하세요.");
            return;
        }

        if ( newad.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "주소를 입력하세요.");
            return;
        }

        if ( newpw.length() < 6 ) {
            JOptionPane.showMessageDialog(MainFrame, "6자리 이상의 비밀번호를 설정해주세요.");
            return;
        }
        CommandDTO command = createCommand();
        sendRequestToServer(command);
    }

    //*******************************************************************
    // Name : createCommand()
    // Type : Method
    // Description : 고객 생성 요청에 필요한 데이터를 CommandDTO 객체에 설정하고 반환
    //*******************************************************************
    private CommandDTO createCommand() {
        CommandDTO command = new CommandDTO();
        command.setRequestType(RequestType.ADD_CUSTOMER);
        command.setName(Text_name.getText());
        command.setId(Text_id.getText());
        command.setPassword(Text_pw.getText());
        command.setPhone(Text_phone.getText());
        command.setAddress(Text_adress.getText());
        return command;
    }

    //*******************************************************************
    // Name : sendRequestToServer()
    // Type : Method
    // Description :  서버로 고객 생성 요청을 비동기적(CompletionHandler)으로 전송
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
                this.setVisible(true);
                MainFrame.display("addCustomer");
                return;
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(responseBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);

            CommandDTO response = (CommandDTO) ois.readObject();
            System.out.println("[클라이언트] 응답 데이터: " + response);

            if (response.getResponseType() == ResponseType.SUCCESS) {
                System.out.println("[클라이언트] 신규 고객: " + response.getName()+ " id: " + response.getId());
                JOptionPane.showMessageDialog(MainFrame, "신규 고객 " + response.getName()+ " 님 환영합니다.");
            } else if (response.getResponseType() == ResponseType.OVERLAP) {
                JOptionPane.showMessageDialog(MainFrame, "이미 존재하는 id입니다.");
                this.setVisible(true);
                MainFrame.display("addCustomer");
                return;
            } else {
                JOptionPane.showMessageDialog(MainFrame, "고객 추가에 실패했습니다.");
            }
            Text_name.setText("");
            Text_id.setText("");
            Text_pw.setText("");
            Text_phone.setText("");
            Text_adress.setText("");
            this.setVisible(false);
            MainFrame.display("Main");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame, "응답 처리 중 오류 발생.");
        }
    }
}
