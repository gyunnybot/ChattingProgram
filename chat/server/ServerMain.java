package chat.server;

import java.io.IOException;

public class ServerMain {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        SessionManager sessionManager = new SessionManager();

        //점진적으로 변경 예정
        //CommandManager commandManagerV1 = new CommandManagerV1(sessionManager);
        //CommandManager commandManagerV2 = new CommandManagerV2(sessionManager);
        //CommandManager commandManagerV3 = new CommandManagerV3(sessionManager);
        CommandManager commandManagerV4 = new CommandManagerV4(sessionManager);
        
        Server server = new Server(PORT, commandManagerV1, sessionManager);
        server.start();
    }
}
