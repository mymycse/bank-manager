package bank;

import common.AccountType;
import common.CommandDTO;
import common.RequestType;
import common.ResponseType;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

//*******************************************************************
// Name : Client
// Type : Class
// Description :  서버와 클라이언트 간 통신을 처리하는 클래스
//                - 클라이언트로부터 요청 수신 및 서버 처리 결과 전송
//*******************************************************************
public class Client {
    private Socket clientSocket;
    private ClientHandler handler;
    private List<CustomerVO> customerList;
    private List<AccountVO> accountList;
    private List<ManagerVO> managerList;
    private InputStream inputStream;
    private OutputStream outputStream;

    //*******************************************************************
    // Name : Client()
    // Type : 생성자
    // Description :  Client 클래스의 생성자로, 클라이언트 소켓과 데이터 스트림 초기화
    // Parameters
    //       - clientSocket : 클라이언트와의 연결을 나타내는 소켓
    //       - handler : 클라이언트 관리를 담당하는 핸들러
    //       - customerList : 고객 정보 리스트
    //       - accountList : 계좌 정보 리스트
    //       - managerList : 관리자 정보 리스트
    //*******************************************************************
    public Client(Socket clientSocket, ClientHandler handler, List<CustomerVO> customerList,List<AccountVO> accountList,List<ManagerVO> managerList) {
        this.clientSocket = clientSocket;
        this.handler = handler;
        this.customerList = customerList;
        this.accountList=accountList;
        this.managerList=managerList;

        try {
            outputStream = clientSocket.getOutputStream();
            inputStream = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        receive();
    }

    //*******************************************************************
    // Name : receive()
    // Type : Method
    // Description :  클라이언트로부터 데이터를 비동기적으로 수신
    //*******************************************************************
    private void receive() {
        new Thread(() -> {
            try {
                while (!clientSocket.isClosed()) {
                    byte[] buffer = new byte[4096];  // 버퍼 크기를 지정
                    int bytesRead = inputStream.read(buffer);

                    if (bytesRead == -1) {
                        System.out.println("[서버] 연결이 종료되었습니다.");
                        disconnectClient();
                        break;
                    }

                    // 받은 바이트 데이터를 객체로 역직렬화
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer, 0, bytesRead);
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    CommandDTO command = (CommandDTO) objectInputStream.readObject();

                    // 요청 타입에 따른 처리
                    if (command != null) {
                        switch (command.getRequestType()) {
                            case LOAD_ACCOUNT -> load(command);
                            case VIEW -> viewAccount(command);
                            case LOGIN -> login(command);
                            case TRANSFER -> transfer(command);
                            case DEPOSIT -> deposit(command);
                            case WITHDRAW -> withdraw(command);
                            case ADD_CUSTOMER -> addCustomer(command);
                            case DELETE_CUSTOMER -> deleteCustomer(command);
                            case ADD_ACCOUNT -> addAccount(command);
                            case DELETE_ACCOUNT -> deleteAccount(command);
                            case VIEW_CUSTOMER -> viewCustomer(command);
                            case VIEW_ACCOUNT -> viewAccount(command);
                            case VIEW_CUSTOMER_LIST -> viewCustomerList();
                            case VIEW_ACCOUNT_LIST -> viewAccountList();
                            case MANAGER_LOGIN -> managerlogin(command);
                            default -> {}
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                disconnectClient();
            }
        }).start();
    }

    //*******************************************************************
    // Name : send()
    // Type : Method
    // Description : 서버에서 클라이언트로 CommandDTO를 전송하는 메서드 (바이트 배열 전송)
    //*******************************************************************
    private void send(CommandDTO commandDTO) {
        try {
            System.out.println("[서버] CommandDTO 전송 준비: " + commandDTO);

            // CommandDTO 객체를 직렬화하여 바이트 배열로 변환
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(commandDTO);
            objectOutputStream.flush();

            byte[] serializedData = byteArrayOutputStream.toByteArray();
            int dataLength = serializedData.length;

            System.out.println("[서버] 전송할 데이터 크기: " + dataLength + " bytes");

            // 데이터 크기를 먼저 전송 (4바이트 정수)
            outputStream.write(ByteBuffer.allocate(4).putInt(dataLength).array());
            outputStream.flush();

            // 직렬화된 바이트 데이터를 서버로 전송
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            disconnectClient();
        }
    }

    //*******************************************************************
    // Name : disconnectClient()
    // Type : Method
    // Description : 클라이언트 연결 종료 처리
    //*******************************************************************
    private void disconnectClient() {
        try {
            clientSocket.close();  // 소켓 닫기
            handler.removeClient(this);  // 클라이언트 제거
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //*******************************************************************
    // Name : login()
    // Type : Method
    // Description : 클라이언트의 로그인 요청 처리
    //               - 입력받은 CommandDTO 객체에 포함된 ID와 비밀번호를 확인
    //               - 검색 성공 시 성공 응답을 CommandDTO에 설정하고 클라이언트로 전송
    //               - 검색 실패 시 실패 응답(ResponseType.FAILURE)을 CommandDTO에 설정하고 클라이언트로 전송
    //               - 결과를 클라이언트로 전송
    //*******************************************************************
    private synchronized void login(CommandDTO commandDTO) {
        Optional<CustomerVO> customer = this.customerList.stream()
                .filter(customerVO -> Objects.equals(customerVO.getId(), commandDTO.getId())
                        && Objects.equals(customerVO.getPassword(), commandDTO.getPassword()))
                .findFirst();

        if (customer.isPresent()) {
            commandDTO.setResponseType(ResponseType.SUCCESS);
            handler.displayInfo(customer.get().getName() + "님이 로그인하였습니다.");
        }
        else {
            commandDTO.setResponseType(ResponseType.FAILURE);
        }

        send(commandDTO);
    }

    //*******************************************************************
    // Name : load()
    // Type : Method
    // Description : 사용자의 계좌 목록 불러오기
    //               - 입력받은 CommandDTO 객체에 포함된 ID를 사용해 고객 정보를 검색
    //               - 고객이 보유한 계좌 목록(accountList)을 CommandDTO 객체에 설정
    //               - 계좌가 없는 경우 ResponseType.NULL_ACCOUNT 응답 설정
    //               - 계좌가 있는 경우 계좌 목록을 포함해 ResponseType.SUCCESS 응답 설정
    //               - 처리 결과를 클라이언트로 전송
    //*******************************************************************
    private synchronized void load(CommandDTO commandDTO){
        CustomerVO customer = this.customerList.stream()
                .filter(customerVO -> Objects.equals(customerVO.getId(), commandDTO.getId()))
                .findFirst().get();
        List<AccountVO> accountlist=customer.getAccounts();

        if(accountlist.size()==0){
            commandDTO.setResponseType(ResponseType.NULL_ACCOUNT);
        }
        else {
            commandDTO.setAccountlist(accountlist);
            commandDTO.setResponseType(ResponseType.SUCCESS);
        }
        send(commandDTO);
    }

    //*******************************************************************
    // Name : view()
    // Type : Method
    // Description : 클라이언트의 계좌 조회 요청 처리
    //               - 입력받은 CommandDTO 객체에 포함된 계좌번호를 사용해 계좌 정보를 검색
    //               - 검색된 계좌의 잔액, 계좌번호를 CommandDTO 객체에 설정
    //               - 조회 결과를 클라이언트로 전송하고 서버 로그에 기록
    //*******************************************************************
    private synchronized void view(CommandDTO commandDTO) {

        AccountVO account= this.accountList.stream().filter(accountV0->Objects.equals(accountV0.getAccountNo(),commandDTO.getUserAccountNo())).findFirst().get();
        commandDTO.setBalance(account.getBalance());
        commandDTO.setUserAccountNo(account.getAccountNo());

        handler.displayInfo(account.getOwner() + "님의 계좌 잔액은 " + account.getBalance() + "원 입니다.");
        send(commandDTO);
    }

    //*******************************************************************
    // Name : transfer()
    // Type : Method
    // Description : 클라이언트의 계좌 이체 요청 처리
    //               - 송금 계좌와 수신 계좌를 검증하고 금액 이체 처리
    //               - 이체 성공 및 실패 조건에 따라 응답 메시지를 설정하고 전송
    //*******************************************************************
    private synchronized void transfer(CommandDTO commandDTO) {
        CustomerVO user = this.customerList.stream()
                .filter(customerVO -> Objects.equals(customerVO.getId(),commandDTO.getId()))
                .findFirst().orElse(null);

        AccountVO userAccount=this.accountList.stream().filter(accountV0->Objects.equals(accountV0.getAccountNo(),commandDTO.getUserAccountNo())).
                findFirst().orElse(null);

        Optional<AccountVO> receiverOptional= this.accountList.stream().filter(accountVO -> Objects.equals(accountVO.getAccountNo(), commandDTO.getReceivedAccountNo())).
                findFirst();

        if (!receiverOptional.isPresent() || receiverOptional.get().getAccountNo().equals(userAccount.getAccountNo())) {
            // 수신 계좌가 존재하지 않거나 송금 계좌와 동일한 경우
            commandDTO.setResponseType(ResponseType.WRONG_ACCOUNT_NO);
        }
        else if (!user.getPassword().equals(commandDTO.getPassword())) {
            // 송금자 비밀번호가 일치하지 않는 경우
            commandDTO.setResponseType(ResponseType.WRONG_PASSWORD);
        }
        else if (userAccount.getBalance() < commandDTO.getAmount()) {
            // 송금 계좌 잔액이 부족한 경우
            commandDTO.setResponseType(ResponseType.INSUFFICIENT);
        }
        else {
            AccountVO receiverAccount = receiverOptional.get();

            // 이체 처리
            userAccount.setBalance(userAccount.getBalance() - commandDTO.getAmount());
            receiverAccount.setBalance(receiverAccount.getBalance() + commandDTO.getAmount());

            // 고객의 계좌 목록 업데이트
            user.getAccounts().removeIf(accountVO -> accountVO.getAccountNo().equals(userAccount.getAccountNo()));
            user.getAccounts().add(userAccount);

            // 수신 고객의 계좌 목록 업데이트
            for (CustomerVO customer : this.customerList) {
                if (customer.getAccounts().contains(receiverAccount)) {
                    customer.getAccounts().removeIf(accountVO -> accountVO.getAccountNo().equals(receiverAccount.getAccountNo()));
                    customer.getAccounts().add(receiverAccount);
                    break;
                }
            }

            // 성공 응답 설정
            commandDTO.setResponseType(ResponseType.SUCCESS);
            handler.displayInfo(userAccount.getAccountNo() + " 계좌에서 " + receiverAccount.getAccountNo() + " 계좌로 " + commandDTO.getAmount() + "원 이체하였습니다.");
        }
        send(commandDTO);
    }

    //*******************************************************************
    // Name : deposit()
    // Type : Method
    // Description : 클라이언트의 입금 요청 처리
    //               - 송금 계좌와 입금 금액을 확인하고 계좌에 잔액을 추가
    //               - 입금 성공 시 응답 메시지와 상태를 설정
    //*******************************************************************
    private synchronized void deposit(CommandDTO commandDTO) {
        // 입금 계좌 조회
        AccountVO account= this.accountList.stream().filter(accountV0->Objects.equals(accountV0.getAccountNo(),commandDTO.getUserAccountNo())).
                findFirst().get();
        // 입금자의 정보 조회
        CustomerVO user = this.customerList.stream()
                .filter(customerVO -> Objects.equals(customerVO.getId(),commandDTO.getId()))
                .findFirst().get();

        // 계좌 상태 업데이트
        //    - 입금할 계좌를 고객의 계좌 목록에서 제거
        //    - 새로 갱신된 잔액을 설정한 후 다시 고객 계좌 목록에 추가
        user.getAccounts().remove(account);
        account.setBalance(commandDTO.getAmount()+account.getBalance());
        user.getAccounts().add(account);

        // 계좌 상태 갱신 하기
        user.setChecking_balance(user.getAccounts());
        user.setSaving_balance(user.getAccounts());

        // 응답 처리 및 서버 로그 기록
        commandDTO.setResponseType(ResponseType.SUCCESS);
        handler.displayInfo(user.getName() + "님이 " + account.getAccountNo() + " 계좌에 " + commandDTO.getAmount() + "원 입금하였습니다.");

        send(commandDTO);
    }

    //*******************************************************************
    // Name : withdraw()
    // Type : Method
    // Description : 클라이언트의 출금 요청 처리
    //               - 계좌 잔액 확인 후 출금 처리
    //               - 출금 금액이 부족할 경우 추가적인 계좌에서 자금을 충당 (체크 계좌 우선)
    //               - 잔액이 부족하면 오류 응답을 반환
    //*******************************************************************
    private synchronized void withdraw(CommandDTO commandDTO) {

        AccountVO account= this.accountList.stream().filter(accountV0->Objects.equals(accountV0.getAccountNo(),commandDTO.getUserAccountNo())).
                findFirst().get();

        CustomerVO user = this.customerList.stream()
                .filter(customerVO -> Objects.equals(customerVO.getId(),commandDTO.getId()))
                .findFirst().get();

        long tmp=commandDTO.getAmount();

        if (account.getBalance() > commandDTO.getAmount()) {
            commandDTO.setResponseType(ResponseType.SUCCESS);
            account.setBalance(account.getBalance()-commandDTO.getAmount());
            handler.displayInfo(user.getName() + "님이 " + account.getAccountNo() + " 계좌에서 " + commandDTO.getAmount() + "원 출금하였습니다.");
        }
        else {
            if (account.getType() == AccountType.SAVINGS) {
                commandDTO.setResponseType(ResponseType.INSUFFICIENT);
                System.out.println(1);
            } else if (account.getType() == AccountType.CHECKING) {
                System.out.println(2);
                System.out.println(user.getTotal_balance());
                if (user.getTotal_balance() < commandDTO.getAmount()) {
                    commandDTO.setResponseType(ResponseType.INSUFFICIENT);
                }
                else{
                    tmp-=account.getBalance();
                    user.getAccounts().remove(account);
                    account.setBalance(0);
                    user.getAccounts().add(account);
                    while(tmp==0){
                        Optional<AccountVO> checkaccount = this.accountList.stream()
                                .filter(accountVO -> Objects.equals(accountVO.getType(), AccountType.CHECKING))
                                .max(Comparator.comparing(AccountVO::getBalance));
                        if(checkaccount.isPresent()){
                            if(tmp>checkaccount.get().getBalance()){
                                tmp-=checkaccount.get().getBalance();
                                user.getAccounts().remove(checkaccount.get());
                                checkaccount.get().setBalance(0);
                                user.getAccounts().add(checkaccount.get());
                            }
                            else{
                                user.getAccounts().remove(checkaccount.get());
                                checkaccount.get().setBalance(checkaccount.get().getBalance()-tmp);
                                user.getAccounts().add(checkaccount.get());
                                tmp=0;
                                commandDTO.setResponseType(ResponseType.SUCCESS);
                            }

                        }
                        else{
                            break;
                        }
                    }
                    while(tmp==0){
                        Optional<AccountVO> saveaccount = this.accountList.stream()
                                .filter(accountVO -> Objects.equals(accountVO.getType(), AccountType.SAVINGS))
                                .max(Comparator.comparing(AccountVO::getBalance));
                        if(saveaccount.isPresent()){
                            if(tmp>saveaccount.get().getBalance()){
                                tmp-=saveaccount.get().getBalance();
                                user.getAccounts().remove(saveaccount.get());
                                saveaccount.get().setBalance(0);
                                user.getAccounts().add(saveaccount.get());
                            }
                            else{
                                user.getAccounts().remove(saveaccount.get());
                                saveaccount.get().setBalance(saveaccount.get().getBalance()-tmp);
                                user.getAccounts().add(saveaccount.get());
                                tmp=0;
                                commandDTO.setResponseType(ResponseType.SUCCESS);
                            }

                        }
                        else {
                            break;
                        }
                    }

                }
            }
        }

        send(commandDTO);
    }

    //*******************************************************************
    // Name : addCustomer()
    // Type : Method
    // Description : 클라이언트의 고객 추가 요청 처리
    //               - 전달받은 CommandDTO를 바탕으로 고객 정보를 확인
    //               - 중복된 ID가 존재하는 경우 응답 상태를 OVERLAP로 설정
    //               - 새로운 고객을 생성하여 고객 리스트에 추가
    //*******************************************************************
    private synchronized void addCustomer(CommandDTO commandDTO){
        System.out.println("[서버] ADD_CUSTOMER 요청 처리: id="+commandDTO.getId());

        // 1. 고객 ID 중복 여부 확인
        Optional<CustomerVO> optionalCustomerVO=this.customerList.stream()
                .filter(customerVO -> Objects.equals(customerVO.getId(),commandDTO.getId()))
                .findFirst();

        if(optionalCustomerVO.isPresent()){
            // 이미 존재하는 고객 ID인 경우
            commandDTO.setResponseType(ResponseType.OVERLAP);
            System.out.println("[서버] 이미 존재하는 id입니다.");
        }
        else{
            // 2. 신규 고객 생성 및 추가
            commandDTO.setResponseType(ResponseType.SUCCESS);
            CustomerVO newCustomer = new CustomerVO();
            List<AccountVO> newAcclist = new ArrayList<>();

            // 고객 정보 설정
            newCustomer.setName(commandDTO.getName());
            newCustomer.setId(commandDTO.getId());
            newCustomer.setPassword(commandDTO.getPassword());
            newCustomer.setPhone(commandDTO.getPhone());
            newCustomer.setAddress(commandDTO.getAddress());
            newCustomer.setAccounts(newAcclist);

            // 고객 리스트에 추가
            customerList.add(newCustomer);
            System.out.println("[서버] 신규 고객 "+newCustomer);
        }

        // 3. 클라이언트로 응답 전송
        send(commandDTO);
    }

    //*******************************************************************
    // Name : deleteCustomer()
    // Type : Method
    // Description : 클라이언트의 고객 제거 요청 처리
    //               - 전달받은 CommandDTO를 기반으로 고객 ID, 비밀번호, 이름의 유효성 검사
    //               - 유효하지 않은 경우 해당 상태 설정 후 클라이언트에 응답
    //               - 유효한 고객인 경우 고객 정보와 연관 데이터를 삭제
    //*******************************************************************
    private synchronized void deleteCustomer(CommandDTO commandDTO){
        handler.displayInfo("[서버] 고객 제거 요청: id="+commandDTO.getId());
        String id = commandDTO.getId();
        String pw = commandDTO.getPassword();
        String name = commandDTO.getName();

        // 1. 고객 ID를 기반으로 조회
        Optional<CustomerVO> optionalCustomerVO=this.customerList.stream()
                .filter(customerVO -> Objects.equals(customerVO.getId(),id))
                .findFirst();

        if ( !optionalCustomerVO.isPresent() ) {
            // 존재하지 않는 ID인 경우
            handler.displayInfo("[서버] 존재하지 않는 id="+id);
            commandDTO.setResponseType(ResponseType.WRONG_ID);
            send(commandDTO);
            return;
        }

        CustomerVO user = optionalCustomerVO.get();

        // 2. 비밀번호가 일치하지 않는 경우
        if ( !user.getPassword().equals(pw)) {
            handler.displayInfo("[서버] 비밀번호 불일치 id="+id);
            commandDTO.setResponseType(ResponseType.WRONG_PASSWORD);
            send(commandDTO);
            return;
        }

        // 3. 이름이 일치하지 않는 경우
        if ( !user.getName().equals(name)) {
            handler.displayInfo("[서버] 고객명 불일치 id:"+id+" 이름: "+name);
            commandDTO.setResponseType(ResponseType.WRONG_NAME);
            send(commandDTO);
            return;
        }

        // 4. 고객 및 보유 계좌 삭제
        this.customerList.remove(optionalCustomerVO.get());
        List<AccountVO> userAccounts = optionalCustomerVO.get().getAccounts();
        for (AccountVO accountVO : userAccounts) {
            this.accountList.remove(accountVO);
        }

        handler.displayInfo("[서버] "+commandDTO.getName()+" 님 계정 삭제");
        commandDTO.setResponseType(ResponseType.SUCCESS);

        // 5. 클라이언트로 응답 전송
        send(commandDTO);
    }

    //*******************************************************************
    // Name : addAccount()
    // Type : Method
    // Description : 클라이언트의 계좌 추가 요청 처리
    //               - 전달받은 CommandDTO를 기반으로 고객 ID와 비밀번호 유효성 검사
    //               - 계좌 번호 생성 (중복 방지)
    //               - 신규 계좌 생성 및 고객, 계좌 리스트에 추가
    //               - 생성된 계좌 정보를 클라이언트에 전송
    //*******************************************************************
    private synchronized void addAccount(CommandDTO command) {
        int randomAcc;
        String account;

        // 1. 고객 ID 기반 조회
        Optional<CustomerVO> customer = this.customerList.stream()
                .filter(customerVO -> Objects.equals(customerVO.getId(), command.getId()))
                .findFirst();

        if (!customer.isPresent()) {
            // 존재하지 않는 ID 처리
            System.out.println("[서버] 존재하지 않는 id입니다: " + command.getId());
            command.setResponseType(ResponseType.WRONG_ID);
            send(command);
            return;
        }

        // 2. 비밀번호 일치 여부 확인
        System.out.println("[서버] 비밀번호 일치 여부 확인중..");
        if (!customer.get().getPassword().equals(command.getPassword())) {
            System.out.println("[서버] 비밀번호 불일치");
            command.setResponseType(ResponseType.WRONG_PASSWORD);
            send(command);
            return;
        }

        // 3. 조회된 고객 정보 설정
        CustomerVO user = customer.get();
        System.out.println("[서버] 조회된 고객 정보: " + user);

        // 4. 계좌 번호 생성 (중복 방지)
        while (true) {
            randomAcc = (int) (Math.random() * 1000000000);
            account = String.format("%09d", randomAcc);
            String randomAccount = account;
            Optional<AccountVO> accountOptional = this.accountList.stream()
                    .filter(accountVO -> Objects.equals(accountVO.getAccountNo(), randomAccount)).
                    findFirst();

            // 중복되지 않는 계좌 번호 생성 완료
            if (!accountOptional.isPresent()) {
                break;
            }
        }

        // 5. 신규 계좌 생성 및 설정
        AccountVO accountVO = new AccountVO();
        accountVO.setOwner(user.getName());
        accountVO.setAccountNo(account);
        accountVO.setBalance(0);
        accountVO.setType(command.getAccountType());
        accountVO.setOpenDate(Date.valueOf(LocalDate.now()));

        // 계좌 리스트와 고객의 계좌 리스트에 추가
        this.accountList.add(accountVO);
        user.getAccounts().add(accountVO);

        // 6. CommandDTO에 응답 데이터 설정
        command.setResponseType(ResponseType.SUCCESS);
        command.setUserAccountNo(account);
        command.setName(user.getName());
        command.setId(user.getId());
        command.setPassword(user.getPassword());
        command.setAddress(user.getAddress());
        command.setPhone(user.getPhone());
        command.setAccountlist(this.accountList);

        // 7. 클라이언트에 응답 전송
        send(command);
    }

    //*******************************************************************
    // Name : deleteAccount()
    // Type : Method
    // Description : 클라이언트의 계좌 삭제 요청 처리
    //               - 전달받은 CommandDTO를 기반으로 고객 ID, 계좌 번호의 유효성 검사
    //               - 유효한 계좌를 고객 및 전체 계좌 리스트에서 삭제
    //               - 처리 결과를 클라이언트에 전송
    //*******************************************************************
    private synchronized void deleteAccount(CommandDTO commandDTO){
        String id = commandDTO.getId();
        String accountNo = commandDTO.getUserAccountNo();

        // 1. 고객 ID를 기반으로 고객 조회
        Optional<CustomerVO> userOptional= this.customerList.stream()
                .filter(customerVO -> Objects.equals(customerVO.getId(), id))
                .findFirst();

        if(userOptional.isPresent()){
            // 고객이 존재하는 경우
            CustomerVO user=userOptional.get();
            commandDTO.setName(user.getName());

            // 2. 계좌 번호를 기반으로 계좌 조회
            Optional<AccountVO> accountOptional= user.getAccounts().stream()
                    .filter(accountVO -> Objects.equals(accountVO.getAccountNo(), accountNo))
                    .findFirst();

            if (accountOptional.isPresent()){
                // 3. 계좌가 존재하는 경우 삭제 처리
                AccountVO accountVO=accountOptional.get();
                user.getAccounts().remove(accountVO);
                accountList.remove(accountVO);
                commandDTO.setResponseType(ResponseType.SUCCESS);
                System.out.println("[서버] 계좌를 삭제했습니다.");
            }
            else{
                // 계좌 번호가 존재하지 않는 경우
                System.out.println("[서버] 존재하지 않는 계좌입니다.");
                commandDTO.setResponseType(ResponseType.WRONG_ACCOUNT_NO);
            }
        }
        else{
            // 4. 고객이 존재하지 않는 경우
            System.out.println("[서버] 존재하지 않는 회원입니다.");
            commandDTO.setResponseType(ResponseType.WRONG_ID);
        }

        // 5. 클라이언트에 응답 전송
        send(commandDTO);
    }

    //*******************************************************************
    // Name : viewCustomer()
    // Type : Method
    // Description : 클라이언트의 고객 정보 조회 요청 처리
    //               - 전달받은 CommandDTO를 기반으로 고객 ID의 유효성 검사
    //               - 고객 정보가 존재하면 상세 정보를 응답으로 설정
    //               - 고객 정보가 없으면 실패 상태를 응답으로 설정
    //               - 처리 결과를 클라이언트에 전송
    //*******************************************************************
    private synchronized void viewCustomer(CommandDTO commandDTO) {
        // 1. 고객 ID를 기반으로 조회
        System.out.println("[서버] VIEW_CUSTOMER 요청 처리: ID=" + commandDTO.getId());
        Optional<CustomerVO> customer = this.customerList.stream()
                .filter(customerVO -> Objects.equals(customerVO.getId(), commandDTO.getId()))
                .findFirst();

        if (customer.isPresent()) {
            // 2. 고객 정보가 존재하는 경우 응답 데이터 설정
            CustomerVO customerVO = customer.get();
            System.out.println("[서버] 조회된 고객 정보: " + customerVO);

            commandDTO.setResponseType(ResponseType.SUCCESS);
            commandDTO.setName(customerVO.getName());
            commandDTO.setAddress(customerVO.getAddress());
            commandDTO.setPhone(customerVO.getPhone());
            commandDTO.setPassword(customerVO.getPassword());
            commandDTO.setAccountlist(customerVO.getAccounts());
        } else {
            // 3. 고객 정보가 존재하지 않는 경우 실패 응답 설정
            commandDTO.setResponseType(ResponseType.FAILURE);
            System.out.println("[서버] 고객 정보 없음: ID=" + commandDTO.getId());
        }

        // 4. 클라이언트로 응답 전송
        send(commandDTO);
    }

    //*******************************************************************
    // Name : viewAccount()
    // Type : Method
    // Description : 클라이언트의 계좌 정보 조회 요청 처리
    //               - 계좌번호를 기반으로 계좌 정보 조회
    //               - 계좌 정보가 존재하면 상세 정보를 응답으로 설정
    //               - 계좌 정보가 없으면 실패 상태를 응답으로 설정
    //               - 처리 결과를 클라이언트에 전송
    //*******************************************************************
    private synchronized void viewAccount(CommandDTO command) {
        // 1. 계좌번호를 기반으로 조회
        System.out.println("[서버] VIEW_ACCOUNT 요청 처리: 계좌번호=" + command.getUserAccountNo());
        // Optional로 감싸서 값이 없는 경우 처리
        Optional<AccountVO> optionalAccount = this.accountList.stream()
                .filter(accountVO -> Objects.equals(accountVO.getAccountNo(), command.getUserAccountNo()))
                .findFirst();

        if (optionalAccount.isPresent()) {
            // 2. 계좌 정보가 존재하는 경우 응답 데이터 설정
            AccountVO account = optionalAccount.get();
            System.out.println("[서버] 조회된 계좌 정보: " + account);

            command.setResponseType(ResponseType.SUCCESS);
            command.setBalance(account.getBalance());
            command.setUserAccountNo(account.getAccountNo());
            command.setName(account.getOwner());
            command.setAccountType(account.getType());
            command.setOpenDate(account.getOpenDate());
        } else {
            // 3. 계좌 정보가 존재하지 않는 경우 실패 응답 설정
            command.setResponseType(ResponseType.FAILURE);
            System.out.println("[서버] 계좌 정보 없음: 계좌번호=" + command.getUserAccountNo());
        }

        // 4. 클라이언트에 응답 전송
        send(command);
    }

    //*******************************************************************
    // Name : viewCustomerList()
    // Type : Method
    // Description : 클라이언트의 고객 리스트 조회 요청 처리
    //               - 고객 리스트의 존재 여부 확인
    //               - 고객 리스트가 존재하면 응답에 고객 정보를 포함하여 설정
    //               - 고객 리스트가 없으면 실패 상태를 응답으로 설정
    //               - 처리 결과를 클라이언트에 전송
    //*******************************************************************
    private synchronized void viewCustomerList() {
        System.out.println("[서버] 고객 리스트 요청 처리");
        CommandDTO command = new CommandDTO();

        // 1. 고객 리스트의 존재 여부 확인
        if (this.customerList != null) {
            // 2. 고객 리스트가 존재하는 경우
            System.out.println("[서버] 조회된 고객 : "+ this.customerList.size()+ " 명");
            command.setRequestType(RequestType.VIEW_CUSTOMER_LIST);
            command.setResponseType(ResponseType.SUCCESS);
            command.setCustomerList(this.customerList);
        } else {
            // 3. 고객 리스트가 없는 경우 실패 응답 설정
            command.setResponseType(ResponseType.FAILURE);
            System.out.println("[서버] 고객 정보 없음");
        }

        // 4. 클라이언트에 응답 전송
        send(command);
    }

    //*******************************************************************
    // Name : viewAccountList()
    // Type : Method
    // Description : 클라이언트의 계좌 리스트 조회 요청 처리
    //               - 계좌 리스트의 존재 여부 확인
    //               - 계좌 리스트가 존재하면 응답에 계좌 정보를 포함하여 설정
    //               - 계좌 리스트가 없으면 실패 상태를 응답으로 설정
    //               - 처리 결과를 클라이언트에 전송
    //*******************************************************************
    private synchronized void viewAccountList() {
        System.out.println("[서버] 계좌 목록 요청 처리");
        CommandDTO command = new CommandDTO();

        // 1. 계좌 리스트의 존재 여부 확인
        if (this.accountList != null) {
            // 2. 계좌 리스트가 존재하는 경우
            System.out.println("[서버] 조회된 고객 : "+ this.accountList.size()+ " 명");
            command.setRequestType(RequestType.VIEW_ACCOUNT_LIST);
            command.setResponseType(ResponseType.SUCCESS);
            command.setAccountlist(this.accountList);
        } else {
            // 3. 계좌 리스트가 없는 경우 실패 응답 설정
            command.setResponseType(ResponseType.FAILURE);
            System.out.println("[서버] 계좌 정보 없음");
        }

        // 4. 클라이언트에 응답 전송
        send(command);
    }

    //*******************************************************************
    // Name : managerlogin()
    // Type : Method
    // Description : 관리자 로그인 요청 처리
    //               - 전달받은 ID와 비밀번호를 기반으로 관리자 리스트에서 관리자 조회
    //               - 로그인 성공 시 성공 응답과 로그 메시지 출력
    //               - 로그인 실패 시 실패 응답 전송
    //               - 처리 결과를 클라이언트에 전송
    //*******************************************************************
    private synchronized void managerlogin(CommandDTO commandDTO){
        // 1. ID와 비밀번호를 기반으로 관리자 조회
        Optional<ManagerVO> manager = this.managerList.stream()
                .filter(ManagerVO -> Objects.equals(ManagerVO.getId(), commandDTO.getId())
                        && Objects.equals(ManagerVO.getPw(), commandDTO.getPassword()))
                .findFirst();

        if (manager.isPresent()) {
            // 2. 로그인 성공 처리
            commandDTO.setResponseType(ResponseType.SUCCESS);
            handler.displayInfo(manager.get().getId() + "님이 로그인하였습니다.");
        } else {
            // 3. 로그인 실패 처리
            commandDTO.setResponseType(ResponseType.FAILURE);
        }

        // 4. 클라이언트에 응답 전송
        send(commandDTO);
    }
}