package dada.tuda.framework;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dada.tuda.framework")
public class DadaTudaFrameworkProperties {
    private MessagingProperties messaging;
    private DomainsProperties domains;

    public DadaTudaFrameworkProperties(MessagingProperties messaging, DomainsProperties domains) {
        this.messaging = messaging;
        this.domains = domains;
    }

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
    @AllArgsConstructor
    public static class DomainsProperties {
        private QueueProperties queue;

        @Getter
        @Setter
        @AllArgsConstructor
        public static class QueueProperties {
            private NamingProperties naming;

            @Getter
            @Setter

            public static class NamingProperties {
                private boolean perService;
            }
        }
    }
}