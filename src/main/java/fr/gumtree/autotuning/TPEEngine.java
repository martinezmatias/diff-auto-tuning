package fr.gumtree.autotuning;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

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

	public void run(File left, File right) throws Exception {

		Map<String, String> bestConfiguration = computeBest(left, right);

	}

	public Map<String, String> computeBest(File left, File right) throws Exception {
		// Init server

		System.out.println("Starting server");
		launcher = new ServerLauncher();
		launcher.start();

		// Call Load

		// Call TPE
		System.out.println();
		String classpath = System.getProperty("java.class.path");
		String javahome = System.getProperty("java.home");

		Runtime rt = Runtime.getRuntime();
		String[] commandAndArguments = { pythonpath, scriptpath, classpath, left.getAbsolutePath(),
				right.getAbsolutePath(), javahome };
		try {
			Process p = rt.exec(commandAndArguments);
			String response = readProcessOutput(p);
			System.out.println(response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Stop server

		launcher.stop();
		System.out.println("End");

		return null;
	}

	private String readProcessOutput(Process p) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String response = "";
		String line;
		while ((line = reader.readLine()) != null) {
			response += line + "\r\n";
		}
		reader.close();
		return response;
	}

}
