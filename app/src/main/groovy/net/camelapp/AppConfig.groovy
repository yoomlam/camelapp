package net.camelapp

import com.google.common.collect.Sets
import groovy.util.logging.Slf4j
import net.camelapp.models.Claim
import net.camelapp.models.Payload
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.seda.SedaComponent
import org.apache.camel.component.servlet.CamelHttpTransportServlet
import org.apache.camel.impl.converter.CoreTypeConverterRegistry
import org.apache.camel.spi.TypeConverterRegistry
import org.apache.camel.spring.boot.CamelContextConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@Slf4j
class AppConfig {
    public final static String SEDA_ASYNC_OPTION = 'waitForTaskToComplete=Never'

    @Value('${net.camelapp.context_path}')
    String contextPath
    public final static String servletName = 'UniqCamelServletName'

    @Autowired
    private CamelContext camelContext

    @Bean
    CamelContextConfiguration contextConfiguration() {
        new CamelContextConfiguration() {
            @Override
            void beforeApplicationStart(CamelContext context) {
                SedaComponent sedaComponent = context.getComponent("seda", SedaComponent)
                sedaComponent.setDefaultBlockWhenFull(true)
            }

            @Override
            void afterApplicationStart(CamelContext camelContext) {
                System.err.println('=====================================')
                registerTypeConverter(camelContext)
            }
        }
    }

    static Set<Class> DTO_CLASSES = Sets.newHashSet(Claim, Payload)

    private void registerTypeConverter(CamelContext camelContext) {
        // Define the behaviour if the TypeConverter already exists
        new DtoConverter(DTO_CLASSES).registerWith(camelContext)

        TypeConverterRegistry registry = camelContext.getTypeConverterRegistry()
        ((CoreTypeConverterRegistry) registry).getTypeMappings().forEach((fromClass, toClass, converter) -> {
//            System.err.println(fromClass.getName()+" -> "+toClass.getName()+" : "+converter.getClass())
        })
//        System.err.println("\n+++++++ " + registry.lookup(Claim, byte[]))
//        System.err.println("\n+++++++ " + registry.lookup(byte[], Claim))
    }

//    @Bean
//    ConnectionFactory rabbitConnectionFactory(){
//        ConnectionFactory factory = new ConnectionFactory()
//        factory.setHost("localhost")
//        factory.setPort(5672)
//        factory.setUsername("guest")
//        factory.setPassword("guest")
//        factory
//    }

    // https://opensource.com/article/18/9/camel-rest-dsl
    // https://stackoverflow.com/questions/55127006/multiple-servlets-with-camel-servlet-possible
    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(), "${contextPath}/*")
        servlet.setName(servletName)
        log.info("servlet: ${servlet}")
        servlet
    }

    @Bean
    ProducerTemplate producerTemplate() {
        camelContext.createProducerTemplate()
    }

    // @Bean
    // ConsumerTemplate consumerTemplate() {
    //   camelContext.createConsumerTemplate()
    // }

}
