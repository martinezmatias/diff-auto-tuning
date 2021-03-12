package fr.gumtree.autotuning.searchengines;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.server.GumtreeAbstractHttpHandler;
import fr.gumtree.autotuning.server.ServerLauncher;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TPEEngine implements SearchMethod {

	private static final String HEADER_RESPONSE_PYTHON = "Best config: ";

	ServerLauncher launcher;

	String pythonpath = "/Library/Frameworks/Python.framework/Versions/3.6/Resources/Python.app/Contents/MacOS/Python";
	String scriptpath = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/src/runners/TPEBridge.py";

	String classpath = System.getProperty("java.class.path");
	String javahome = System.getProperty("java.home");

	public TPEEngine(String pythonpath, String scriptpath) {
		this.pythonpath = pythonpath;
		this.scriptpath = scriptpath;
	}

	public TPEEngine() {
	}

	public ResponseBestParameter computeBestLocal(File left, File right) throws Exception {
		return computeBestLocal(left, right, ASTMODE.GTSPOON);
	}

	public ResponseBestParameter computeBestLocal(File left, File right, ASTMODE astmode) throws Exception {

		System.out.println("Starting server");
		launcher = new ServerLauncher();
		launcher.start();
		ResponseBestParameter resultGeneral = null;

		GumtreeAbstractHttpHandler handler = launcher.getHandlerSimple();

		JsonObject responseJSon = launcher.initSimple(left, right, astmode);

		resultGeneral = processOutput(resultGeneral, handler, responseJSon);

		JsonArray infoEvaluations = this.launcher.retrieveInfoSimple();
		if (resultGeneral != null)
			resultGeneral.setInfoEvaluations(infoEvaluations);

		launcher.stop();
		System.out.println("End Simple");

		return resultGeneral;
	}

	public ResponseBestParameter computeBestGlobal(File dataFilePairs) throws Exception {
		return computeBestGlobal(dataFilePairs, ASTMODE.GTSPOON);
	}

	public ResponseBestParameter computeBestGlobal(File dataFilePairs, ASTMODE astmode) throws Exception {

		System.out.println("Starting server");
		launcher = new ServerLauncher();
		launcher.start();
		ResponseBestParameter resultGeneral = null;

		GumtreeAbstractHttpHandler handler = launcher.getHandlerMultiple();

		JsonObject responseJSon = launcher.initMultiple(dataFilePairs, astmode);

		resultGeneral = processOutput(resultGeneral, handler, responseJSon);

		JsonArray infoEvaluations = this.launcher.retrieveInfoMultiple();
		if (resultGeneral != null)
			resultGeneral.setInfoEvaluations(infoEvaluations);

		launcher.stop();
		System.out.println("End Multiple");

		return resultGeneral;
	}

	public ResponseBestParameter processOutput(ResponseBestParameter resultGeneral, GumtreeAbstractHttpHandler handler,
			JsonObject responseJSon) throws IOException, InterruptedException {
		String status = responseJSon.get("status").getAsString();
		if ("created".equals(status)) {

			String best = queryBestConfigOnServer(handler);
			if (best != null) {

				System.out.println("Checking obtaining Best: ");
				JsonObject responseBest = launcher.callWithHandle(best, handler);
				System.out.println(responseBest);

				JsonObject responseJSonFromBest = new Gson().fromJson(responseBest, JsonObject.class);

				String checkedBestParameters = responseJSonFromBest.get("parameters").getAsString();

				System.out.println("");

				JsonArray actionsArray = responseJSonFromBest.get("actions").getAsJsonArray();

				int nrActions = actionsArray.size();

				ResponseBestParameter result = new ResponseBestParameter();
				result.setBest(checkedBestParameters);
				result.setNumberOfEvaluatedPairs(nrActions);

				DescriptiveStatistics stats = new DescriptiveStatistics();

				for (JsonElement action : actionsArray) {
					int nractions = action.getAsJsonObject().get("nractions").getAsInt();
					stats.addValue(nractions);
				}

				double mean = stats.getMean();
				double std = stats.getStandardDeviation();
				double median = stats.getPercentile(50);
				result.setMedian(median);

				resultGeneral = result;

			}

		} else {
			System.out.println("Operation unknown " + status);
		}
		// Stop server
		return resultGeneral;
	}

	/**
	 * 
	 * @param handler
	 * @return
	 */
	public String queryBestConfigOnServer(GumtreeAbstractHttpHandler handler) {
		// Call TPE

		Runtime rt = Runtime.getRuntime();

		// Create command
		String[] commandAndArguments = { pythonpath, scriptpath, classpath, javahome, handler.getHost(),
				Integer.toString(handler.getPort()), handler.getPath(), HEADER_RESPONSE_PYTHON };
		try {
			Process p = rt.exec(commandAndArguments);
			String response = readProcessOutput(p);

			String error = readProcessError(p);
			String bestConfig = null;
			if (response.startsWith(HEADER_RESPONSE_PYTHON)) {

				bestConfig = response.replace(HEADER_RESPONSE_PYTHON, "");
			}

			System.err.println(error);
			System.out.println("best: " + bestConfig);

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
