package top.code666.producer;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * 发布订阅模式-消息生产者
 */
public class PS_Producer {
    public static void main(String[] args) throws JMSException {
        //1.创建连接工厂
        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
        //2.创建连接
        Connection connection = factory.createConnection();
        //3.打开连接
        connection.start();
        //4.创建session
        /**
         * 参数一:是否开启事务
         * 参数二:消息确认机制
         */
        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        //5.创建目标地址(Queue:点对点消息，Topic:发布订阅消息)
        Topic topic = session.createTopic("topic01");
        //6.创建消息生产者
        MessageProducer producer=session.createProducer(topic);
        //7.创建消息
        TextMessage message=session.createTextMessage("hello,this is PS message");
        //8.发送消息
        producer.send(message);
        System.out.println("生产者发送完毕...");
        //9.释放资源
        session.close();
        connection.close();
    }
}
