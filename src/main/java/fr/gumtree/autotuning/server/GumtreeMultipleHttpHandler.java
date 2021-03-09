package fr.gumtree.autotuning.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import fr.gumtree.autotuning.GTProxy;
import fr.gumtree.autotuning.ExhaustiveEngine.ASTMODE;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;
import fr.gumtree.autotuning.treebuilder.JDTTreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class GumtreeMultipleHttpHandler extends GumtreeAbstractHttpHandler {

	List<Pair<Tree, Tree>> files = new ArrayList<>();
	List<String> names = new ArrayList<>();

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
				System.out.println("Creating multiples trees: ");
				cacheResults = new JsonArray();
				createMultipleTrees(httpExchange, treebuilder, file);

				handleResponse(httpExchange,
						"{status=created, operation=multiplecreate, pairs=" + this.files.size() + "}");

			} catch (Exception e) {
				System.out.println("Error loading trees");
				e.printStackTrace();

				handleResponse(httpExchange, "error");
			}

		} else if (queryParams.get("action").contains("run")) {
			JsonObject root = new JsonObject();

			JsonArray actions = new JsonArray();
			root.add("actions", actions);
			String parameters = queryParams.get("parameters").get(0);
			root.addProperty("parameters", parameters);

			System.out.println("\n**run with params " + parameters);
			System.out.println("--current analyzed in cache: " + this.cacheResults.size());
			for (int i = 0; i < this.files.size(); i++) {
				System.out.println("running " + (i + 1) + "/" + this.files.size());
				Pair<Tree, Tree> pair = files.get(i);

				File out = null;
				if (queryParams.containsKey("out"))
					out = new File(queryParams.get("out").get(0));

				GTProxy proxy = new GTProxy();

				Diff diff = proxy.run(pair.first, pair.second, parameters, out);

				JsonObject config = new JsonObject();
				config.addProperty("file", this.names.get(0));
				actions.add(config);

				if (diff != null) {
					config.addProperty("nractions", diff.editScript.asList().size());

				} else {
					// As the diff is null (probably an error happens) we put a large integer)
					config.addProperty("nractions", Integer.MAX_VALUE);
				}
			}

			if (actions.size() > 0)
				root.addProperty("status", "ok");
			else
				root.addProperty("status", "error");

			cacheResults.add(root);
			System.out.println("Output " + root.toString());
			handleResponse(httpExchange, root.toString());

		} else if (queryParams.get("action").contains("info")) {

			System.out.println("Output info" + cacheResults.toString());
			handleResponse(httpExchange, cacheResults.toString());

		}
	}

	public void createMultipleTrees(HttpExchange httpExchange, ITreeBuilder treebuilder, String path)
			throws IOException {
		this.files.clear();
		this.names.clear();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);

				System.out.println("Line " + line);

				String[] sp = line.split(" ");

				Tree tl = treebuilder.build(new File(sp[0]));

				Tree tr = treebuilder.build(new File(sp[1]));

				this.files.add(new Pair<Tree, Tree>(tl, tr));
				this.names.add(sp[0]);

				// Next line
				line = reader.readLine();

			}
			reader.close();

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