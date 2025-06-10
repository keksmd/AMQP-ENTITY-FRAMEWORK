package dada.tuda.framework.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class QueueAnnotationParser {


    private final Environment environment;

    public org.springframework.amqp.core.Queue parseQueue(Queue queueAnnotation) {
        String name = resolve(queueAnnotation.name());
        if (name.isEmpty()) {
            name = resolve(queueAnnotation.value());
        }

        // Resolve booleans
        boolean durable = parseBoolean(resolve(queueAnnotation.durable()), !name.isEmpty()); // default = true if name is given
        boolean exclusive = parseBoolean(resolve(queueAnnotation.exclusive()), false);
        boolean autoDelete = parseBoolean(resolve(queueAnnotation.autoDelete()), false);

        // Parse arguments
        Map<String, Object> args = parseArguments(queueAnnotation.arguments());

        return new org.springframework.amqp.core.Queue(name, durable, exclusive, autoDelete, args);
    }

    private String resolve(String value) {
        if (this.environment != null) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }

    private boolean parseBoolean(String value, boolean defaultValue) {
        return (value == null || value.isEmpty()) ? defaultValue : Boolean.parseBoolean(value);
    }

    private Map<String, Object> parseArguments(org.springframework.amqp.rabbit.annotation.Argument[] arguments) {
        Map<String, Object> map = new HashMap<>();
        for (org.springframework.amqp.rabbit.annotation.Argument arg : arguments) {
            String key = resolve(arg.name());
            String val = resolve(arg.value());
            String type = resolve(arg.type());

            Object parsedValue = parseValue(type, val);
            map.put(key, parsedValue);
        }
        return map;
    }

    private Object parseValue(String type, String value) {
        if ("java.lang.String".equals(type)) {
            return value;
        } else if ("java.lang.Integer".equals(type) || "int".equals(type)) {
            return Integer.parseInt(value);
        } else if ("java.lang.Boolean".equals(type) || "boolean".equals(type)) {
            return Boolean.parseBoolean(value);
        } else if ("java.lang.Long".equals(type) || "long".equals(type)) {
            return Long.parseLong(value);
        } else if ("java.lang.Double".equals(type) || "double".equals(type)) {
            return Double.parseDouble(value);
        } else {
            return value;
        }
    }
}
