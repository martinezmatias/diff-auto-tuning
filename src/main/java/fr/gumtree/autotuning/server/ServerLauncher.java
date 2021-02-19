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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import fr.gumtree.autotuning.TuningEngine.ASTMODE;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ServerLauncher {

	private HttpServer server = null;

	private GumtreeSingleHttpHandler handlerSimple;
	private GumtreeMultipleHttpHandler handlerMultiple;

	public static void main(String[] args) throws IOException {

		ServerLauncher launcher = new ServerLauncher();

		launcher.start();

	}

	public boolean start() throws IOException {

		handlerSimple = new GumtreeSingleHttpHandler();
		handlerMultiple = new GumtreeMultipleHttpHandler();

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

	public JsonObject callWithHandle(String param, GumtreeAbstractHttpHandler handle)
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

		System.out.println("-->" + res);
		System.out.println(convertedObject);
		return convertedObject;

	}

	public JsonObject callMultiple(String param) throws IOException, InterruptedException {

		return callWithHandle(param, handlerMultiple);

	}

	public JsonObject call(String param) throws IOException, InterruptedException {

		return callWithHandle(param, handlerSimple);
	}

	public JsonObject initSimple(File fs, File ft) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();

		GumtreeSingleHttpHandler handle = new GumtreeSingleHttpHandler();
		URI create = URI.create(
				"http://" + handle.getHost() + ":" + handle.getPort() + "/" + handle.getPath() + "?action=load&model="
						+ ASTMODE.GTSPOON + "&left=" + fs.getAbsolutePath() + "&right=" + ft.getAbsolutePath());

		System.out.println(create);

		HttpRequest request = HttpRequest.newBuilder().uri(create).build();
		HttpResponse<String> d = client.send(request, BodyHandlers.ofString());

		String res = d.body();
		System.out.println("-->" + res);
		JsonObject responseJSon = new Gson().fromJson(res, JsonObject.class);
		return responseJSon;
	}

	public JsonObject initMultiple(File fs) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();

		GumtreeMultipleHttpHandler handle = new GumtreeMultipleHttpHandler();

		URI create = URI.create("http://" + handle.getHost() + ":" + handle.getPort() + "/" + handle.getPath()
				+ "?action=load&model=" + ASTMODE.GTSPOON + "&file=" + fs.getAbsolutePath());

		System.out.println(create);

		HttpRequest request = HttpRequest.newBuilder().uri(create).build();
		HttpResponse<String> d = client.send(request, BodyHandlers.ofString());

		String res = d.body();

		System.out.println("-->" + res);

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
