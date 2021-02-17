package fr.gumtree.autotuning.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpServer;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ServerLauncher {

	private HttpServer server = null;

	public static void main(String[] args) throws IOException {

		ServerLauncher launcher = new ServerLauncher();

		launcher.start();

	}

	public boolean start() throws IOException {

		GumtreeSingleHttpHandler handler = new GumtreeSingleHttpHandler();
		GumtreeMultipleHttpHandler handlerMultiple = new GumtreeMultipleHttpHandler();

		server = HttpServer.create(new InetSocketAddress(handler.getHost(), handler.getPort()), 0);

		server.createContext("/" + handler.getPath(), handler);

		server.createContext("/" + handlerMultiple.getPath(), handlerMultiple);

		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		server.setExecutor(threadPoolExecutor);

		server.start();

		System.out.println("Server started on port " + handler.getPort());

		return true;

	}

	public void stop() throws IllegalAccessException {

		if (server == null) {
			throw new IllegalAccessException("Server is null");
		}
		this.server.stop(0);
	}

}
