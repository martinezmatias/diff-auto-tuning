package fr.gumtree.autotuning.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.google.gson.JsonArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import fr.gumtree.autotuning.outils.SaverDiff;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class GumtreeAbstractHttpHandler implements HttpHandler {

	public abstract String getHost();

	public abstract int getPort();

	public abstract String getPath();

	protected File outDirectory = null;

	/**
	 * Stores the different calls to run
	 */
	JsonArray cacheResults = new JsonArray();

	SaverDiff saver = new SaverDiff();

	public void handleResponse(HttpExchange httpExchange, String reponse) throws IOException {

		OutputStream outputStream = httpExchange.getResponseBody();

		StringBuilder htmlBuilder = new StringBuilder();

		htmlBuilder.append(reponse);

		httpExchange.getResponseHeaders().set("Content-Type", "appication/json");

		// String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());
		String htmlResponse = reponse;
		httpExchange.sendResponseHeaders(200, htmlResponse.length());

		outputStream.write(htmlResponse.getBytes());

		outputStream.flush();

		outputStream.close();

	}

	public File getOutDirectory() {
		return outDirectory;
	}

	public void setOutDirectory(File outDirectory) {
		this.outDirectory = outDirectory;
	}

}
