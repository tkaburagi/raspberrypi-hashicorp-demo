package am.ik.demo.facebootifier;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SlackClient {

    private SlackSession session;

    public SlackClient() {
        try {
            this.session = SlackSessionFactory.createWebSocketSlackSession("");
            session.connect();
        } catch (IOException e) {
        }
    }

    public void sendMessage(byte[] file, String filename) {
        session.sendFileToUser("kabu", file, filename);
    }
}