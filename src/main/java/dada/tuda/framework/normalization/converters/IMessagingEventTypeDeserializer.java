package dada.tuda.framework.normalization.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import dada.tuda.framework.normalization.types.interfaces.IMessagingEventType;
import dada.tuda.framework.normalization.types.realizations.CancelUtils;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
public class IMessagingEventTypeDeserializer extends JsonDeserializer<IMessagingEventType> {

    public static final List<IMessagingEventType> MESSAGING_EVENT_TYPES = new ArrayList<>(List.of(CancelUtils.CANCELLING_EVENT_TYPE));

    @Override
    public IMessagingEventType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.readValueAsTree().toString().replace("\"", "");
        return MESSAGING_EVENT_TYPES.stream().filter(e -> e.name().equals(text)).findFirst().orElseThrow(IllegalStateException::new);
    }

}

