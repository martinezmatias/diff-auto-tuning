package fr.gumtree.autotuning.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import fr.gumtree.autotuning.entity.ResponseGlobalBestParameter;
import fr.gumtree.autotuning.experimentrunner.StructuredFolderfRunner;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.outils.DatOutputEngine;
import fr.gumtree.autotuning.searchengines.ResultByConfig;

/**
 * 
 * @author Matias Martinez
 *
 */
public class GumtreeCacheHttpHandler extends GumtreeMultipleHttpHandler {

	ResultByConfig valuesPerConfig;

	List<File> filesToAnalyze = new ArrayList<>();
	String host = "localhost";
	int port = 8001;
	String path = "multiple";
	DatOutputEngine saver = new DatOutputEngine(null);

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {

		System.out.println("\n\n--Receiviing Multiple: ");

		if ("GET".equals(httpExchange.getRequestMethod())) {

			MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(httpExchange.

					getRequestURI()).build().getQueryParams();

			System.out.println("-path-> " + httpExchange.getRequestURI().getPath());

			System.out.println(queryParams);

			if (httpExchange.getRequestURI().getPath().equals("/multiple")) {

				if (queryParams.get("action").contains("load")) {
					retrieveFilesToAnalyze(httpExchange, queryParams);

				} else if (queryParams.get("action").contains("run")) {
					retrieveCacheDiff(httpExchange, queryParams);

				} else if (queryParams.get("action").contains("info")) {

					System.out.println("Output info" + cacheResults.toString());
					handleResponse(httpExchange, cacheResults.toString());

				}

			} else {
				System.err.println("Error: unknown path: " + httpExchange.getRequestURI().getPath());
			}

		}

	}

	public void retrieveCacheDiff(HttpExchange httpExchange, MultiValueMap<String, String> queryParams)
			throws IOException {
		JsonObject root = new JsonObject();

		JsonArray actions = new JsonArray();
		root.add("actions", actions);
		String parameters = queryParams.get("parameters").get(0);
		root.addProperty("parameters", parameters);

		System.out.println("\run Cache Mode  with params " + parameters);
		// System.out.println("--current analyzed in cache: " +
		// this.cacheResults.size());

		List<Integer> values = this.valuesPerConfig.get(parameters);

		if (values == null) {
			System.out.println(" no values for " + parameters);
		}

		if (values.size() != this.filesToAnalyze.size()) {
			System.err.println("Error! Different sizes!");
		}

		System.out.println("Values " + values.size());

		for (int i = 0; i < this.filesToAnalyze.size(); i++) {
			// System.out.println("running " + (i + 1) + "/" + this.filesToAnalyze.size());
			File pair = filesToAnalyze.get(i);

			JsonObject config = new JsonObject();
			config.addProperty("file", pair.getName());
			actions.add(config);

			config.addProperty("nractions", values.get(i));

			if (this.getOutDirectory() != null) {
				try {
					// saver.saveUnified(this.names.get(i), parameters, diff,
					// this.getOutDirectory());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		if (actions.size() > 0)
			root.addProperty("status", "ok");
		else
			root.addProperty("status", "error");

		cacheResults.add(root);
		System.out.println("Output " + root.toString());
		handleResponse(httpExchange, root.toString());
	}

	public void retrieveFilesToAnalyze(HttpExchange httpExchange, MultiValueMap<String, String> queryParams)
			throws IOException {
		System.out.println("Load");

		String file = queryParams.get("file").get(0);

		try {
			System.out.println("Creating multiples trees: ");
			cacheResults = new JsonArray();
			createRepresention(httpExchange, file);

			handleResponse(httpExchange,
					"{status=created, operation=multiplecreate, pairs=" + this.filesToAnalyze.size() + "}");

		} catch (Exception e) {
			System.out.println("Error loading trees");
			e.printStackTrace();

			handleResponse(httpExchange, "error");
		}
	}

	public void createRepresention(HttpExchange httpExchange, String path) throws IOException {
		filesToAnalyze.clear();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();
			while (line != null) {

				File nFile = new File(line.trim());

				if (nFile.exists()) {
					filesToAnalyze.add(nFile);
				} else {
					System.out.println("Could not find file " + line);
				}

				// Next line
				line = reader.readLine();

			}
			reader.close();
			StructuredFolderfRunner runner = new StructuredFolderfRunner();

			ResponseGlobalBestParameter bestFromTraining = runner.summarizeBestGlobal(this.filesToAnalyze, METRIC.MEAN,
					false);
			this.valuesPerConfig = bestFromTraining.getValuesPerConfig();

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