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
// Name : PanViewCustomer
// Type : Class
// Description :  특정 고객의 정보 조회 요청 기능을 제공하는 패널을 구현한 Class
//                - 사용자로부터 고객 ID를 입력받아 서버에 고객 정보 요청을 전송
//                - 서버에서 받은 응답 데이터를 기반으로 고객 정보 화면으로 전환
//                - 조회 실패 시 사용자에게 알림 메시지를 표시하고 입력 필드를 초기화
//                - 취소 버튼을 통해 메인 화면으로 돌아가는 기능 제공
//*******************************************************************
public class PanViewCustomer extends JPanel implements ActionListener {
    private JButton viewButton, cancelButton;
    private JLabel titlelabel, idlabel;
    private  JTextField Text_id;
    ManagerMain MainFrame;

    //*******************************************************************
    // Name : PanViewCustomer()
    // Type : 생성자
    // Description :  PanViewCustomer Class의 생성자 구현
    //*******************************************************************
    public PanViewCustomer(ManagerMain parent) {
        MainFrame = parent;
        InitGUI();
    }

    //*******************************************************************
    // Name : InitGUI
    // Type : Method
    // Description : 고객 조회 패널의 GUI를 초기화 하는 메소드 구현
    //*******************************************************************
    private void InitGUI() {
        setLayout(null);
        setBounds(0,0,480,320);

        //제목
        titlelabel = new JLabel("고객 열람");
        titlelabel.setFont(new Font("Sanserif", Font.BOLD, 20));
        titlelabel.setSize(getWidth(), 60);
        titlelabel.setLocation(0,0);
        titlelabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titlelabel);

        //아이디
        idlabel = new JLabel("id:  ");
        idlabel.setBounds(70,120,70,20);
        idlabel.setHorizontalAlignment(JLabel.RIGHT);
        add(idlabel);

        Text_id = new JTextField();
        Text_id.setBounds(140,120,200,20);
        Text_id.setEditable(true);
        add(Text_id);

        //확인버튼부분
        viewButton = new JButton("열람");
        viewButton.setBounds(240,220,100,50);
        viewButton.addActionListener(this);
        add(viewButton);

        //취소버튼부분
        cancelButton = new JButton("취소");
        cancelButton.setBounds(350,220,100,50);
        cancelButton.addActionListener(this);
        add(cancelButton);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listner
    // Description :  열람 버튼 클릭 시 viewCustomer() 호출하여 고객 정보 조회 요청 전송
    //                취소 버튼 클릭 시 입력 필드 초기화 및 메인 화면으로 전환
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == viewButton) {
            this.setVisible(false);
            viewCustomer();
        } else if ( e.getSource() == cancelButton ) {
            Text_id.setText("");
            MainFrame.display("Main");
            this.setVisible(false);
            MainFrame.display("Main");
        }
    }

    //*******************************************************************
    // Name : viewCustomer()
    // Type : Method
    // Description :  사용자가 입력한 정보를 확인하고 계좌 생성 요청을 서버로 전송
    //                - 입력값 검증 후 CommandDTO 객체를 생성하고 서버로 요청 전송
    //                - 입력값 없을 경우 사용자에게 알림메시지 전송
    //*******************************************************************
    public void viewCustomer(){;
        String id = Text_id.getText();

        if ( id.isEmpty() ) {
            JOptionPane.showMessageDialog(MainFrame, "ID를 입력하세요.");
            this.setVisible(true);
            return;
        }

        CommandDTO command = createCommand(id);
        sendRequestToServer(command);
    }

    //*******************************************************************
    // Name : createCommand()
    // Type : Method
    // Description :  고객 조회 요청에 필요한 데이터를 CommandDTO 객체에 설정하고 반환
    //*******************************************************************
    private CommandDTO createCommand(String id) {
        CommandDTO command = new CommandDTO();
        command.setRequestType(RequestType.VIEW_CUSTOMER);
        command.setId(id);
        return command;
    }

    //*******************************************************************
    // Name : sendRequestToServer()
    // Type : Method
    // Description :  서버로 고객 조회 요청을 비동기적(CompletionHandler)으로 전송
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
                Text_id.setText("");
                return;
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(responseBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);

            CommandDTO response = (CommandDTO) ois.readObject();
            System.out.println("[클라이언트] 응답 데이터: " + response);

            if (response.getResponseType() == ResponseType.SUCCESS) {
                displayCustomerInfo(response);
                Text_id.setText("");
            } else {
                JOptionPane.showMessageDialog(MainFrame, "고객 정보를 찾을 수 없습니다.");
                this.setVisible(true);  // 고객 열람 화면 다시 표시
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame, "응답 처리 중 오류 발생.");
        }
    }

    //*******************************************************************
    // Name : displayCustomerInfo()
    // Type : Method
    // Description :  고객 정보 화면으로 전환
    // Parameters - CommandDTO : 서버에서 전달된 응답 데이터를 포함하는 객체
    //*******************************************************************
    private void displayCustomerInfo(CommandDTO response) {
        SwingUtilities.invokeLater(() -> {
            MainFrame.Pan_customerInfo.setCustomerData(response);
            MainFrame.display("customerInfo"); // 화면 전환
        });
    }

}
