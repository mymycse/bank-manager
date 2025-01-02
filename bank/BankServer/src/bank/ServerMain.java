package bank;

import common.AccountType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//*******************************************************************
// Name : ServerMain
// Type : Class
// Description :  BankServer의 GUI 프레임이며, ATM,BANK_MANAGER와의 소켓통신을 담당한다.
//                계좌 정보들을 보유하고 있으며, 관련 기능들을 가지고 있다.
//*******************************************************************
class ServerMain extends JFrame implements ActionListener, ClientHandler {
    private JLabel Label_UserCount;
    private JLabel Label_UserCount_2;
    private JToggleButton Btn_StartStop;
    private JButton Btn_Reset;
    private JTextArea TextArea_Log;
    private JScrollPane sp;

    private ServerSocket serverSocket;
    private static List<CustomerVO> customerList;
    private List<AccountVO> accountList;
    private List<Client> clientList = new Vector<>();
    private List<ManagerVO> managerList;
    private boolean isRunning;

    //*******************************************************************
    // Name : ServerMain()
    // Type : 생성자
    // Description :  ServerMain Class의 생성자로서 계좌 정보를 Load 하고, GUI를 초기화 한다.
    //                계좌 정보는 ./Account.txt에 저장하며 Server 실행시 Load, 종료시 Save 동작을 한다
    //*******************************************************************
    public ServerMain() {
        InitGui();
        customerList = ReadCustomerFile("./Customer.txt");
        accountList=ReadAccountFile("./Account.txt");
        managerList=ReadManagerFile("./Manager.txt");
        setVisible(true);

        // WindowListener 추가
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 프레임이 종료될 때 SaveCustomerFile 메서드 호출
                SaveCustomerFile(customerList, "./Customer.txt");
                SaveAccountFile(accountList,"./Account.txt");
                SaveManagerFile(managerList,"./Manager.txt");
            }
        });
    }

    //*******************************************************************
    // Name : GetDefaultCustomers()
    // Type : Method
    // Description :  Server 시작 시 저장된 고객 정보가 없으면 Default 고객을 생성하는 기능
    //*******************************************************************
    private static List<CustomerVO> GetDefaultCustomers() {
        List<CustomerVO> customerList = new Vector<>();
        customerList.add(new CustomerVO("202400001", "광수", "202400001", "010-1234-5678","대전 유성구", new ArrayList<>()));
        customerList.add(new CustomerVO("202400002", "영철", "202400002", "010-1212-3434", "부산 해운대구",new ArrayList<>()));
        customerList.add(new CustomerVO("202400003", "영숙", "202400003", "010-5656-7878", "서울 강남구",new ArrayList<>()));
        customerList.add(new CustomerVO("202400004", "옥순", "202400004", "010-1234-1234", "대전 대덕구",new ArrayList<>()));
        return customerList;
    }

    //*******************************************************************
    // Name : GetDefaultAccount()
    // Type : Method
    // Description :  Server 시작 시 저장된 계좌 정보가 없으면 Default 계좌를 생성하는 기능
    //*******************************************************************
    private static List<AccountVO> GetDefaultAccount() {
        List<AccountVO> accountList = new Vector<>();

        // 고객 리스트에서 무작위로 4명 이하의 고객 선택
        List<CustomerVO> selectedCustomers = new ArrayList<>(customerList);
        Collections.shuffle(selectedCustomers);
        int maxAccounts = Math.min(4, selectedCustomers.size());

        for (int i = 0; i < maxAccounts; i++) {
            CustomerVO customer = selectedCustomers.get(i);

            // 계좌 생성
            String accountNo = String.format("%09d", (int) (Math.random() * 1000000000));
            AccountVO account = new AccountVO(
                    customer.getName(),
                    accountNo,
                    AccountType.CHECKING,
                    (long) (Math.random() * 10_000_000 + 1_000_000),
                    Date.valueOf(LocalDate.now())
            );

            // 계좌를 고객과 전체 계좌 리스트에 추가
            customer.getAccounts().add(account);
            accountList.add(account);
        }

        return accountList;
    }

    //*******************************************************************
    // Name : GetDefaultManager()
    // Type : Method
    // Description :  Server 시작 시 저장된 관리자 정보가 없으면 Default 관리자를 생성하는 기능
    //*******************************************************************
    private static List<ManagerVO> GetDefaultManager(){
        List<ManagerVO> managerlist=new Vector<>();
        ManagerVO manager1=new ManagerVO("1234","1234");
        managerlist.add(manager1);
        return  managerlist;
    }

    //*******************************************************************
    // Name : SaveCustomerFile()
    // Type : Method
    // Description :  현재까지의 고객 정보를 txt 파일로 저장하는 기능
    //*******************************************************************
    public void SaveCustomerFile(List<CustomerVO> customers, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(customers);
            System.out.println("Objects saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //*******************************************************************
    // Name : SaveAccountFile()
    // Type : Method
    // Description :  현재까지의 계좌 정보를 txt 파일로 저장하는 기능
    //*******************************************************************
    public void SaveAccountFile(List<AccountVO> accounts, String filePath){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(accounts);
            System.out.println("Objects saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //*******************************************************************
    // Name : SaveManagerFile()
    // Type : Method
    // Description :  현재까지의 관리자 정보를 txt 파일로 저장하는 기능
    //*******************************************************************
    public void SaveManagerFile(List<ManagerVO> managers, String filePath){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(managers);
            System.out.println("Objects saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //*******************************************************************
    // Name : ReadCustomerFile()
    // Type : Method
    // Description :  txt 파일로 저장된 고객 정보를 Load 하는 기능
    //*******************************************************************
    public List<CustomerVO> ReadCustomerFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            List<CustomerVO> customers = (List<CustomerVO>) ois.readObject();
            System.out.println("Objects read from " + filePath);
            return customers;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("File not found. Initializing with default data.");
            List<CustomerVO> defaultCustomers = GetDefaultCustomers();
            SaveCustomerFile(defaultCustomers, filePath);
            return defaultCustomers;
        }
    }

    //*******************************************************************
    // Name : ReadAccountFile()
    // Type : Method
    // Description :  txt 파일로 저장된 계좌 정보를 Load 하는 기능
    //*******************************************************************
    public List<AccountVO> ReadAccountFile(String filePath){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            List<AccountVO> accounts = (List<AccountVO>) ois.readObject();
            System.out.println("Objects read from " + filePath);
            return accounts;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("File not found. Initializing with default data.");
            List<AccountVO> defaultAccount = new ArrayList<>();
            SaveAccountFile(defaultAccount , filePath);
            return defaultAccount;
        }
    }

    //*******************************************************************
    // Name : ReadManagerFile()
    // Type : Method
    // Description :  txt 파일로 저장된 관리자 정보를 Load 하는 기능
    //*******************************************************************
    public List<ManagerVO> ReadManagerFile(String filePath){
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            List<ManagerVO> managers = (List<ManagerVO>) ois.readObject();
            System.out.println("Objects read from " + filePath);
            return managers;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("File not found. Initializing with default data.");
            List<ManagerVO> defaultManager = GetDefaultManager();
            SaveManagerFile(defaultManager , filePath);
            return defaultManager;
        }
    }

    //*******************************************************************
    // Name : InitGui
    // Type : Method
    // Description :  ServerMain Class의 GUI 컴포넌트를 할당하고 초기화 한다.
    //                ServerMain Frame은 서버 시작 버튼 및 텍스트 창 초기화 버튼을 가지고 있다
    //*******************************************************************
    private void InitGui() {
        setTitle("서버 GUI");
        setSize(480, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        Label_UserCount = new JLabel("현재 유저 수: ");
        topPanel.add(Label_UserCount);

        Label_UserCount_2 = new JLabel("0");
        topPanel.add(Label_UserCount_2);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        TextArea_Log = new JTextArea();
        TextArea_Log.setEditable(false);
        sp = new JScrollPane(TextArea_Log);
        mainPanel.add(sp, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        Btn_StartStop = new JToggleButton("시작");
        Btn_StartStop.addActionListener(this);
        bottomPanel.add(Btn_StartStop);

        Btn_Reset = new JButton("텍스트 창 초기화");
        Btn_Reset.addActionListener(this);
        bottomPanel.add(Btn_Reset);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    //*******************************************************************
    // Name : actionPerformed
    // Type : Listener
    // Description :  ServerMain Frame의 버튼 컴포넌트들의 동작을 구현한 부분
    //                아래 코드에서는 서버 Start/Stop 토글 버튼 기능 및 텍스트창 초기화 버튼기능이 구현 되어 있다.
    //*******************************************************************
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == Btn_StartStop) {
            if (Btn_StartStop.isSelected()) {
                startServer();
            } else {
                stopServer();
            }
        } else if (e.getSource() == Btn_Reset) {
            TextArea_Log.setText(null);
        }
    }

    //*******************************************************************
    // Name : startServer
    // Type : Method
    // Description :  서버 소켓을 port 5001 로 bind 하여 open 하는 기능 및
    //                클라이언트 소켓의 접속 시도시 accept 하여 연결 시키는 기능이 구현 되어 있다.
    //*******************************************************************
    public void startServer() {
        isRunning = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5002);
                SwingUtilities.invokeLater(() -> {
                    addMsg("서버 시작");
                    Btn_StartStop.setText("정지");
                });

                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    addMsg("클라이언트 접속: " + clientSocket.getInetAddress());
                    Client client = new Client(clientSocket, ServerMain.this, customerList,accountList,managerList);
                    clientList.add(client);

                    // Update the user count
                    SwingUtilities.invokeLater(() -> Label_UserCount_2.setText(String.valueOf(clientList.size())));
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopServer();
            }
        }).start();
    }

    //*******************************************************************
    // Name : stopServer
    // Type : Method
    // Description :  서버 소켓을 연결 해제 하는 기능
    //*******************************************************************
    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            clientList.clear();
            SwingUtilities.invokeLater(() -> {
                addMsg("서버 정지");
                Btn_StartStop.setText("시작");
                Label_UserCount_2.setText("0");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //*******************************************************************
    // Name : removeClient()
    // Type : Method
    // Description :  클라이언트 소켓이 해제 되었을 때
    //                ServerMain 의 clientList 리스트 에서 해당 인덱스를 제거하는 기능
    //*******************************************************************
    @Override
    public void removeClient(Client client) {
        clientList.remove(client);
        addMsg(client + " 제거됨");

        // Update the user count
        SwingUtilities.invokeLater(() -> Label_UserCount_2.setText(String.valueOf(clientList.size())));
    }

    //*******************************************************************
    // Name : displayInfo()
    // Type : Method (Override)
    // Description :  전달받은 메시지를 추가 메시지 메서드(addMsg)를 통해 처리
    //*******************************************************************
    @Override
    public void displayInfo(String msg) {
        addMsg(msg);
    }

    //*******************************************************************
    // Name : addMsg()
    // Type : Method
    // Description :  입력받은 데이터를 TextArea_Log에 추가
    //*******************************************************************
    public void addMsg(String data) {
        TextArea_Log.append(data + "\n");
    }

    public static void main(String[] args) throws Exception {
        ServerMain f = new ServerMain();
    }
}
