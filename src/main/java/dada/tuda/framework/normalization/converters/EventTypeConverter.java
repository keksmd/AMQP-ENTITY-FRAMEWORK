package dada.tuda.framework.normalization.converters;

import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Converter(autoApply = true)
public class EventTypeConverter implements AttributeConverter<IMessagingEventType, String> {


    private final List<IMessagingEventType> eventTypes;

    @Override
    public String convertToDatabaseColumn(IMessagingEventType iMessagingEventType) {
        return iMessagingEventType.name();
    }

    @Override
    public IMessagingEventType convertToEntityAttribute(String s) {
        return eventTypes.stream().filter(type -> type.name().equals(s)).findFirst().orElseThrow(IllegalStateException::new);
    }
}
