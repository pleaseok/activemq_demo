# ActiveMQ - Spring&SpringBoot整合
> 前面一篇讲了消息中间件的一些基本概念、JMS协议还列举了原生JMS模式下的开发，这一篇主要讲spring和springboot框架下的开发，比原生模式下还是节省了很多开发时间的

### Spring+ActiveMQ
> spring与ActiveMQ的整合，有Spring基础的应该知道，除了添加相关依赖外，肯定少不了要写xml的配置文件

**1. 添加相关依赖**
```xml
<!--这里除了spring框架的基础依赖外还添加了activemq相关依赖与junit单元测试的依赖-->
    <dependencies>
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-all</artifactId>
            <version>5.11.2</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-oxm</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aop</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>5.0.2.RELEASE</version>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jms</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>javax.jms</groupId>
            <artifactId>javax.jms-api</artifactId>
            <version>2.0.1</version>
        </dependency>
        
        <dependency>
            <groupId>org.apache.xbean</groupId>
            <artifactId>xbean-spring</artifactId>
            <version>3.7</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
    </dependencies>
```


**2. 编写spring整合activemq的配置文件**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:amp="http://activemq.apache.org/schema/core"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd">

    <!--1. 创建连接工厂对象-->
    <amp:connectionFactory
        id="connectionFactory"
        brokerURL="tcp://127.0.0.1:61616"
        userName="admin"
        password="admin"
    />
    <!--2. 创建缓存连接工厂-->
    <bean id="cachingConnectionFactory" class="org.springframework.jms.connection.CachingConnectionFactory">
        <!--注入连接工厂-->
        <property name="targetConnectionFactory" ref="connectionFactory"/>
        <!--缓存消息数据-->
        <property name="sessionCacheSize" value="5"/>
    </bean>
    <!--3. 创建用于点对点发送的JmsTemplate-->
    <bean id="jmsQueueTemplate" class="org.springframework.jms.core.JmsTemplate">
        <!--注入缓存连接工厂-->
        <property name="connectionFactory" ref="cachingConnectionFactory"/>
        <!--指定是否为发布订阅模式-->
        <property name="pubSubDomain" value="false"/>
    </bean>
    <!--4. 创建用于发布订阅发送的JmsTemplate-->
    <bean id="jmsTopicTemplate" class="org.springframework.jms.core.JmsTemplate">
        <!--注入缓存连接工厂-->
        <property name="connectionFactory" ref="cachingConnectionFactory"/>
        <!--指定是否为发布订阅模式-->
        <property name="pubSubDomain" value="true"/>
    </bean>
</beans>
```


**3. 完成上面两步后我们就可以正式工作了**
这里我把两个模式的生产者写在一个类里
```java
/*
* 演示Spring与ActiveMQ整合
*/
@RunWith(SpringJUnit4ClassRunner.class) // junit与spring整合
@ContextConfiguration("classpath:applicationContext-producer.xml") // 加载spring配置
public class SpringProducer {
    // 点对点模式的模板对象
    @Autowired
    @Qualifier("jmsQueueTemplate")
    private JmsTemplate jmsQueueTemplate;

    // 发布订阅模式
    @Autowired
    @Qualifier("jmsTopicTemplate")
    private JmsTemplate jmsTopicTemplate;

    /*
     * 点对点发送
     */
    @Test
    public void ptpSender(){
        /*
        * 参数一：指定队列的名称
        * 参数二：MessageCreator接口,我们需要提供该接口的匿名内部实现
        */
        jmsQueueTemplate.send("spring_queue", new MessageCreator() {
            // 我们只需要返回发送的消息内容即可
            @Override
            public Message createMessage(Session session) throws JMSException {
                // 创建文本消息
                TextMessage textMessage = session.createTextMessage("spring test message");
                return textMessage;
            }
        });
        System.out.println("消息发送已完成");
    }

    /*
     * 发布订阅发送
     */
    @Test
    public void psSender(){
        jmsTopicTemplate.send("spring_topic", new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                // 创建文本消息
                TextMessage textMessage = session.createTextMessage("spring test message--topic");
                return textMessage;
            }
        });
        System.out.println("消息发送已完成");
    }
}
```
消费者: 用监听器的方式实现
```java
/*
* 点对点
*/
@Component // 放入SpringIOC容器,名称queueListener
public class QueueListener implements MessageListener {

    // 用于接收消息
    @Override
    public void onMessage(Message message) {
        if(message instanceof TextMessage){
            TextMessage textMessage= (TextMessage) message;
            try {
                System.out.println("queue接口消息: "+textMessage.getText());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
```


### SpringBoot+ActiveMQ
> 上面讲了spring模式下的整合，其实springboot比这个更简单(springboot设计之初本来就是更少的配置文件，所以肯定会更简单)

**1. 添加相关依赖**
```xml
	<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.1.RELEASE</version>
        <relativePath/>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-activemq</artifactId>
        </dependency>
    </dependencies>
```


**2. 编写springboot的配置文件(application.yml)**
```yml
server:
  port: 9091 # 服务启动端口
spring:
  application:
    name: activemq-demo # 服务名称，如果用于微服务架构来说要用到
# springboot与activemq的整合
  activemq:
    broker-url: tcp://127.0.0.1:61616
    user: admin
    password: admin
# 指定发送模式(点对点:false,发布订阅:true)
  jms:
    pub-sub-domain: true
# 自定义属性
activemq:
  name: springboot_topic
#  name: springboot_queue
```

**3. 编写相关类**
生产者:
```java
/**
 * 演示springboot与activemq整合
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ProducerApplication.class)
public class SpringbootProducer {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    @Value("${activemq.name}")
    private String name;

    @Test
    public void ptpSender(){
        jmsMessagingTemplate.convertAndSend(name,
                "this is springboot message");
    }
}
```
消费者:
```java
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
    }
}
```
好了，完成！没有很多的文字叙述，个人感觉还是代码更直观！就不展示运行效果了吧，有兴趣的请自己打开idea试试吧~