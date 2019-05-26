package info.jerrinot;

import com.hazelcast.integration.micronaut.HazelcastSyncCache;
import io.micronaut.cache.SyncCache;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

import javax.inject.Inject;
import java.util.Optional;

@Controller("/hello")
public class HelloController {

    @Inject
    public FooBean fooBean;

    @Inject
    public HazelcastSyncCache cache;

    @Get("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String index() {
        Optional<String> foo = cache.get("foo", String.class);
        if (foo.isPresent()) {
            return foo.get();
        } else {
            String s = fooBean.somethingSlow();
            cache.put("foo", s);
            return s;
        }
    }
}