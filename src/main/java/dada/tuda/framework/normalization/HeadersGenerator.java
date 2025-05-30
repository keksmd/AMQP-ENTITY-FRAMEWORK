package dada.tuda.framework.normalization;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class HeadersGenerator implements Consumer<Map<String, Object>> {
    private final List<Header> headers;

    @Override
    public void accept(Map<String, Object> stringObjectMap) {
        headers.forEach(g -> g.accept(stringObjectMap));
    }
}
