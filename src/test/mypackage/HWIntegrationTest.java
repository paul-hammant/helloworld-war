package mypackage;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;

public class HWIntegrationTest {

    @Before
    public void beforeAll() throws Exception {

        System.out.println("cd: " + new File(".").getAbsolutePath());

        Server server = new Server(8080);
        // Configure webapp provided as external WAR
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar("../sample.war");
        server.setHandler(webapp);

        // Start the server
        server.start();

    }

    @Test
    public void testSomething() throws InterruptedException {

//        Thread.sleep(10000000);

        when()
            .get("/hello")
        .then()
            .statusCode(200)
            .body(containsString("Hello, World"));
    }

}
