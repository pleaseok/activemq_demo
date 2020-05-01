package top.code666.consumer.listener;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

@Component // 放入IOC容器
public class MsgListener {
    /**
     * 接收TextMessage的方法
     */
    @JmsListener(destination = "${activemq.name}")
    public void receiveMessage(Message message, Session session){
        if(message instanceof TextMessage){
            TextMessage textMessage = (TextMessage) message;
            try {
                System.out.println("接收消息:"+textMessage.getText());
                session.commit();
            } catch (JMSException e) {
                e.printStackTrace();
                try {
                    session.commit();// 一旦事务回滚，MQ会重发消息，一共重发6次
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
