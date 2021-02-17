package fr.gumtree.autotuning.server;

import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class GumtreeAbstractHttpHandler implements HttpHandler {

	public abstract String getHost();

	public abstract int getPort();

	public abstract String getPath();

}
