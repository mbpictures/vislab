package de.hska.iwi.vislab.lab2.example;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
// import javax.ws.rs.core.MediaType;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FibonacciTest {

	private static HttpServer server;
	private static WebTarget target;

	@BeforeClass
	public static void setUp() {
		// start the server
		server = Main.startServer();
		// create the client
		Client c = ClientBuilder.newClient();

		// uncomment the following line if you want to enable
		// support for JSON in the client (you also have to uncomment
		// dependency on jersey-media-json module in pom.xml and
		// Main.startServer())
		// --
		// c.configuration().enable(new
		// org.glassfish.jersey.media.json.JsonJaxbFeature());

		target = c.target(Main.BASE_URI);
	}

	@AfterClass
	public static void tearDown() {
		server.shutdown();
	}

	@Test
	public void test1AddFib() {
		String responseMsg = target.path("fibonacci").request().post(Entity.text(""), String.class);
		assertEquals("0", responseMsg);
	}

	@Test
	public void test2GetFib() {
		String responseMsg = target.path("fibonacci/0").request().method("GET").readEntity(String.class);
		assertEquals("1", responseMsg);
		responseMsg = target.path("fibonacci").request().method("GET").readEntity(String.class);
		assertEquals("[1]", responseMsg);
	}

	@Test
	public void test3IncreaseCounter() {
		String responseMsg = target.path("fibonacci/0/increase").request().post(Entity.text(""), String.class);
		assertEquals("1", responseMsg);
		responseMsg = target.path("fibonacci/0/increase").request().post(Entity.text(""), String.class);
		assertEquals("2", responseMsg);
		responseMsg = target.path("fibonacci/0/increase").request().post(Entity.text(""), String.class);
		assertEquals("3", responseMsg);

		responseMsg = target.path("fibonacci/0").request().method("GET").readEntity(String.class);
		assertEquals("3", responseMsg);
	}

	@Test
	public void test4ResetCounter() {
		String responseMsg = target.path("fibonacci/0/reset").request().post(Entity.text(""), String.class);
		assertEquals("1", responseMsg);
		responseMsg = target.path("fibonacci/0").request().method("GET").readEntity(String.class);
		assertEquals("1", responseMsg);
	}

	@Test
	public void test5PutCounter() {
		String responseMsg = target.path("fibonacci/0").request().put(Entity.json(new Fibonacci.FibonacciValue(5)), String.class);
		assertEquals("5", responseMsg);

		responseMsg = target.path("fibonacci/0").request().method("GET").readEntity(String.class);
		assertEquals("5", responseMsg);
	}

	@Test
	public void test6DeleteCounter() {
		target.path("fibonacci/0").request().delete();
		String responseMsg = target.path("fibonacci").request().get().readEntity(String.class);
		assertEquals("[]", responseMsg);
	}
}
