package de.hska.iwi.vislab.lab2.example;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("helloworld")
public class HalloWeltService {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String halloText(@DefaultValue("Welt") @QueryParam("name") String name) {
		return "Hallo " + name + "!";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String halloHtml(@DefaultValue("Welt") @QueryParam(value = "name") String name) {
		String answer = "Hallo " + name + "!";
		return "<html><title>HelloWorld</title><body><h2>Html: " + answer + "</h2></body></html>";
	}
}
