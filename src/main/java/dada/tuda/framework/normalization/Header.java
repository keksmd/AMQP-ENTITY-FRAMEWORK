package dada.tuda.framework.normalization;

import java.util.Map;
import java.util.function.Consumer;

public interface Header extends Consumer<Map<String,Object>> {
    void accept(Map<String, Object> stringObjectMap);
}
