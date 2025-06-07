package dada.tuda.framework;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@Getter
@Setter
@AllArgsConstructor
@ConfigurationProperties(prefix = "dada.tuda.framework")
public class DadaTudaFrameworkProperties {
    @NestedConfigurationProperty
    private MessagingProperties messaging;
    @NestedConfigurationProperty
    private DomainsProperties domains;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class MessagingProperties {
        private boolean storeOnlyCancelable;
        @NestedConfigurationProperty
        private SagaProperties saga;

        @Getter
        @Setter
        @AllArgsConstructor
        public static class SagaProperties {
            private boolean enabled;
            private Duration ttl;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class DomainsProperties {
        @NestedConfigurationProperty
        private final QueueProperties queue;

        @Getter
        @Setter
        @AllArgsConstructor
        public static class QueueProperties {
            @NestedConfigurationProperty
            private final NamingProperties naming;

            @Getter
            @Setter
            @AllArgsConstructor
            public static class NamingProperties {
                private boolean perService;
            }
        }
    }
}