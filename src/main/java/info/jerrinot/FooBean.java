package info.jerrinot;

import io.micronaut.cache.annotation.Cacheable;

import javax.inject.Singleton;
import java.util.UUID;


@Singleton
public class FooBean {

    @Cacheable
    public String somethingSlow() {
        return UUID.randomUUID().toString();
    }
}
