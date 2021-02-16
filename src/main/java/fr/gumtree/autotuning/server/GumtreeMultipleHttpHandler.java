package fr.gumtree.autotuning.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.utils.Pair;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import fr.gumtree.autotuning.GTProxy;
import fr.gumtree.autotuning.ITreeBuilder;
import fr.gumtree.autotuning.TuningEngine.ASTMODE;
import fr.gumtree.autotuning.treebuilder.JDTTreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class GumtreeMultipleHttpHandler implements HttpHandler {

	List<Pair<Tree, Tree>> files = new ArrayList();

	String host = "localhost";
	int port = 8001;
	String path = "multiple";

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

		System.out.println("Multiple: ");

		if ("GET".equals(httpExchange.getRequestMethod())) {

			MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(httpExchange.

					getRequestURI()).build().getQueryParams();

			System.out.println("-path-> " + httpExchange.getRequestURI().getPath());

			System.out.println(queryParams);

			if (httpExchange.getRequestURI().getPath().equals("/multiple")) {

				singleDiff(httpExchange, queryParams);

			} else {
				System.err.println("Error: unknown path: " + httpExchange.getRequestURI().getPath());
			}

		}

	}

	public void singleDiff(HttpExchange httpExchange, MultiValueMap<String, String> queryParams) throws IOException {
		// http://localhost:8001/test?name=sam&action=load&model=jdt&left=l1&right=r1
		if (queryParams.get("action").contains("load")) {
			System.out.println("Load");

			String model = queryParams.get("model").get(0);
			String file = queryParams.get("file").get(0);

			System.out.println(model + " " + file);

			ITreeBuilder treebuilder = null;
			if (ASTMODE.GTSPOON.name().equals(model)) {
				treebuilder = new SpoonTreeBuilder();
			} else if (ASTMODE.JDT.name().equals(model)) {
				treebuilder = new JDTTreeBuilder();
			} else {
				System.err.println("Mode not configured " + model);
			}

			try {

				createMultipleTrees(httpExchange, treebuilder, file);

				handleResponse(httpExchange, "{status=ok, operation=multiplecreate, pairs=" + this.files.size() + "}");

			} catch (Exception e) {
				System.out.println("Error loading trees");
				e.printStackTrace();

				handleResponse(httpExchange, "error");
			}

		} else if (queryParams.get("action").contains("run")) {

			for (Pair<Tree, Tree> pair : files) {

				String out = null;
				if (queryParams.containsKey("out"))
					out = queryParams.get("out").get(0);

				String parameters = queryParams.get("parameters").get(0);

				System.out.println("run with params " + parameters);

				GTProxy proxy = new GTProxy();

				Diff diff = proxy.run(pair.first, pair.second, parameters);
				System.out.println("actions " + diff.editScript.asList().size());

				handleResponse(httpExchange, "{status=ok, actions=" + diff.editScript.size() + "}");
			}
		}
	}

	public void handleResponse(HttpExchange httpExchange, String reponse) throws IOException {

		OutputStream outputStream = httpExchange.getResponseBody();

		StringBuilder htmlBuilder = new StringBuilder();

		htmlBuilder.append(reponse);

		// encode HTML content

		String htmlResponse = StringEscapeUtils.escapeHtml4(htmlBuilder.toString());

		// this line is a must

		// httpExchange.getResponseHeaders().set("Content-Type", "appication/json");

		httpExchange.sendResponseHeaders(200, htmlResponse.length());

		outputStream.write(htmlResponse.getBytes());

		outputStream.flush();

		outputStream.close();

	}

	public void createMultipleTrees(HttpExchange httpExchange, ITreeBuilder treebuilder, String path)
			throws IOException {
		this.files.clear();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);

				line = reader.readLine();

				String[] sp = line.split(" ");

				Tree tl = treebuilder.build(new File(sp[0]));

				Tree tr = treebuilder.build(new File(sp[1]));

				this.files.add(new Pair<Tree, Tree>(tl, tr));

			}
			reader.close();

			handleResponse(httpExchange, "created");
		} catch (Exception e) {
			handleResponse(httpExchange, "error");
			e.printStackTrace();
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}