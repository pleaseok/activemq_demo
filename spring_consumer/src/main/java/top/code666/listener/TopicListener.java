package top.code666.listener;

import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/*
* 发布订阅
*/
@Component // 放入SpringIOC容器,名称topicListener
public class TopicListener implements MessageListener {

    // 用于接收消息
    @Override
    public void onMessage(Message message) {
        if(message instanceof TextMessage){
            TextMessage textMessage= (TextMessage) message;
            try {
                System.out.println("topic接口消息: "+textMessage.getText());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
