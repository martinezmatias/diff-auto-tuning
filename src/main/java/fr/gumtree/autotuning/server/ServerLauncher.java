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

	public static void main(String[] args) throws IOException {

		GumtreeSingleHttpHandler handler = new GumtreeSingleHttpHandler();

		HttpServer server = HttpServer.create(new InetSocketAddress(handler.getHost(), handler.getPort()), 0);

		server.createContext("/" + handler.getPath(), handler);

		GumtreeMultipleHttpHandler handlerMultiple = new GumtreeMultipleHttpHandler();

		server.createContext("/" + handlerMultiple.getPath(), handler);

		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		server.setExecutor(threadPoolExecutor);

		server.start();

		System.out.println("Server started on port " + handler.getPort());

	}

}
