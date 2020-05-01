package top.code666.comsumer;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class PTP_Consumer {
    public static void main(String[] args) throws JMSException {
        //1.创建连接工厂
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
        //2.创建连接
        Connection connection = factory.createConnection();
        //3.打开连接
        connection.start();
        //4.创建session
        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        //5.指定目标地址
        Queue queue = session.createQueue("queue01");
        //6.创建消息消费者
        MessageConsumer consumer = session.createConsumer(queue);
        //7.接受消息
        while (true){
            Message message = consumer.receive();
            if(message == null){
                break;
            }

            if(message instanceof TextMessage){
                TextMessage textMessage = (TextMessage) message;
                System.out.println("接受到的消息: "+textMessage.getText());
            }
        }
    }
}
