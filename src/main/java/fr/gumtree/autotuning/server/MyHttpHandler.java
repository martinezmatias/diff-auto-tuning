package fr.gumtree.autotuning.server;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang3.StringEscapeUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MyHttpHandler implements HttpHandler {

	int a = 0;
	String param = "";

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

		String requestParamValue = null;

		if ("GET".equals(httpExchange.getRequestMethod())) {

			requestParamValue = handleGetRequest(httpExchange);
			param += requestParamValue;

		} else if ("POST".equals(httpExchange)) {

			// requestParamValue = handlePostRequest(httpExchange);

		}

		handleResponse(httpExchange, requestParamValue);

	}

	// http://localhost:8001/test?name=sam
	private String handleGetRequest(HttpExchange httpExchange) {

		return httpExchange.

				getRequestURI()

				.toString()

				.split("\\?")[1]

						.split("=")[1];

	}

	public void handleResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {

		System.out.println("a: " + a++);
		OutputStream outputStream = httpExchange.getResponseBody();

		StringBuilder htmlBuilder = new StringBuilder();

		htmlBuilder.append("<html>").

				append("<body>").

				append("<h1>").

				append("Hello ")

				.append(param).append(a)

				.append("</h1>")

				.append("</body>")

				.append("</html>");

		// encode HTML content

		String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());

		// this line is a must

		httpExchange.sendResponseHeaders(200, htmlResponse.length());

		outputStream.write(htmlResponse.getBytes());

		outputStream.flush();

		outputStream.close();

	}

}