package dada.tuda.framework;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dada.tuda.framework")
public class DadaTudaFrameworkProperties {
    private final MessagingProperties messaging = new MessagingProperties();
    private final DomainsProperties domains = new DomainsProperties();

    @Getter
    @Setter
    public static class MessagingProperties {
        private boolean storeOnlyCancelable;
        private SagaProperties saga;

        @Getter
        @Setter
        public static class SagaProperties {
            private boolean enabled;
        }
    }

    @Getter
    @Setter
    public static class DomainsProperties {
        private final QueueProperties queue = new QueueProperties();

        @Getter
        @Setter
        public static class QueueProperties {
            private final NamingProperties naming = new NamingProperties();

            @Getter
            @Setter

            public static class NamingProperties {
                private boolean perService;
            }
        }
    }
}