package dada.tuda.framework.normalization.types.realizations;

import dada.tuda.framework.crud.MessagingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@MessagingEntity(domain = "cancelled")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelPayload {
    private String reason;
}
