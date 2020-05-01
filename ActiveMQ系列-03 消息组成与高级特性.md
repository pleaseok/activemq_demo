# ActiveMQ - 进阶学习
> 这一篇篇幅有点长，其实完全可以分两篇来写，只不过个人感觉这样更好知识归纳...

## JMS 消息组成
### JMS消息组成格式
整个JMS协议组成结构如下

| 结构 | 描述 |
|--------|--------|
|JMS Provider|消息中间件/消息服务器(这里就是指ActiveMQ了)|
|JMS Producer|消息生产者|
|JMS Consumer|消息消费者|
|JMS Message|消息(重要)|

其中JMS Message消息由三部分组成:
1. 消息头
2. 消息体
3. 消息属性

### 消息头
> 前面一篇我们有运行过实例，并打开页面控制台([http://127.0.0.1:8161](http://127.0.0.1:8161))有看到效果。点对点模式下选择菜单栏的Queues，发布订阅模式下选择Topics。但是我们并没有再点进去看详情了，其实详情就有我们的消息头信息。

如图:

![ActiveMQ](http://pic.code666.top/ActiveMQ13.png)
![ActiveMQ](http://pic.code666.top/ActiveMQ14.png)

但是这些头信息都是什么意思呢？让我们来通过一个表格来了解吧

| 名称 | 描述 |
|--------|--------|
|**Message ID**	|提供者发送的每一条信息的唯一标识，由提供者设置|
|**Destination**|消息的目的地，queue或者topic，由提供者设置|
|**Correlation ID**|通常用来链接响应与请求消息，由发送消息的JMS程序设置|
|Group|用于消息分组|
|Sequence|/|
|**Expiration**|消息的失效时间，由0表明消息不会过期，默认值为0|
|Persistence|持久化状态|
|**Priority**|消息的优先级，由提供者在发送过程中设置。优先级0的级别最低，优先级9的级别最高。0-4为普通消息，5-9为加急消息。默认值为4，但是ActiveMQ不保证优先级高就一定先发送，只是保证加急必须先于普通发送|
|Redelivered|消息的重发标志,false，代表该消息是第一次发生;true，代表该消息为重发消息|
|Reply To|请求程序用它来指出回复消息应发送的地方，由发送消息的JMS程序设置|
|Timestamp|提供者发送消息的时间(毫秒)，由提供者在发送过程中设置|
|Type|JMS程序用它来指出消息的类型|

不过，需要注意的是，上面大部分的值都是由JMS Provider来设置。只有以下几个值是可以由开发者设置的(使用setJMSXXX()方法)：
<font color="red">CorrelationID</font>
<font color="red">ReplyTo</font>
<font color="red">Type</font>

```java
TextMessage text = session.createTextMessage("test message...");
text.setJMSCorrelationID("10086"); // 有效
text.setJMSMessageID("10010"); //　有提供方法，但是运行无效
```
### 消息属性
> 除了Header里的那些属性外，我们还可以给消息设置自定义属性，对于实现消息的过滤功能还是非常有用的


```java
message.setStringProperty("property",property);

receive.getStringProperty("property");
```

### 消息体
> 消息体有TextMessage,MapMessage,ObjectMessage,BytesMessage,StreamMessage;前面的例子中我们有讲到TextMessage,其实都挺简单的，从名字就可以看出来每个消息体大概是做什么用的

**TextMessage**
==普通的字符串消息，前面好多例子都是用的这个==

```java
TextMessage textMessage = session.createTextMessage();
textMessage.setText("普通的字符串消息");
```

**MapMessage**
==一个map的消息类型，key是String 类型，值可以是各大基本类型或者Object类型==


**ObjectMessage**
==对象消息，包含一个可序列化的java对象==

**BytesMessage**
==二进制的数组消息，包含一个byte[]==

**StreamMessage**
==java数据流消息，用标准流操作来顺序的填充和读取==



## 消息持久化
> 持久化主要有两种：KahaDB日志消息存储和JDBC数据库消息存储。ActiveMq默认是KahaDB存储方式，当然也可以存储在内存中，但是这样就不能保证数据安全了。

生产者流程图
![生产者流程图](http://pic.code666.top/ActiveMQ15-producer.png)

消费者流程图
![消费者流程图](http://pic.code666.top/ActiveMQ16-consumer.png)

### KahaDB日志消息存储
* 存储位置：在ActiveMq的安装目录/data/KaHaDB目录

![消费者流程图](http://pic.code666.top/ActiveMQ17.png)

* SpringBoot配置

```yml
spring:
  jms:
    pub-sub-domain: true
    template:
      delivery-mode: persistent # 持久化，存储在KahaDB
#      delivery-mode: non_persistent # 非持久化，存储在内存中
```

### JDBC数据源消息存储
1. 配置application.yml文件

```yml
spring:
  jms:
    pub-sub-domain: true
    template:
      delivery-mode: persistent # 持久化
```

2. 修改activemq.xml(在conf目录下)

```xml
<!--配置数据库连接池-->
<bean name="mysql-ds" class="com.alibaba.druid.pool.DruidDataSource" destory-method="close">
	<property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <!--注意，确保有db_activemq这个数据库-->
    <property name="url" value="jdbc:mysql://127.0.0.1:3306/db_activemq"/>
    <property name="username" value="root"/>
    <property name="password" value="123456"/>
</bean>


<!--
	JDBC 用于master/slave模式的数据库分享
	更改persistenceAdapter标签
-->
<persistenceAdapter>
	<jdbcPersistenceAdapter dataSource="#mysql-ds"/>
</persistenceAdapter>
```
3. 拷贝相关jar包到activemq的lib目录下(这里拷贝mysql驱动和durid jar包)
4. 重启activemq


## 消息事务
> 消息事务，保证消息传递原子性的一个重要特性，和jdbc的事务特征类似。ActiveMQ的事务主要偏向在生产者的应用。

ActiveMQ消息事务流程图
![ActiveMQ消息事务流程图](http://pic.code666.top/ActiveMQ18.png)

### 生产者事务
> 不多BB,还是直接上代码吧(个人感觉有时候代码更直观)

测试类
```java
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ProducerApplication.class)
public class SpringBootProducer {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Value("${activemq.name}")
    private String name;

    /**
     * 没有加入事务
     */
    @Test
    public void testMessage(){
        for(int i=1;i<=10;i++){
            // 模拟异常
            if(i==4){
                int a = 10/0;
            }
            // 前三条会成功到达mq，后面会失败
            jmsMessagingTemplate.convertAndSend(name,"消息"+i);
        }
    }

    /**
     * 事务性发送(原生JMS API)
     */
    @Test
    public void sendMessageTx() {
        // 获取连接工厂
        ConnectionFactory connectionFactory = jmsMessagingTemplate.getConnectionFactory();

        Session session =null;
        try {
            // 创建连接
            Connection connection = connectionFactory.createConnection();
            session = connection.createSession(true,Session.AUTO_ACKNOWLEDGE);
            // 创建生产者
            MessageProducer producer = session.createProducer(session.createQueue(name));

            for (int i=1;i<=10;i++){
                TextMessage textMessage = session.createTextMessage("消息"+i);
                producer.send(textMessage);
            }

            // 注意：一旦开启事务发送，那么就必须使用commit方法进行事务提交，否则消息无法到达MQ服务器
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
            try {
                // 消息事务回滚
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Autowired
    private MessageService messageService;

    /**
     * 事务性发送(Spring的JmsTransactionManager功能)
     */
    @Test
    public void sendMessageTx2(){
        messageService.sendMessage();
    }
}
```

由于Spring的事务发送是必须写在业务方法，所以增加两个类
-- 配置类
```java
/**
 * ActiveMQ配置类
 */
@Configuration
public class ActiveMQConfig {
    /**
     * 添加JMS事务管理器
     */
    @Bean
    public PlatformTransactionManager createTransactionManager(ConnectionFactory connectionFactory){
        return new JmsTransactionManager(connectionFactory);
    }
}
```
-- 业务类
```java
/**
 * 消息发送的业务类
 */
@Service
public class MessageService {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    @Value("${activema.name}")
    private String name;

    @Transactional // 对消息发送加入事务管理(同时也对数据库的事务生效)
    public void sendMessage(){
        for(int i=1;i<=10;i++){
            // 模拟异常
            if(i==4){
                int a=10/0;
            }
            jmsMessagingTemplate.convertAndSend(name,"消息"+i);
        }
    }
}
```

### 消费者事务
```java
@Component // 放入IOC容器
public class MsgListener {
    /**
     * 接收TextMessage的方法
     */
    @JmsListener(destination = "${activemq.name}")
    public void receiveMessage(Message message, Session session){ // 参数加个Session
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
```

## 消费者消息确认机制
JMS消息只有在被确认之后，才认为已经被成功地消费了，消息的成功消费通常包含三个阶段：客户接收消息、客户处理消息和消息被确认。**在事务性会话中，当一个事务被提交的时候，确认自动发生。**在非事务性会话中，消息何时被确认取决于创建会话时的应答模式(acknowledgement mode)。该参数有以下三个可选值：

| 值 | 描述 |
|--------|--------|
| Session.AUTO_ACKNOWLEDGE | 当客户成功的从receive方法返回的时候，或者从MessageListener.onMessage方法成功返回的时候，会话自动确认客户收到的消息 |
| Session.CLIENT_ACKNOWLEDGE | 客户通过消息的acknowledge方法确认消息。需要注意的是，在这种模式中，确认是在会话层上进行：确认一个被消费的消息将自动确认所有已被会话消费的消息。列入，如果一个消息消费者消费了10个消息，然后确认第5个消息，那么所有10个消息都被确认
| Session.DUPS_ACKNOWLEDGE | 该选择只是会话迟钝确认消息的提交。如果JMS provider失败，那么可能会导致一些重复的消息。如果是重复的消息，那么JMS provider必须把消息头的JMSRedelivered字段设置为true

注意：消息确认机制与事务机制是冲突的，只能选其中一种，所以演示消息确认前，先关闭事务。
![ActiveMQ消息事务流程图](http://pic.code666.top/ActiveMQ19.png)

## 消息投递
异步，同步，延迟，定时

### 同步投递
消息生产者使用持久传递模式发送消息的时候，Producer.send()方法会被阻塞，知道broker发送一个确认消息给生产者，这个确认消息暗示broker已经成功接收到消息并把消息保存到二级存储中。


### 异步投递
如果应用程序能够容忍一些消息的丢失，那么可以使用异步发送。异步发送不会在收到broker的确认之前一直阻塞Producer.send方法。
想要使用异步，在brokerURL中增加jms.alwaysSyncSend=false&jms.useAsyncSend=true属性

1. 如果设置了alwaysSyncSend=true系统将会忽略useAsyncSend设置的值都采用同步。

2. 当alwaysSyncSend=false时，“NON_PERSISTENT”(非持久化)，事务中的消息将使用“异步发送”

3. 当alwaysSyncSend=false时，如果指定了useAsyncSend=true，“PERSISTENT”类型的消息使用异步发送。如果useAsyncSend=false，“PERSISTENT”类型的消息使用同步发送。

总结：默认情况（alwaysSyncSend=false，useAsyncSend=false），非持久化消息，事务内的消息均采用异步发送；对于持久化消息采用同步发送。

异步投递如何确认发送成功：
异步投递丢失消息的场景是：生产者设置UserAsyncSend=true，使用producer.send(msg)持续发送消息。
由于消息不阻塞，生产者会认为所有send的消息均被成功发送至MQ。如果MQ突然宕机，此时生产者端内存中尚未被发送至MQ的消息都会丢失。
这时，可以给异步投递方法接收回调，以确认消息是否发送成功。

### 延迟投递
生产者提供两个发送消息的方法，一个是即时发送消息，一个是延时发送消息。

延迟投递和定时投递的四个属性

| 属性名 | 类型 | 描述
|--------|--------|
| AMQ_SCHEDULED_DELAY | long | 延迟投递的时间
| AMQ_SCHEDULED_PERIOD | long | 重复投递的时间间隔
| AMQ_SCHEDULED_REPEAT | int | 重复投递次数
| AMQ_SCHEDULED_CRON | String | Cron表达式

首先修改activemq.xml文件里的属性，添加schedulerSupport=“true”配置

```xml
<broker xmlns="http://activemq.apache.org/schema/core"  brokerName="localhost" dataDirectory="${activemq. data}" schedulerSupport="true" />
```
之后在代码里设置延时时长
```java
message.setLongProperty(ScheduledMessage.AMO_SCHEDULED_DELAY, 10000);//10秒
```

### 定时投递
启动类上添加

```java
/**
 *　生产者启动类
 */
#SpringBootApplication
@EnableScheduling // 开启定时任务
public class ProducerApplication{
	public static void main(String[] args){SpringApplication.run(ConsumerApplication.class,args);}
}
```
业务上添加

```java
/**
 *　定时任务消息发送
 */
public class Producer{
	/**
 	 *　定时发送消息
 	 */
    @Scheduled(fixedDelay=3000)
    public void seandMessage(){}
}
```
## 死信队列
死信队列，用来保存处理失败或者过期的信息。

出现以下情况的时候，消息会被重发：

A transacted session is used and rollback() is called.

A transacted session is closed before commit is called.

A session is using CLIENT_ACKNOWLEDGE and Session.recover() is called.

当一个消息被重发超过6次（缺省为6），会给broker发送一个“poison ack”，这个消息被认为是a poison pill，这时broker会将这个消息发送到死信队列，以便后续处理。

注意两点：

1. 缺省持久消息过期，会被送到DLQ，非持久消息不会送到DLQ。

2. 缺省的死信队列是ActiveMQ.DLQ，如果没有特别指定，死信都会被发送到这个队列中。

可以通过配置文件activemq.xml来调整死信发送策略。
![ActiveMQ消息事务流程图](http://pic.code666.top/ActiveMQ20.png)

为每个队列建立独立得死信队列（下面是queue和topic两种形式，选一种即可）
![ActiveMQ消息事务流程图](http://pic.code666.top/ActiveMQ21.png)

还有其他策略，这个要根据实际情况来对应处理。
比如：
非持久消息保存到死信队列
```xml
<policyEntry queue=">">
    <deadLetterStrategy>
        <sharedDeadLetterStrategy processNonPersistent="true" />
    </deadLetterStrategy>
</policyEntry>    
```

过期消息不保存到死信队列
```xml
<policyEntry queue=">">
    <deadLetterStrategy>
        <sharedDeadLetterStrategy processExpired="false" />
    </deadLetterStrategy>
</policyEntry>
```

