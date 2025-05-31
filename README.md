This starter is all you need to start declarative DDD with distributed transactions(Saga is implemented) out of box (
between services using this framework or adapters)
Now we support only RabbitMQ, but we have a plan to promote Kafka support

Configuration enables automaticaly while you have
org.springframework.amqp.rabbit.connection.ConnectionFactory bean
and org.springframework.data.redis.connection.RedisConnectionFactory bean enables caching

Design PET domain

``` java
@Data
@MessagingEntity(domain = PetMessagePayload.PET_DOMAIN,queues = "pet-queue")
public class PetMessagePayload {
    public static final String PET_DOMAIN = "pet";
    String id;
    String name;
    String description;
    String type;
    String status;
}
```

Use JPA-like repository to send messages, you need only to extend MessagingEntityRepository interface

``` java
public interface PetMessageRepository  extends MessagingEntittyRepository<PetMessagePayload> {
}
```

Use handler by Domain and ActionType for handling Event and cancel it (in Saga pipeline)

``` java
@Component
public class CreatePetHandler extends AbstractCancelableCommandMessageHandler {
    @Autowired
    private  PetLocalService petLocalService;
    @Autowired
    private ObjectMapper objectMapper;
    
    //Describes message handler can process
    @Override
    public Boolean canHandle(IMessagingDomain domain, IEventAction action) {
        return domain.getName().equals(PET_DOMAIN) &&
               action.equals(CRUDEventActionTypes.CREATED);
    }
    //Describes how to handle message
    @Override
    public void handleCommand(NormalMessage message) throws Exception {
        var payload = message.getPayloadMap();
        var petToDelete = objectMapper.convertValue(payload, PetEntityDto.class);
        petLocalService.create(petToDelete);
    }
    
    //Describes how to cancel message (same as was handled)
    @Override
    public void cancel(NormalMessage message) {
        var payload = message.getPayloadMap();
        var petToDelete = objectMapper.convertValue(payload, PetEntityDto.class);
        petLocalService.delete(petToDelete.getId());
    }
}
```

More details you can find in the [Demo-Project](https://github.com/keksmd/AMQP-ENTITTY-FRAMEWORK-DEMO)

