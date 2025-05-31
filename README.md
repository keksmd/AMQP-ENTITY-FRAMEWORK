This framework is all you need to start declarative DDD with distributed transactions(Saga is implemented) out of box (between services using this framework or adapters)


Now framework supports only RabbitMQ, but we have a plan to promote Kafka support

Enable configuration partitionaly with
```
@EnableCustomConfigs(types = { RABBIT, REDIS, TRACING, CACHE, IDEMPOTENCY, CACHE_ANNOTATIONS ,IDEMPOTENCY_SAGAS, SAGAS })
```
or entire with 
```
@EnableCustomConfigs(types = {ConfigType.ALL})
```




Design Activity domain and EventType Beans (using EnumBean tool)
``` java
@EnumBean(classnamePrefix = "false",lowercase = "true")
public enum MessagingAggregate implements IMessagingDomain, IEnum {
    ACTIVITY
    @Override
    public String getName() {
        return this.name();
    }
}
@EnumBean
@Getter
public enum MessagingEventType implements IMessagingEventType, IEnum {
    ACTIVITY_CREATED_EVENT(ACTIVITY, CREATED, false),
    ACTIVITY_UPDATED_EVENT(ACTIVITY, UPDATED, false),
    ACTIVITY_DELETED_EVENT(ACTIVITY, DELETED, false),

    private final IMessagingDomain domain;
    private final IEventAction actionType;
    private final boolean query;

    MessagingEventType(IMessagingDomain domain, IEventAction actionType, boolean query) {
        this.domain = domain;
        this.actionType = actionType;
        this.query = query;
    }
}
```

Use handler by Messagigng type for handling Event and cancel it (in Saga pipeline)

``` java
@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityCreatedMessageHandler extends AbstractCancelableCommandMessageHandler {
    private final ObjectMapper objectMapper;
    private final LocalActivityCRUDPort localActivityCRUDPort;
    
    @Override
    public Boolean canHandle(IMessagingEventType type) {
        return MessagingEventType.ACTIVITY_CREATED_EVENT.equals(type);
    }

    @Override
    public void handleCommand(AbstractNormalMessage message) throws Exception{
        ActivityDto eventDto = objectMapper.convertValue(message.getProperties(), ActivityDto.class);
        localActivityCRUDPort.save(eventDto);
    }

    @Override
    public void cancel(AbstractNormalMessage message) {
        ActivityDto eventDto = objectMapper.convertValue(message.getProperties(), ActivityDto.class);
        if (eventDto != null) {
            localActivityCRUDPort.deleteById(eventDto.getId());
            log.info("Compensive Transaction for message (Id={}) and event with ID={}", message.getOperationId(), eventDto.getId());
        } else {
            log.error("can't convert message to ActivityDto, message: {}", message);
        }
    }
}



```


