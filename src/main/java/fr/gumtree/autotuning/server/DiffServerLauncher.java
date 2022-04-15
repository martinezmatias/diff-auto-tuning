package fr.gumtree.autotuning.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import fr.gumtree.autotuning.fitness.Fitness;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DiffServerLauncher {

	private HttpServer server = null;

	private GumtreeSingleHttpHandler handlerSimple;
	private GumtreeMultipleHttpHandler handlerMultiple;

	Fitness fitnessFunction;
	METRIC metric;

	public DiffServerLauncher(GumtreeSingleHttpHandler handlerSimple, GumtreeMultipleHttpHandler handlerMultiple) {
		super();
		this.handlerSimple = handlerSimple;
		this.handlerMultiple = handlerMultiple;
	}

	public DiffServerLauncher(Fitness fitnessFunction, METRIC metric) {
		super();
		handlerSimple = new GumtreeSingleHttpHandler(fitnessFunction, metric);
		handlerMultiple = new GumtreeMultipleHttpHandler(fitnessFunction, metric);
	}

	public boolean start() throws IOException {

		server = HttpServer.create(new InetSocketAddress(handlerSimple.getHost(), handlerSimple.getPort()), 0);

		server.createContext("/" + handlerSimple.getPath(), handlerSimple);

		server.createContext("/" + handlerMultiple.getPath(), handlerMultiple);

		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		server.setExecutor(threadPoolExecutor);

		server.start();

		System.out.println("Server started on port " + handlerSimple.getPort());

		return true;

	}

	public void stop() throws IllegalAccessException {

		if (server == null) {
			throw new IllegalAccessException("Server is null");
		}
		this.server.stop(0);
	}

	public JsonObject callRunWithHandle(String param, GumtreeAbstractHttpHandler handle)
			throws IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();

		URI create = URI.create("http://" + handle.getHost() + ":" + handle.getPort() + "/" + handle.getPath()
				+ "?action=run&parameters=" + param + "&out=./out");

		System.out.println(create);

		HttpRequest request = HttpRequest.newBuilder().uri(create).build();
		HttpResponse<String> responseRequest = client.send(request, BodyHandlers.ofString());

		String res = responseRequest.body();
		System.out.println(res);
		JsonObject convertedObject = new Gson().fromJson(res, JsonObject.class);

		// System.out.println("-->" + res);
		System.out.println(convertedObject);
		return convertedObject;

	}

	public JsonObject callMultiple(String param) throws IOException, InterruptedException {

		return callRunWithHandle(param, handlerMultiple);

	}

	public JsonObject call(String param) throws IOException, InterruptedException {

		return callRunWithHandle(param, handlerSimple);
	}

	public JsonObject initSimple(File fs, File ft, ASTMODE astmode) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();

		GumtreeSingleHttpHandler handle = this.handlerSimple;

		// GumtreeSingleHttpHandler handle = new GumtreeSingleHttpHandler();
		URI create = URI.create("http://" + handle.getHost() + ":" + handle.getPort() + "/" + handle.getPath()
				+ "?action=load&model=" + astmode + "&left=" + fs.getAbsolutePath() + "&right=" + ft.getAbsolutePath());

		System.out.println(create);

		HttpRequest request = HttpRequest.newBuilder().uri(create).build();
		HttpResponse<String> d = client.send(request, BodyHandlers.ofString());

		String res = d.body();
		// System.out.println("-->" + res);
		JsonObject responseJSon = new Gson().fromJson(res, JsonObject.class);
		return responseJSon;
	}

	public JsonArray retrieveInfoSimple() throws IOException, InterruptedException {

		// GumtreeSingleHttpHandler handle = new GumtreeSingleHttpHandler();

		return retrieveInfo(handlerSimple);
	}

	public JsonArray retrieveInfoMultiple() throws IOException, InterruptedException {

		// GumtreeMultipleHttpHandler handle = new GumtreeMultipleHttpHandler();

		return retrieveInfo(this.handlerMultiple);
	}

	public JsonArray retrieveInfo(GumtreeAbstractHttpHandler handle) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();

		// GumtreeSingleHttpHandler handle = new GumtreeSingleHttpHandler();
		URI create = URI.create(
				"http://" + handle.getHost() + ":" + handle.getPort() + "/" + handle.getPath() + "?action=info");

		System.out.println(create);

		HttpRequest request = HttpRequest.newBuilder().uri(create).build();
		HttpResponse<String> d = client.send(request, BodyHandlers.ofString());

		String res = d.body();
		// System.out.println("-->" + res);
		JsonArray responseJSon = new Gson().fromJson(res, JsonArray.class);
		return responseJSon;
	}

	public JsonObject initMultiple(File fs, ASTMODE astomode) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();

		GumtreeMultipleHttpHandler handle = this.handlerMultiple;

		URI create = URI.create("http://" + handle.getHost() + ":" + handle.getPort() + "/" + handle.getPath()
				+ "?action=load&model=" + astomode + "&file=" + fs.getAbsolutePath());

		System.out.println(create);

		HttpRequest request = HttpRequest.newBuilder().uri(create).build();
		HttpResponse<String> d = client.send(request, BodyHandlers.ofString());

		String res = d.body();

		// System.out.println("-->" + res);

		JsonObject jsonResponse = new JsonParser().parse(res).getAsJsonObject();
		return jsonResponse;
	}

	public GumtreeSingleHttpHandler getHandlerSimple() {
		return handlerSimple;
	}

	public GumtreeMultipleHttpHandler getHandlerMultiple() {
		return handlerMultiple;
	}

}
