package am.ik.demo.facebootifier;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class SlackClient {
    private SlackSession session;

    public SlackClient() throws Exception {
        this.session = SlackSessionFactory.createWebSocketSlackSession("");
        session.connect();
    }

    public void sendMessage(byte[] file, String filename) {
        session.sendFileToUser("kabu", file, filename);
    }
}