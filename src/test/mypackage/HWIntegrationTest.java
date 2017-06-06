package mypackage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;
import com.google.common.reflect.ClassPath;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.EventListener;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;

public class HWIntegrationTest {

    static void loadAnnotations(WebAppContext context) throws Exception {
        // Add annotations from classpath, using only file resources
        // (thus using only classes from this project's src folder).
        // NOTE: This mimics the annotation behaviour of loading a WAR,
        // but it is certainly not fully functional
        // (some annotations and/or annotate elements are not implemented).
        ClassPath classpath = ClassPath.from(HWIntegrationTest.class.getClassLoader());
        ImmutableSet<ClassPath.ClassInfo> topLevelClasses = classpath.getTopLevelClasses();
        for (ClassPath.ClassInfo classInfo : topLevelClasses) {
            if (!classInfo.getPackageName().startsWith("mypackage")) {
                continue;
            }
            if (classInfo.url().toString().startsWith("file:")) {
                String cname = classInfo.getName();
                Class<?> type = Class.forName(cname);

                WebServlet servletAnn = type.getAnnotation(WebServlet.class);
                if (servletAnn != null) {
                    for (String pattern : ObjectArrays.concat(
                            servletAnn.value(), servletAnn.urlPatterns(),
                            String.class)) {
                        System.out.println("pattern:" + pattern + " servlet:"
                                + cname);
                        context.addServlet(new ServletHolder((HttpServlet) type
                                .getConstructor().newInstance()), pattern);
                    }
                }

                WebFilter filterAnn = type.getAnnotation(WebFilter.class);
                if (filterAnn != null) {
                    for (String pattern : ObjectArrays.concat(
                            filterAnn.value(), filterAnn.urlPatterns(),
                            String.class)) {
                        System.out.println("pattern:" + pattern + " filter:"
                                + cname);
                        DispatcherType[] disTypes = filterAnn.dispatcherTypes();
                        EnumSet<DispatcherType> dispatches = EnumSet
                                .copyOf(Arrays.asList(disTypes));
                        context.addFilter(new FilterHolder((Filter) type
                                        .getConstructor().newInstance()), pattern,
                                dispatches);
                    }
                }

                WebListener listenAnn = type.getAnnotation(WebListener.class);
                if (listenAnn != null) {
                    System.out.println("listener:" + cname);
                    context.addEventListener((EventListener) type
                            .getConstructor().newInstance());
                }
            }
        }
    }


    @Before
    public void beforeAll() throws Exception {
        Server server = new Server(8080);
        WebAppContext context = new WebAppContext();
        context.setResourceBase("./web");
        loadAnnotations(context);
        server.setHandler(context);
        server.start();
    }

    @Test
    public void testSomething() {
        when()
            .get("/helloworld")
        .then()
            .statusCode(200)
            .body(equalTo("Hello, world\n"));
    }

}
