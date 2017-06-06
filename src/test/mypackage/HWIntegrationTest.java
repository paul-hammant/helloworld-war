package mypackage;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.Test;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

import static mypackage.WebAppUnassembled.loadAnnotations;
import static org.hamcrest.CoreMatchers.equalTo;

public class HWIntegrationTest {

    @Before
    public void beforeAll() throws Exception {

        Server server = new Server(8080);

        WebAppContext context = new WebAppContext();

        context.setResourceBase("./web");
        loadAnnotations(context);
        server.setHandler(context);

        server.start();
        //server.join();

        System.out.println();


    }

    @Test
    public void testSomething() {
        when()
                .get("/helloworld")
            .then()
                .statusCode(200)
                .body(equalTo("OK"));
    }

}
