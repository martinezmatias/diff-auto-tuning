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
public class GumtreeMultipleHttpHandler extends GumtreeAbstractHttpHandler {

	public GumtreeMultipleHttpHandler(Fitness fitnessFunction, METRIC metric) {
		super(fitnessFunction, metric);
	}

	List<Pair<Tree, Tree>> files = new ArrayList<>();
	List<String> names = new ArrayList<>();

	String host = "localhost";
	int port = 8001;
	String path = "multiple";
	DatOutputEngine saver = new DatOutputEngine(null);

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

		System.out.println("Multiple: ");

		if ("GET".equals(httpExchange.getRequestMethod())) {

			MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(httpExchange.

					getRequestURI()).build().getQueryParams();

			System.out.println("-path-> " + httpExchange.getRequestURI().getPath());

			System.out.println(queryParams);

			if (httpExchange.getRequestURI().getPath().equals("/multiple")) {

				if (queryParams.get("action").contains("load")) {
					loadTree(httpExchange, queryParams);

				} else if (queryParams.get("action").contains("run")) {
					runDiff(httpExchange, queryParams);

				} else if (queryParams.get("action").contains("info")) {

					System.out.println("Output info" + cacheResults.toString());
					handleResponse(httpExchange, cacheResults.toString());

				}

			} else {
				System.err.println("Error: unknown path: " + httpExchange.getRequestURI().getPath());
			}

		}

	}

	public void runDiff(HttpExchange httpExchange, MultiValueMap<String, String> queryParams) throws IOException {
		JsonObject root = new JsonObject();

		String parameters = queryParams.get("parameters").get(0);
		root.addProperty("parameters", parameters);

		System.out.println("\n**run with params " + parameters);
		System.out.println("--current analyzed in cache: " + this.cacheResults.size());

		List<Double> values = new ArrayList<>();
		JsonArray valuessArray = new JsonArray();
		
		JsonArray timesArray = new JsonArray();

		// Collect values
		for (int i = 0; i < this.files.size(); i++) {
			System.out.println("running " + (i + 1) + "/" + this.files.size());
			Pair<Tree, Tree> pair = files.get(i);

			GTProxy proxy = new GTProxy();

			long start = System.currentTimeMillis();
			Diff diff = proxy.run(pair.first, pair.second, parameters, null); // we dont want to save here, so we pass
			// null to the out
			
			long end = System.currentTimeMillis();

			Double fitnessOfDiff = fitnessFunction.getFitnessValue(diff, this.metric);
			System.out.println("fitness "+fitnessOfDiff);
			values.add(fitnessOfDiff);
			valuessArray.add(fitnessOfDiff);
			
			timesArray.add((end - start));


		}

		double fitness = fitnessFunction.computeFitness(values, this.metric);
		root.addProperty("fitness", fitness);
		root.addProperty("values", values.size());
		root.add("allvalues", valuessArray);
		root.add("times", timesArray);

		if (values.size() > 0)
			root.addProperty("status", "ok");
		else
			root.addProperty("status", "error");

		cacheResults.add(root);
		System.out.println("Output for parameter after " + values.size() + " evaluations " + root.toString());
		handleResponse(httpExchange, root.toString());
	}

	public void loadTree(HttpExchange httpExchange, MultiValueMap<String, String> queryParams) throws IOException {
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

			handleResponse(httpExchange, "{status=created, operation=multiplecreate, pairs=" + this.files.size() + "}");

		} catch (Exception e) {
			System.out.println("Error loading trees");
			e.printStackTrace();

			handleResponse(httpExchange, "error");
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