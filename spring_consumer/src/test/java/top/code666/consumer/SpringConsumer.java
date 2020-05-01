package top.code666.consumer;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

import java.io.IOException;

/*
* 用来启动消费方监听
*/
public class SpringConsumer {
    public static void main(String[] args) throws IOException {
        //1. 加载spring配置
        ClassPathXmlApplicationContext cxt
                = new ClassPathXmlApplicationContext("classpath:applicationContext-consumer.xml");
        //2. 启动
        cxt.start();
        //3. 阻塞方法,让程序一直处于等待状态
        System.in.read();
    }
}
