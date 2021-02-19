package fr.gumtree.autotuning.server;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class GumtreeAbstractHttpHandler implements HttpHandler {

	public abstract String getHost();

	public abstract int getPort();

	public abstract String getPath();

	public void handleResponse(HttpExchange httpExchange, String reponse) throws IOException {

		OutputStream outputStream = httpExchange.getResponseBody();

		StringBuilder htmlBuilder = new StringBuilder();

		htmlBuilder.append(reponse);

		// encode HTML content

		// this line is a must

		httpExchange.getResponseHeaders().set("Content-Type", "appication/json");

		// String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
		String htmlResponse = reponse;
		httpExchange.sendResponseHeaders(200, htmlResponse.length());

		outputStream.write(htmlResponse.getBytes());

		outputStream.flush();

		outputStream.close();

	}

}
