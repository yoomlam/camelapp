/**
 *
 */
package net.camelapp;

import com.google.common.collect.Sets;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.seda.SedaComponent;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.converter.CoreTypeConverterRegistry;
import org.apache.camel.spi.TypeConverterRegistry;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.camelapp.models.Claim;
import net.camelapp.models.Payload;

import java.util.Set;

@Configuration
public class AppConfig {
    public final static String SEDA_ASYNC_OPTION = "waitForTaskToComplete=Never";

    @Value("${net.camelapp.context_path}")
    public String contextPath;
    public static String servletName = "UniqCamelServletName";

    @Autowired
    private CamelContext camelContext;

    @Bean
    CamelContextConfiguration contextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext context) {
                SedaComponent sedaComponent = context.getComponent("seda", SedaComponent.class);
                sedaComponent.setDefaultBlockWhenFull(true);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                System.err.println("=====================================");
                registerTypeConverter(camelContext);
            }
        };
    }

    public static Set<Class> DTO_CLASSES = Sets.newHashSet(Claim.class, Payload.class);

    private void registerTypeConverter(CamelContext camelContext) {
        // Define the behaviour if the TypeConverter already exists
        new DtoConverter(DTO_CLASSES).registerWith(camelContext);

        TypeConverterRegistry registry = camelContext.getTypeConverterRegistry();
        ((CoreTypeConverterRegistry) registry).getTypeMappings().forEach((fromClass, toClass, converter) -> {
//            System.err.println(fromClass.getName()+" -> "+toClass.getName()+" : "+converter.getClass());
        });
//        System.err.println("\n+++++++ " + registry.lookup(Claim.class, byte[].class));
//        System.err.println("\n+++++++ " + registry.lookup(byte[].class, Claim.class));
    }

//    @Bean
//    ConnectionFactory rabbitConnectionFactory(){
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost");
//        factory.setPort(5672);
//        factory.setUsername("guest");
//        factory.setPassword("guest");
//        return factory;
//    }

    // https://opensource.com/article/18/9/camel-rest-dsl
    // https://stackoverflow.com/questions/55127006/multiple-servlets-with-camel-servlet-possible
    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean
                (new CamelHttpTransportServlet(), contextPath + "/*");
        servlet.setName(servletName);
        System.out.println(servlet);
        return servlet;
    }

    @Bean
    ProducerTemplate producerTemplate() {
        return camelContext.createProducerTemplate();
    }

    // @Bean
    // ConsumerTemplate consumerTemplate() {
    //   return camelContext.createConsumerTemplate();
    // }

}
