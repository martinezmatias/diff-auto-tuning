package fr.gumtree.autotuning.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.tree.Tree;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import fr.gumtree.autotuning.GTProxy;
import fr.gumtree.autotuning.ITreeBuilder;
import fr.gumtree.autotuning.TuningEngine.ASTMODE;
import fr.gumtree.autotuning.treebuilder.JDTTreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

public class GumtreeHttpHandler implements HttpHandler {

	int a = 0;
	String param = "";

	Tree tl = null;
	Tree tr = null;

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

		if ("GET".equals(httpExchange.getRequestMethod())) {

			MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(httpExchange.

					getRequestURI()).build().getQueryParams();

			System.out.println(queryParams);

			// http://localhost:8001/test?name=sam&action=load&model=jdt&left=l1&right=r1
			if (queryParams.get("action").contains("load")) {
				System.out.println("Load");

				String model = queryParams.get("model").get(0);
				String left = queryParams.get("left").get(0);
				String right = queryParams.get("right").get(0);

				System.out.println(model + left + right);

				ITreeBuilder treebuilder = null;
				if (ASTMODE.GTSPOON.name().equals(model)) {
					treebuilder = new SpoonTreeBuilder();
				} else if (ASTMODE.JDT.name().equals(model)) {
					treebuilder = new JDTTreeBuilder();
				} else {
					System.err.println("Mode not configured " + model);
				}

				try {
					tl = treebuilder.build(new File(left));

					tr = treebuilder.build(new File(right));

					handleResponse(httpExchange, "created");

				} catch (Exception e) {
					System.out.println("Error loading trees");
					e.printStackTrace();

					handleResponse(httpExchange, "error");

				}

			} else if (queryParams.get("action").contains("run")) {

				if (tl == null || tr == null) {
					System.out.println("One tree is null");
					return;
				}

				String parameters = queryParams.get("parameters").get(0);

				System.out.println("run with params " + parameters);

				GTProxy proxy = new GTProxy();

				Diff diff = proxy.run(tl, tr, parameters);

				handleResponse(httpExchange, "{status=ok, actions=" + diff.editScript.size() + "}");

			}

		}

	}

	public void handleResponse(HttpExchange httpExchange, String reponse) throws IOException {

		System.out.println("a: " + a++);
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

}