package top.code666.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

@Component
public class MsgListener {

    @JmsListener(destination = "${activemq.name}")
    public void receive(Message message){
        if (message instanceof TextMessage){
            TextMessage textMessage = (TextMessage) message;
            try {
                System.out.println("接收消息: "+textMessage.getText());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        MapMessage mapMessage = (MapMessage) message;

    }
}
