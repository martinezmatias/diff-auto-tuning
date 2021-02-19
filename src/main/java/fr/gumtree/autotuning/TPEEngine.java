package fr.gumtree.autotuning;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import com.google.gson.JsonObject;

import fr.gumtree.autotuning.server.GumtreeAbstractHttpHandler;
import fr.gumtree.autotuning.server.ServerLauncher;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TPEEngine {

	ServerLauncher launcher;

	String pythonpath = "/Library/Frameworks/Python.framework/Versions/3.6/Resources/Python.app/Contents/MacOS/Python";
	String scriptpath = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/src/runners/TPEBridge.py";

	String classpath = System.getProperty("java.class.path");
	String javahome = System.getProperty("java.home");

	public String computeBest(File left, File right) throws Exception {
		// Init server
		;
		System.out.println("Starting server");
		launcher = new ServerLauncher();
		launcher.start();

		// Call Load

		GumtreeAbstractHttpHandler handler = launcher.getHandlerSimple();

		JsonObject responseJSon = launcher.initSimple(left, right);

		String status = responseJSon.get("status").getAsString();
		if ("created".equals(status)) {

			String best = queryBestConfig(handler);
			if (best != null) {

				JsonObject responseBest = launcher.call(best);
				System.out.println(responseBest);
			}

			return best;

		} else {
			System.out.println("Operation unknown " + status);
		}
		// Stop server

		launcher.stop();
		System.out.println("End");

		return null;
	}

	public String queryBestConfig(GumtreeAbstractHttpHandler handler) {
		// Call TPE

		Runtime rt = Runtime.getRuntime();
		String[] commandAndArguments = { pythonpath, scriptpath, classpath, javahome, handler.getHost(),
				Integer.toString(handler.getPort()), handler.getPath() };
		try {
			Process p = rt.exec(commandAndArguments);
			String response = readProcessOutput(p);

			String error = readProcessError(p);
			String bestConfig = null;
			if (response.startsWith("Best config: ")) {

				bestConfig = response.replace("Best config: ", "");
			}

			System.err.println(error);
			System.out.println(bestConfig);

			return bestConfig;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private String readProcessOutput(Process p) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String response = "";
		String line;
		String last = "";
		while ((line = reader.readLine()) != null) {
			response += line + "\r\n";
			last = line;
		}
		reader.close();
		System.out.println(response);
		return last;
	}

	private String readProcessError(Process p) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String response = "";
		String line;
		while ((line = reader.readLine()) != null) {
			response += line + "\r\n";
		}
		reader.close();
		return response;
	}

}
