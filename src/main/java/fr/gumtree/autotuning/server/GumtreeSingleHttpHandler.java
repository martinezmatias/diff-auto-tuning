package fr.gumtree.autotuning.server;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.tree.Tree;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import fr.gumtree.autotuning.fitness.Fitness;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.gumtree.GTProxy;
import fr.gumtree.autotuning.outils.DatOutputEngine;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;
import fr.gumtree.autotuning.treebuilder.JDTTreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class GumtreeSingleHttpHandler extends GumtreeAbstractHttpHandler {

	public GumtreeSingleHttpHandler(Fitness fitnessFunction, METRIC metric) {
		super(fitnessFunction, metric);
	}
	int counterPartialDiff = 0;
	int a = 0;
	String param = "";

	Tree tl = null;
	Tree tr = null;

	String host = "localhost";
	int port = 8001;
	String path = "single";

	String nameLeft = null;

	DatOutputEngine saver = new DatOutputEngine(null);

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

		if ("GET".equals(httpExchange.getRequestMethod())) {

			MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(httpExchange.

					getRequestURI()).build().getQueryParams();

			System.out.println("-path-> " + httpExchange.getRequestURI().getPath());

			System.out.println(queryParams);

			if (httpExchange.getRequestURI().getPath().equals("/single")) {

				singleDiff(httpExchange, queryParams);

			} else {

				System.err.println("Error: unknown path: " + httpExchange.getRequestURI().getPath());

			}

		}

	}
	
	GTProxy proxy = new GTProxy();
	
	public void singleDiff(HttpExchange httpExchange, MultiValueMap<String, String> queryParams) throws IOException {
		// http://localhost:8001/test?name=sam&action=load&model=jdt&left=l1&right=r1
		
		counterPartialDiff++;
		
		if (queryParams.get("action").contains("load")) {
			loadTrees(httpExchange, queryParams);

		} else if (queryParams.get("action").contains("run")) {

			if (tl == null || tr == null) {
				System.out.println("One tree is null");
				return;
			}

			String parameters = queryParams.get("parameters").get(0);

			System.out.println("Nr: "+ counterPartialDiff+" run with params " + parameters);
			//GTProxy proxy = new GTProxy();
			long start = System.currentTimeMillis();
			Diff diff = proxy.runT(tl, tr, parameters, null); // we dont want to save here, so we pass null to the out
			long end = System.currentTimeMillis();
			/////////
			JsonObject root = new JsonObject();
			if (diff != null) {

				System.out.println("Computed GumTree actions " + diff.editScript.asList().size());

				root.addProperty("fitness", diff.editScript.asList().size());
				root.addProperty("values", 1);
				
				JsonArray array = new JsonArray();
				array.add(diff.editScript.asList().size());
				root.add("allvalues", array);

				root.addProperty("parameters", parameters);

				System.out.println("run with params " + parameters);

				root.addProperty("status", "ok");

				root.addProperty("time", (end - start));

				if (outDirectory != null) {
					//
//					try {
//						//saver.saveUnified(nameLeft, parameters, diff, outDirectory);
//					} catch (NoSuchAlgorithmException | IOException e) {
//						e.printStackTrace();
//					}
				}

			} else {
				root.addProperty("status", "error");
			}

			//cacheResults.add(root);

			System.out.println("Output " + root.toString());
			handleResponse(httpExchange, root.toString());

		} else if (queryParams.get("action").contains("info")) {

			System.out.println("Output info" + cacheResults.toString());
			handleResponse(httpExchange, cacheResults.toString());

		}
	}

	public void loadTrees(HttpExchange httpExchange, MultiValueMap<String, String> queryParams) throws IOException {
		System.out.println("Load");
		
		counterPartialDiff = 0;

		String model = queryParams.get("model").get(0);
		String left = queryParams.get("left").get(0);
		String right = queryParams.get("right").get(0);

		if (queryParams.containsKey("out"))
			outDirectory = new File(queryParams.get("out").get(0));

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
			// Reset the cache
			cacheResults = new JsonArray();

			this.nameLeft = left;
			tl = treebuilder.build(new File(left));

			tr = treebuilder.build(new File(right));

			handleResponse(httpExchange, "{status=created}");

		} catch (Exception e) {
			System.out.println("Error loading trees");
			e.printStackTrace();

			handleResponse(httpExchange, "{status=error}");

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