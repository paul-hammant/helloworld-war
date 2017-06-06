package mypackage;

import com.google.common.collect.ObjectArrays;
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

    private static void addListener(WebAppContext context, Class<?> type) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException {
        WebListener listenAnn = type.getAnnotation(WebListener.class);
        if (listenAnn != null) {
            context.addEventListener((EventListener) type
                    .getConstructor().newInstance());
        }
    }

    private static void addFilter(WebAppContext context, Class<?> type) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException {
        WebFilter filterAnn = type.getAnnotation(WebFilter.class);
        if (filterAnn != null) {
            for (String pattern : ObjectArrays.concat(
                    filterAnn.value(), filterAnn.urlPatterns(),
                    String.class)) {
                DispatcherType[] disTypes = filterAnn.dispatcherTypes();
                EnumSet<DispatcherType> dispatches = EnumSet
                        .copyOf(Arrays.asList(disTypes));
                context.addFilter(new FilterHolder((Filter) type
                                .getConstructor().newInstance()), pattern,
                        dispatches);
            }
        }
    }

    private static void addServlet(WebAppContext context, Class<?> type) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException {
        WebServlet servletAnn = type.getAnnotation(WebServlet.class);
        if (servletAnn != null) {
            for (String pattern : ObjectArrays.concat(
                    servletAnn.value(), servletAnn.urlPatterns(),
                    String.class)) {
                context.addServlet(new ServletHolder((HttpServlet) type
                        .getConstructor().newInstance()), pattern);
            }
        }
    }


    @Before
    public void beforeAll() throws Exception {
        Server server = new Server(8080);
        WebAppContext context = new WebAppContext();
        context.setResourceBase("./web");
        addServlet(context, HelloworldServlet.class);
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
