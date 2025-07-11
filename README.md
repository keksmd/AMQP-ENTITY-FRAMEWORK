This starter is all you need to start declarative DDD with distributed transactions(Saga is implemented) out of the
box (
between services using this framework or adapters)
Now we support only RabbitMQ, but we have a plan to promote Kafka support

Configuration enables automatically while you have
org.springframework.amqp.rabbit.connection.ConnectionFactory bean

org.springframework.data.redis.connection.RedisConnectionFactory also enables caching

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
and add `@EnableMessagingRepositories(basePackages = "your.package.with.repositories")` to your configuration class

``` java
public interface PetMessageRepository  extends MessagingEntittyRepository<PetMessagePayload> {
}
```

Use handler by Domain and ActionType for handling Event and cancel it (in Saga pipeline)

``` java

@DomainHandlers(domain = "example")
public class PetHandler {
    @Autowired
    private  PetLocalService petLocalService;

    @ActionHandler(action = "created", cancelMethod = "cancelCreate")
    public void  handleCreate(PetDto petDto) {
        petLocalService.create(petDto);
    }

    @ActionHandler(PetDto petDto)
    public void handleDelete(PetDto petToRemove) {
         petLocalService.delete(petDto);
    }

    public void cancelCreate(NormalMessage message, PetDto petToRemove) {
       log.info("deleting pet due to {}",message.getO) 
       petLocalService.deleteIfExists(petDto.getId());
    }

    @ActionHandler(action = "requested")
    public Object handleQuery(NormalMessage message) {
        return petLocalService.getById(petDto);
    }
}

```

More details and examples you can find in the [Demo-Project](https://github.com/keksmd/AMQP-ENTITTY-FRAMEWORK-DEMO)

