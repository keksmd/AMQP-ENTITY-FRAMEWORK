package dada.tuda.framework.consistency;

public class DefaultIdempotencyProvider implements IdempotencyProvider {
    @Override
    public boolean eventProcessed(String operationId) {
        return false;
    }

    @Override
    public void storeEventAsProcessed(String s) {
    }
}
