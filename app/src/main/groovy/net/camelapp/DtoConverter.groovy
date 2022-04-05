package net.camelapp

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import groovy.util.logging.Slf4j
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.TypeConversionException
import org.apache.camel.spi.TypeConverterRegistry
import org.apache.camel.support.TypeConverterSupport

// Data Transfer Object Converter
// needed for sending POJOs through activemq, rabbitmq, etc.
@Slf4j
class DtoConverter extends TypeConverterSupport {

    final Set<Class> dtoClasses

    DtoConverter(dataTransferObjectClasses) {
        dtoClasses = dataTransferObjectClasses
    }

    TypeConverterRegistry registerWith(CamelContext camelContext) {
        TypeConverterRegistry registry = camelContext.getTypeConverterRegistry()
        // registry.setTypeConverterExists(TypeConverterExists.Override)
        dtoClasses.forEach(clazz -> {
            registry.addTypeConverter(clazz, byte[], this)
            registry.addTypeConverter(byte[], clazz, this)
        })
        registry
    }

    @Override
    <T> T convertTo(Class<T> targetClass, Exchange exchange, Object value) throws TypeConversionException {
        try {
//            log.info("targetClass: " + targetClass + " value: " + value.getClass())
            if (dtoClasses.contains(value.getClass()) && targetClass == byte[]) {
                return (T) toByteArray(value)
            } else if (value.getClass() == byte[]) {
                return toPojo(targetClass, (byte[]) value)
            }
        } catch (IOException e) {
            throw new TypeConversionException(value, targetClass, e)
        }
        null
    }

    static final ObjectMapper mapper = new ObjectMapper()
    static final ObjectWriter writer = mapper.writer()
    static final ObjectReader reader = mapper.reader()

    static byte[] toByteArray(Object obj) throws JsonProcessingException {
        log.info('convert toByteArray: ' + writer.writeValueAsString(obj))
        writer.writeValueAsBytes(obj)
    }

    static <T> T toPojo(Class<T> targetClass, byte[] bytes) throws IOException {
        log.info('convert toPojo: ' + targetClass)
        reader.readValue(new String(bytes), targetClass)
    }
}
