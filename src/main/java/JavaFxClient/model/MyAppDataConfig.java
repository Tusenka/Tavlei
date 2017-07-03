package JavaFxClient.model;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Irina
 */
public class MyAppDataConfig {
    public static ApplicationContext getContext() {
        return context;
    }
    static private final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/app-context.xml");

}
