package dada.tuda.framework.normalization.types.realizations;

import dada.tuda.framework.crud.MessagingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@MessagingEntity(domain = CancelPayload.CANCEL_DOMAIN, createDefaultBindings = "false", ttl = "0")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelPayload {
    public static final String CANCEL_DOMAIN = "cancelled";
    private String reason;
}
