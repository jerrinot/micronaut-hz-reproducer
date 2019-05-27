package info.jerrinot;

import com.hazelcast.config.Config;
import com.hazelcast.config.DomConfigProcessor;
import com.hazelcast.config.yaml.W3cDomUtil;
import com.hazelcast.integration.micronaut.HazelcastSyncCache;
import com.hazelcast.internal.yaml.YamlDomBuilder;
import com.hazelcast.internal.yaml.YamlMapping;
import com.hazelcast.internal.yaml.YamlNode;
import io.micronaut.cache.SyncCache;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import org.w3c.dom.Node;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Controller("/hello")
public class HelloController {

    @Inject
    public FooBean fooBean;

    @Inject
    public HazelcastSyncCache cache;

    @Property(name = "my-hazelcast")
    @MapFormat(transformation = MapFormat.MapTransformation.NESTED)
    Map<Object, Object> props;

    @Get("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String index() {
        Object hazelcast = props.get("hazelcast");
        LinkedHashMap<Object, Object> root = new LinkedHashMap<>();
        root.put("hazelcast", hazelcast);

        Config config = new Config();
        try {
            Method buildMethod = YamlDomBuilder.class.getDeclaredMethod("build", Object.class, String.class);
            buildMethod.setAccessible(true);
            YamlMapping yamlMapping = (YamlMapping) buildMethod.invoke(null, root, "hazelcast");
            Node node = W3cDomUtil.asW3cNode(yamlMapping);

            Class<?> yamlMemberProcessorClazz = Class.forName("com.hazelcast.config.YamlMemberDomConfigProcessor");
            Constructor<?> declaredConstructor = yamlMemberProcessorClazz.getDeclaredConstructor(boolean.class, Config.class);
            declaredConstructor.setAccessible(true);
            DomConfigProcessor yamlMemberProcessor = (DomConfigProcessor) declaredConstructor.newInstance(true, config);
            yamlMemberProcessor.buildConfig(node);

            System.out.println(node);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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