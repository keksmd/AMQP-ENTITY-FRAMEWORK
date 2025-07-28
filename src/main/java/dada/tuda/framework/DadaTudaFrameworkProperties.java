package dada.tuda.framework;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@Getter
@Setter
public class DadaTudaFrameworkProperties {
    @NestedConfigurationProperty
    private MessagingProperties messaging;
    @NestedConfigurationProperty
    private DomainsProperties domains;

    @Getter
    @Setter
    public static class MessagingProperties {
        private boolean storeOnlyCancelable;
        @NestedConfigurationProperty
        private SagaProperties saga;
        @NestedConfigurationProperty
        private CacheProperties cache;

        @Getter
        @Setter
        public static class SagaProperties {
            private boolean enabled;
            private Duration ttl;
        }

        @Getter
        @Setter
        public static class CacheProperties {
            @NestedConfigurationProperty
            private InMemoryProperties inMemory;

            @Getter
            @Setter
            public static class InMemoryProperties {
                private int size;
            }

        }
    }

    @Getter
    @Setter
    public static class DomainsProperties {
        @NestedConfigurationProperty
        private QueueProperties queue;

        @Getter
        @Setter
        public static class QueueProperties {
            @NestedConfigurationProperty
            private NamingProperties naming;

            @Getter
            @Setter
            @AllArgsConstructor
            public static class NamingProperties {
                private boolean perService;
            }
        }
    }
}