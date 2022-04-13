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
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration;
import fr.gumtree.autotuning.server.DiffServerLauncher;
import fr.gumtree.autotuning.server.GumtreeAbstractHttpHandler;
import fr.gumtree.autotuning.server.GumtreeMultipleHttpHandler;
import fr.gumtree.autotuning.server.GumtreeSingleHttpHandler;

/**
 * Implementation of TPE
 * 
 * @author Matias Martinez
 *
 */
public class TPEEngine implements OptimizationMethod {

	private static final String HEADER_RESPONSE_PYTHON = "Best config: ";

	DiffServerLauncher launcher;

	public TPEEngine() {

	}

	@Override
	public ResponseBestParameter computeBestLocal(File left, File right, ExecutionConfiguration configuration)
			throws Exception {

		System.out.println("Starting server");
		launcher = new DiffServerLauncher();
		launcher.start();
		ResponseBestParameter resultGeneral = null;

		GumtreeSingleHttpHandler handler = launcher.getHandlerSimple();

		ASTMODE astmode = configuration.getAstmode();
		JsonObject responseJSon = launcher.initSimple(left, right, astmode, handler);

		resultGeneral = computeBestCallingTPE(resultGeneral, handler, responseJSon,
				(ExecutionTPEConfiguration) configuration);

		JsonArray infoEvaluations = this.launcher.retrieveInfoSimple();
		if (resultGeneral != null)
			resultGeneral.setInfoEvaluations(infoEvaluations);

		launcher.stop();
		System.out.println("End Simple");

		return resultGeneral;
	}

	@Override
	public ResponseBestParameter computeBestGlobal(File dataFilePairs, ExecutionConfiguration configuration)
			throws Exception {

		System.out.println("Starting server");
		launcher = new DiffServerLauncher();
		launcher.start();
		ResponseBestParameter resultGeneral = null;

		GumtreeMultipleHttpHandler handler = launcher.getHandlerMultiple();

		JsonObject responseJSon = launcher.initMultiple(dataFilePairs, configuration.getAstmode(), handler);

		resultGeneral = computeBestCallingTPE(resultGeneral, handler, responseJSon,
				(ExecutionTPEConfiguration) configuration);

		JsonArray infoEvaluations = this.launcher.retrieveInfoMultiple();
		if (resultGeneral != null)
			resultGeneral.setInfoEvaluations(infoEvaluations);

		launcher.stop();
		System.out.println("End Multiple");

		return resultGeneral;
	}

	public ResponseBestParameter computeBestCallingTPE(ResponseBestParameter resultGeneral,
			GumtreeAbstractHttpHandler handler, JsonObject responseJSon, ExecutionTPEConfiguration configuration)
			throws IOException, InterruptedException {
		String status = responseJSon.get("status").getAsString();

		handler.setOutDirectory(configuration.getDirDiffTreeSerialOutput());

		if ("created".equals(status)) {

			String best = queryBestConfigOnServer(handler, configuration);
			if (best != null) {

				System.out.println("Checking obtaining Best: ");
				JsonObject responseBest = launcher.callRunWithHandle(best, handler);
				System.out.println(responseBest);

				JsonObject responseJSonFromBest = new Gson().fromJson(responseBest, JsonObject.class);

				String checkedBestParameters = responseJSonFromBest.get("parameters").getAsString();

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

				if (configuration.getMetric().equals(METRIC.MEAN)) {
					result.setMetricValue(mean);
					result.setMetricUnit(METRIC.MEAN);
				} else {
					result.setMetricValue(median);
					result.setMetricUnit(METRIC.MEDIAN);
				}
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
	 * @param configuration
	 * @return
	 */
	public String queryBestConfigOnServer(GumtreeAbstractHttpHandler handler, ExecutionTPEConfiguration configuration) {
		// Call TPE

		Runtime rt = Runtime.getRuntime();

		// Create command
		String[] commandAndArguments = { configuration.getPythonpath(), configuration.getScriptpath(),
				configuration.getClasspath(), configuration.getJavahome(), handler.getHost(),
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
