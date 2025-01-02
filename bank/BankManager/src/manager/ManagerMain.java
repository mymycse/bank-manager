package manager;

import common.CommandDTO;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

//*******************************************************************
// Name : ManagerMain
// Type : Class
// Description :  은행관리자의 Gui 프레임 / 서버와의 통신 담당
//*******************************************************************
public class ManagerMain extends JFrame implements ActionListener {
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    public static String ManagerId;

    private JLabel Label_Title;
    private JLabel Label_Image;
    private ImageIcon IconChacha;

    private JButton Btn_addCustomer;
    private JButton Btn_addAccount;
    private JButton Btn_deleteCustomer;
    private JButton Btn_deleteAccount;
    private JButton Btn_viewCustomer;
    private JButton Btn_viewAccount;
    private JButton Btn_exit;
    private JButton Btn_viewCustomerList;
    private JButton Btn_viewAccountList;
    private JButton Btn_logout;

    PanaddCustomer Pan_addCustomer;
    PanaddAccount  Pan_addAccount;
    PandeleteCustomer Pan_deleteCustomer;
    PandeleteAccount Pan_deleteAccount;
    PanViewCustomer Pan_viewCustomer;
    PanViewAccount Pan_viewAccount;
    PanCustomerInfo Pan_customerInfo;
    PanAccountInfo Pan_accountInfo;
    PanViewCustomerList Pan_viewCustomerList;
    PanViewAccountList Pan_viewAccountList;
    PanLogin Pan_login;

    //*******************************************************************
    // Name : ManagerMain()
    // Type : 생성자
    // Description :  ManagerMain Class의 생성자로서, 소켓통신을 시작하고 GUI를 초기화한다
    //*******************************************************************
    public ManagerMain() {
        startClient();
        InitGui();
        display("login");
        setVisible(true);
    }

    //*******************************************************************
    // Name : InitGui
    // Type : Method
    // Description :  ManagerMain Class의 GUI 컴포넌트를 할당하고 초기화한다.
    //                ManagerMain Frame은 각 화면에 해당하는 패널들을 가지고있다.
    //*******************************************************************
    private void InitGui() {
        setLayout(null);
        setTitle("Manager GUI");
        setBounds(0, 0, 480, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        try {
            Image Img_CHACHALogo = ImageIO.read(getClass().getResource("chacha.png"));
            IconChacha = new ImageIcon(Img_CHACHALogo.getScaledInstance(150,150,Image.SCALE_SMOOTH));
            Label_Image = new JLabel();
            Label_Image.setIcon(IconChacha);
            Label_Image.setBounds(150, 70, IconChacha.getIconWidth(), IconChacha.getIconHeight());
            add(Label_Image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Label_Title = new JLabel("CNU BANK MANAGER");
        Label_Title.setFont(new Font("Arial", Font.BOLD, 30));
        Label_Title.setSize(getWidth(), 60);
        Label_Title.setLocation(0, 0);
        Label_Title.setHorizontalAlignment(JLabel.CENTER);
        add(Label_Title);

        Btn_viewCustomer=new JButton("고객 열람");
        Btn_viewCustomer.setSize(100,50);
        Btn_viewCustomer.setLocation(30,60);
        Btn_viewCustomer.addActionListener(this);
        add(Btn_viewCustomer);

        Btn_addCustomer = new JButton("고객 추가");
        Btn_addCustomer.setSize(100, 50);
        Btn_addCustomer.setLocation(30, 120);
        Btn_addCustomer.addActionListener(this);
        add(Btn_addCustomer);

        Btn_deleteCustomer=new JButton("고객 삭제");
        Btn_deleteCustomer.setSize(100,50);
        Btn_deleteCustomer.setLocation(30,180);
        Btn_deleteCustomer.addActionListener(this);
        add(Btn_deleteCustomer);

        Btn_viewCustomerList = new JButton("고객명단");
        Btn_viewCustomerList.setSize(100, 30);
        Btn_viewCustomerList.setLocation(30,240);
        Btn_viewCustomerList.addActionListener(this);
        add(Btn_viewCustomerList);

        Btn_logout =new JButton("로그아웃");
        Btn_logout.setSize(90,30);
        Btn_logout.setLocation(140,240);
        Btn_logout.addActionListener(this);
        add(Btn_logout);

        Btn_viewAccount=new JButton("계좌 열람");
        Btn_viewAccount.setSize(100,50);
        Btn_viewAccount.setLocation(320,60);
        Btn_viewAccount.addActionListener(this);
        add(Btn_viewAccount);

        Btn_addAccount=new JButton("계좌 추가");
        Btn_addAccount.setSize(100,50);
        Btn_addAccount.setLocation(320,120);
        Btn_addAccount.addActionListener(this);
        add(Btn_addAccount);

        Btn_deleteAccount=new JButton("계좌 삭제");
        Btn_deleteAccount.setSize(100,50);
        Btn_deleteAccount.setLocation(320,180);
        Btn_deleteAccount.addActionListener(this);
        add(Btn_deleteAccount);

        Btn_viewAccountList=new JButton("계좌 목록");
        Btn_viewAccountList.setSize(100, 30);
        Btn_viewAccountList.setLocation(320,240);
        Btn_viewAccountList.addActionListener(this);
        add(Btn_viewAccountList);

        Btn_exit = new JButton("종료");
        Btn_exit.setSize(70, 30);
        Btn_exit.setLocation(240,240);
        Btn_exit.setBackground(new Color(255,173,169));
        Btn_exit.addActionListener(this);
        add(Btn_exit);


        Pan_addCustomer=new PanaddCustomer(this);
        add(Pan_addCustomer);
        Pan_addCustomer.setVisible(false);

        Pan_addAccount=new PanaddAccount(this);
        add(Pan_addAccount);
        Pan_addAccount.setVisible(false);

        Pan_deleteCustomer=new PandeleteCustomer(this);
        add(Pan_deleteCustomer);
        Pan_deleteCustomer.setVisible(false);

        Pan_deleteAccount=new PandeleteAccount(this);
        add(Pan_deleteAccount);
        Pan_deleteAccount.setVisible(false);

        Pan_viewCustomer=new PanViewCustomer(this);
        add(Pan_viewCustomer);
        Pan_viewCustomer.setVisible(false);

        Pan_viewAccount=new PanViewAccount(this);
        add(Pan_viewAccount);
        Pan_viewAccount.setVisible(false);

        Pan_customerInfo=new PanCustomerInfo(this);
        add(Pan_customerInfo);
        Pan_customerInfo.setVisible(false);

        Pan_accountInfo=new PanAccountInfo(this);
        add(Pan_accountInfo);
        Pan_accountInfo.setVisible(false);

        Pan_viewCustomerList=new PanViewCustomerList(this);
        add(Pan_viewCustomerList);
        Pan_viewCustomerList.setVisible(false);

        Pan_viewAccountList=new PanViewAccountList(this);
        add(Pan_viewAccountList);
        Pan_viewAccountList.setVisible(false);

        Pan_login=new PanLogin(this);
        add(Pan_login);
        Pan_login.setVisible(false);
    }

    //*******************************************************************
    // Name : startClient()
    // Type : Method
    // Description :  ManagerMain Class 가 가지고있는 소켓을 서버소켓에 접속시킨다
    //*******************************************************************
    private void startClient() {
        if (socket != null && !socket.isClosed()) {
            System.out.println("이미 서버와 연결되어 있습니다.");
            return;
        }
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 5002));
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            System.out.println("뱅크 서버 접속");
        } catch (IOException e) {
            e.printStackTrace();
            disconnectServer();
        }
    }

    //*******************************************************************
    // Name : stopClient(), disconnectServer()
    // Type : Method
    // Description :  ManagerMain Class 가 가지고있는 소켓의 연결을 해제한다.
    //*******************************************************************
    public void stopClient() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("연결 종료");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectServer() {
        stopClient();
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listener
    // Description :  ManagerMain Frame의 버튼 컴포넌트들의 동작을 구현한 부분
    //                아래 코드에서는 각 기능별 화면으로 전환하는 코드가 작성되어있다.
    //*******************************************************************

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==Btn_addCustomer){
            display("addCustomer");
        }
        else if(e.getSource()==Btn_addAccount){
            display("addAccount");
        }
        else if(e.getSource()==Btn_deleteCustomer){
            display("deleteCustomer");
        }
        else if(e.getSource()==Btn_deleteAccount){
            display("deleteAccount");
        }
        else if(e.getSource()==Btn_viewCustomer){
            display("viewCustomer");
        }
        else if(e.getSource()==Btn_viewAccount){
            display("viewAccount");
        }
        else if(e.getSource()==Btn_viewCustomerList){
            display("viewCustomerList");
        }
        else if(e.getSource()==Btn_viewAccountList){
            display("viewAccountList");
        }
        else if(e.getSource()== Btn_logout){
            ManagerId = null;
            JOptionPane.showMessageDialog(this, "로그아웃 되었습니다.");
            display("login");
        }else if(e.getSource()==Btn_exit){
            stopClient();
            System.out.println("프로그램 종료");
            dispose();
        }
    }


    //*******************************************************************
    // Name : display()
    // Type : Method
    // Description :  화면 전환을 담당하는 메서드로, viewName에 따라 특정 화면을 활성화한다.
    //                로그인 상태를 확인하고 로그인되지 않은 상태에서는 제한된 화면만 접근할 수 있다.
    //*******************************************************************

    public void display(String viewName) {
        if (ManagerId == null) {
            if (!viewName.equals("login") && !viewName.equals("Main")) {
                JOptionPane.showMessageDialog(null, "카드를 투입하거나 로그인하세요.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        SetFrameUI(false);
        switch (viewName) {
            case "addCustomer" -> Pan_addCustomer.setVisible(true);
            case "addAccount" -> Pan_addAccount.setVisible(true);
            case "deleteCustomer" -> Pan_deleteCustomer.setVisible(true);
            case "deleteAccount" -> Pan_deleteAccount.setVisible(true);
            case "viewCustomer" -> Pan_viewCustomer.setVisible(true);
            case "viewAccount" -> Pan_viewAccount.setVisible(true);
            case "customerInfo" -> Pan_customerInfo.setVisible(true);
            case "accountInfo" -> Pan_accountInfo.setVisible(true);
            case "viewCustomerList" -> Pan_viewCustomerList.setVisible(true);
            case "viewAccountList" -> Pan_viewAccountList.setVisible(true);
            case "login"-> {
                Pan_login.setVisible(true);
                Pan_login.requestFocus();   // 입력 포커스 설정
            }
            case "Main" -> SetFrameUI(true);
        }
    }

    //*******************************************************************
    // Name : SetFrameUI()
    // Type : Method
    // Description : ManagerMain 모든 컴포넌트의 가시성을 설정한다.
    //               파라미터 b에 따라 각 컴포넌트의 visible 속성을 변경한다.
    //*******************************************************************

    private void SetFrameUI(boolean b) {
        Label_Title.setVisible(b);
        Label_Image.setVisible(b);
        Btn_addCustomer.setVisible(b);
        Btn_addAccount.setVisible(b);
        Btn_deleteCustomer.setVisible(b);
        Btn_deleteAccount.setVisible(b);
        Btn_viewCustomer.setVisible(b);
        Btn_viewAccount.setVisible(b);
        Btn_exit.setVisible(b);
        Btn_viewCustomerList.setVisible(b);
        Btn_viewAccountList.setVisible(b);
        Btn_logout.setVisible(b);
    }


    //*******************************************************************
    // Name : send()
    // Type : Method
    // Description :  CommandDTO를 매개변수로 하여 서버에 요청 메시지를 전달하는 메소드
    //                CommandDTO Class 에는 Manager 서비스 요청에 필요한 데이터들이 정의 되어 있다.
    //                ManagerMain Class는 BankServiceHandler 인터페이스를 상속하였다.
    //*******************************************************************

    public void send(CommandDTO commandDTO, CompletionHandler<Integer, ByteBuffer> handlers) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(commandDTO);
            objectOutputStream.flush();

            byte[] serializedData = byteArrayOutputStream.toByteArray();
            outputStream.write(serializedData);
            outputStream.flush();

            // 데이터 길이 먼저 읽기
            byte[] lengthBuffer = new byte[4];
            inputStream.read(lengthBuffer);
            int dataLength = ByteBuffer.wrap(lengthBuffer).getInt();

            // 데이터 읽기
            byte[] buffer = new byte[dataLength];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead == dataLength) {
                ByteBuffer responseBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                handlers.completed(bytesRead, responseBuffer);
            } else {
                handlers.failed(new IOException("Incomplete data read"), null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            disconnectServer();
            handlers.failed(e, null);
        }
    }

    public static void main(String[] args) {
        ManagerMain my = new ManagerMain();
    }

    }







