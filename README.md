# JAVA 콘솔 채팅 프로그램 💬

---

## 읽기/쓰기 블로킹 문제
프로그램을 사용하는 클라이언트는 채팅 메세지를 읽음과 동시에 쓸 수 있어야 합니다.

ReadHandler, WriteHandler를 따로 구현 후 멀티스레드 방식으로 클라이언트 코드를 설계했습니다.

```java
public void start() throws IOException {
    log("클라이언트 시작");
    socket = new Socket(host, port);
    input = new DataInputStream(socket.getInputStream());
    output = new DataOutputStream(socket.getOutputStream());
        
    readHandler = new ReadHandler(input, this);
    writeHandler = new WriteHandler(output, this);

    Thread readThread = new Thread(readHandler, "readHandler");
    Thread writeThread = new Thread(writeHandler, "writeHandler");

    readThread.start();
    writeThread.start();
}
```

## 클라이언트-서버 연결 관리와 자원 정리
특정 사용자가 보내는 메세지는 채팅 프로그램에 접속해 있는 모든 사용자에게 전송되어야 합니다.

클라이언트 연결 요청이 들어올 때마다 서버 소켓은 세션(Session)을 활용해 소켓을 생성하고, 해당 세션은 세션 매니저(SessionManager)가 관리하도록 설계하였습니다.

세션 매니저 내 sendAll()을 통해 전체 사용자가 메세지를 받을 수 있습니다.

```java
public synchronized void sendAll(String message) { //전체 공지 보내기
    for (Session session : sessions) {
        try {
            session.send(message);
        } catch (IOException e) {
            log(e);
        }
    }
}
```

자원 정리 또한 세션 매니저가 관리합니다. ShutdownHook에 등록하여 프로그램 종료 시 일괄 처리됩니다.

```java
public synchronized void closeAll() { //전체 자원 정리
    for (Session session : sessions) {
        session.close();
    }

    sessions.clear();
}
```

## 커맨드 패턴(Command Pattern)을 활용한 채팅 기능 구현
클라이언트가 사용할 수 있는 각각의 기능을 하나의 Command로 보고 인터페이스와 구현체를 작성했습니다.

주요 명령어와 그에 맞는 기능을 구현해 key-value 쌍으로 입력한 후 특정 key가 입력되면 명령을 실행합니다.

사전에 입력되지 않은 key는 getOrDefault를 통해 디폴트로 선언한 명령을 수행합니다.

```java
public class CommandManagerV4 implements CommandManager {
    public static final String DELIMITER = "!";
    private final Map<String, Command> commands = new HashMap<>();
    private final Command defaultCommand = new DefaultCommand();

    public CommandManagerV4(SessionManager sessionManager) {
        commands.put("/join", new JoinCommand(sessionManager));
        commands.put("/message", new MessageCommand(sessionManager));
        commands.put("/change", new ChangeCommand(sessionManager));
        commands.put("/users", new UsersCommand(sessionManager));
        commands.put("/exit", new ExitCommand());
    }

    @Override
    public void execute(String totalMessage, Session session) throws IOException {
        String[] args = totalMessage.split(DELIMITER);
        String key = args[0];
        
        Command command = commands.getOrDefault(key, defaultCommand);
        command.execute(args, session);
    }
}
```