package top.code666;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
