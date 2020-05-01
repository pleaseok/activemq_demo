# ActiveMQ - 初步认识

## 消息中间件应用场景
* 异步处理
> 场景说明: 用户注册，需要执行三个业务逻辑，分别为写入用户表，发注册邮件以及注册短信

* 应用解耦
> 场景说明: 用户下单后，订单系统需要通知库存系统。

* 流量削峰
> 场景说明: 秒杀活动，一般会因为流量过大，导致流量暴增，应用挂掉。（在用户请求与秒杀业务处理中间加入消息队列）

## ActiveMQ介绍与JMS协议
### 简介
* 什么是ActiveMQ?
> ActiveMQ是遵守Apache开源规则的最流行，能力强劲的消息中间件。ActiveMQ是一个完全支持JMS1.1和J2EE1.4规范的JMS Provider实现。

* 什么是JMS?
> JMS即Java消息服务（Java Message Service）应用程序接口，是一个Java平台中关于面向消息中间件（MOM）的API，用于在两个应用程序之间，或分布式系统中发送消息，进行异步通信。Java消息服务是一个与具体平台无关的API，绝大多数MOM提供商都对JMS提供支持。

### JMS消息模式
> 消息中间件一般有两种传递模式:点对点模式(P2P)和发布－订阅模式(Pub/Sub)

#### 点对点模型(Queue队列模型)
> P2P: 即生产者和消费者之间的消息往来

![ActiveMQ](http://pic.code666.top/ActiveMQ01.png)
每个消息都被发送到特定的消息队列，接收者从队列中获取消息。队列保留着消息，直到他们被消费或超时

> P2P特点:

* 每个消息只有一个消费者(Consumer)，即一旦被消费，消息就不再在消息队列中
* 发送者和接收者之间在时间上没有依赖性，也就是说当发送者发送了消息之后，不管接收者有没有正在运行，它不会影响到消息被发送到队列
* 接收者在成功接收消息之后需向队列应答成功

#### 发布/订阅模型(Publish-Subscribe)
> 发布/订阅

包含三个角色: 主题(Topic),发布者(Publisher),订阅者(Subscriber),多个发布者将消息发送到topic，系统将这些消息投递到订阅此topic的订阅者。

![ActiveMQ](http://pic.code666.top/ActiveMQ02.png)

发布者发送到topic的消息，只有订阅了topic的订阅者才会收到消息。topic实现了发布和订阅，当你发布一个消息，所有订阅这个topic的服务都能得到这个消息，所以从1到N个订阅者都能得到这个消息的拷贝。

> 发布/订阅模型的特点:

* 每个消息可以有多个消费者
* 发布者和订阅者之间有时间上的依赖性(先订阅再发布)
* 订阅者必须保持运行的状态，才能接收发布者发布的消息


### JMS编程API
| 要素 | 作用 |
|--------|--------|
|Destination|表示消息所走通道的目标定义，用来定义消息从发送端发出后要走的通道，而不是接收方。Destination属于类对象|
|ConnectionFactory|用于创建连接对象，ConnectionFactory属于管理类的对象|
|Connection|连接接口，所负责的重要工作时创建Session|
|Session|会话接口，这是一个非常重要的对象，消息发送者、消息接收者以及消息对象本省，都是通过这个会话对象创建的|
|MessageConsume|消息消费者，也就是订阅消息并处理消息的对象|
|MessageProducer|消息的生产者，也就是用来发送消息的对象|

1. **ConnectionFactory**
创建Connection对象的工厂，针对两种不同的jms消息模型，分别有QueueConnectionFactory和TopicConnectionFactory两种。

2. **Destination**
Destination的意思是消息生产者的消息发送目标或者说消息消费者的消息来源。对于消息生产者来说，它的Destination是某个队列(Queue)或某个主题(Topic);对于消息消费者来说，它的Destination也是某个队列或主题(即消息来源)。所以，Destination实际上就是两种类型的对象:Queue、Topic

3. **Connection**
Connection表示在客户端和JMS系统之间建立的连接(对TCP/IP socket的包装)。Connection可以产生一个或多个Session。

4. **Session**
Session是我们对消息进行操作的接口，可以通过Session创建生产者、消费者、消息等。Session提供了事务的功能，如果需要使用session发送/接收多个消息时，可以将这些发送/接收动作放到一个事务中。

5. **Producer**
消息生产者由Session创建，并用于将消息发送到Destination。同样，消息生产者分两种类型: QueueSender和TopicPublisher。可以调用消息生产者的方法(send或publish方法)发送消息

6. **Consumer**
消息消费者由Session创建，用于接收被发送到Destination的消息。两张类型: QueueReceiver和TopicSubscriber。可分别通过session的createReceiver(Queue)或createSubscriber(Topic)来创建。当然，也可以session的createDurableSubscriber方法来创建持久化的订阅者。

7. **MessageListener**
消息监听器。如果注册了消息监听器，一旦消息到达，将自动调用监听器的onMessage方法。EJB中的MDB(Message-Driven Bean)就是一种MessageListener。

![ActiveMQ](http://pic.code666.top/ActiveMQ03.png)


### ActiveMQ安装
> 安装(Linux)

```
第一步：　安装JDK(略)

第二步：　下载activemq的压缩包(apache-activemq-5.15.12-bin.tar.gz)到Linux系统

第三步：　解压文件
tar -zxvf apache-activemq-5.15.12-bin.tar.gz

第四步：　进入apache-activemq-5.15.12的bin目录

cd apache-activemq-5.15.12/bin

第五步：　启动activemq
./activemq start   (执行2次：第一次生产配置文件；第二次启动)

第六步：　停止activemq
./activemq stop

其它命令：
./activemq status -- 查看activemq的状态
./activemq restart -- 重启activemq
./activemq purge FOO.BAR -- 删除队列中的所有消息，队列名称是FOO.BAR
./activemq dstat -- 显示默认broker的所有主题和队列统计信息
./activemq dstat topics -- 显示主题的统计信息
./activemq dstat queue -- 显示队列的统计信息
...
```

> 访问

[http://127.0.0.1:8161](http://127.0.0.1:8161)

```
页面控制台：　http://ip:8161 （监控）
请求地址：　tcp://ip:61616　　（java代码访问消息中间件）

初始用户名和密码: admin/admin
```

## 原生JMS开发
### 点对点模式
##### 生产者
1. Maven引入依赖
```
<dependencies>
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-all</artifactId>
            <version>5.15.11</version>
        </dependency>
    </dependencies>
```

2. 编写生产消息的类(PTP_Producer.class)
```
public class PTP_Producer {
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
        Queue queue = session.createQueue("queue01");
        //6.创建消息生产者
        MessageProducer producer=session.createProducer(queue);
        //7.创建消息
        TextMessage message=session.createTextMessage("hello,this is PTP message");
        //8.发送消息
        producer.send(message);
        System.out.println("生产者发送完毕...");
        //9.释放资源
        session.close();
        connection.close();
    }
}
```

3. 运行效果
![ActiveMQ](http://pic.code666.top/ActiveMQ04.png)
![ActiveMQ](http://pic.code666.top/ActiveMQ05.png)



##### 消费者
1. Maven引入依赖
如上,略

2. 编写接收消息的类(PTP_Consumer.class) -- receive方法
```
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
            Message message = consumer.receive(); // 不断的接收，还有一个方法receive(long l)，这个是隔多少毫秒接收一次
            if(message == null){ // 表示没有信息了，退出循环
                break;
            }

            if(message instanceof TextMessage){
                TextMessage textMessage = (TextMessage) message;
                System.out.println("接受到的消息: "+textMessage.getText());
            }
        }
    }
}
```

3. 编写接收消息的类(PTP_Consumer2.class) -- 监听器方法(常用)
```
public class PTP_Consumer2 {
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
        //7.设置消息监听器来接收消息
        consumer.setMessageListener(new MessageListener() {
            // 处理消息
            @Override
            public void onMessage(Message message) {
                if(message instanceof TextMessage){
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        System.out.println("接收的消息(2):"+textMessage.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // 注意: 在监听器模式下千万不要关闭连接，一旦关闭，消息无法接收
    }
}
```

4. 运行效果
![ActiveMQ](http://pic.code666.top/ActiveMQ06.png)
![ActiveMQ](http://pic.code666.top/ActiveMQ07.png)

### 发布订阅模式
##### 生产者
1. Maven引入依赖
如上,略

2. 编写生产类(PS_Producer.class)
```
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
```

3. 运行效果
![ActiveMQ](http://pic.code666.top/ActiveMQ08.png)
![ActiveMQ](http://pic.code666.top/ActiveMQ09.png)


##### 消费者
1. Maven引入依赖
如上,略

2. 编写生产类(PS_Consumer.class)
```
/*
 * 发布订阅模式消费者
 */
public class PS_Consumer {
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
        Topic topic = session.createTopic("topic01");
        //6.创建消息消费者
        MessageConsumer consumer = session.createConsumer(topic);
        //7.设置消息监听器来接收消息
        consumer.setMessageListener(new MessageListener() {
            // 处理消息
            @Override
            public void onMessage(Message message) {
                if(message instanceof TextMessage){
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        System.out.println("接收的消息---topic:"+textMessage.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // 注意: 在监听器模式下千万不要关闭连接，一旦关闭，消息无法接收
    }
}
```

3. 运行效果)
![ActiveMQ](http://pic.code666.top/ActiveMQ10.png)

这时我们看到订阅到的topic消息是没有被消费的。上面有说到，在发布订阅模式下，一定要先启动消费者，然后才能消费到发布者推送的订阅的信息。让我们重新启动下PS_Producer类，再看看效果
![ActiveMQ](http://pic.code666.top/ActiveMQ11.png)

这时已经成功获取到消息了，再看看页面控制台
![ActiveMQ](http://pic.code666.top/ActiveMQ12.png)
消息入列2条，成功出列1条，1个消费者
