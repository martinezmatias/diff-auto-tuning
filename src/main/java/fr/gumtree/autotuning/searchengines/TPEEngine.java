package fr.gumtree.autotuning.searchengines;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.fitness.Fitness;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration;
import fr.gumtree.autotuning.server.DiffServerLauncher;
import fr.gumtree.autotuning.server.GumtreeAbstractHttpHandler;
import fr.gumtree.autotuning.server.GumtreeCacheHttpHandler;
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
	public ResponseBestParameter computeBestLocal(File left, File right, Fitness fitnessFunction,
			ExecutionConfiguration configuration) throws Exception {

		System.out.println("Starting server");
		launcher = new DiffServerLauncher(fitnessFunction, configuration.getMetric());
		launcher.start();
		ResponseBestParameter resultGeneral = null;

		GumtreeSingleHttpHandler handler = launcher.getHandlerSimple();

		ASTMODE astmode = configuration.getAstmode();
		JsonObject responseJSon = launcher.initSimple(left, right, astmode);

		resultGeneral = computeBestCallingTPE(resultGeneral, handler, responseJSon,
				(ExecutionTPEConfiguration) configuration);

		JsonArray infoEvaluations = this.launcher.retrieveInfoSimple();
		if (resultGeneral != null)
			resultGeneral.setInfoEvaluations(infoEvaluations);

		launcher.stop();
		System.out.println("End Simple");

		return resultGeneral;
	}

	@Deprecated // not used, only in test?
	@Override
	public ResponseBestParameter computeBestGlobal(File dataFilePairs, Fitness fitnessFunction,
			ExecutionConfiguration configuration) throws Exception {

		System.out.println("Starting server");
		launcher = new DiffServerLauncher(fitnessFunction, configuration.getMetric());
		launcher.start();
		ResponseBestParameter resultGeneral = null;

		GumtreeMultipleHttpHandler handler = launcher.getHandlerMultiple();

		JsonObject responseJSon = launcher.initMultiple(dataFilePairs, configuration.getAstmode());

		resultGeneral = computeBestCallingTPE(resultGeneral, handler, responseJSon,
				(ExecutionTPEConfiguration) configuration);

		JsonArray infoEvaluations = this.launcher.retrieveInfoMultiple();
		if (resultGeneral != null)
			resultGeneral.setInfoEvaluations(infoEvaluations);

		launcher.stop();
		System.out.println("End Multiple");

		return resultGeneral;
	}

	public ResponseBestParameter computeBestGlobalCache(File dataFilePairs, Fitness fitnessFunction,
			ExecutionConfiguration configuration) throws Exception {

		System.out.println("Starting server");
		GumtreeCacheHttpHandler handler = new GumtreeCacheHttpHandler(fitnessFunction, configuration.getMetric());
		launcher = new DiffServerLauncher(new GumtreeSingleHttpHandler(fitnessFunction, configuration.getMetric()),
				handler);
		launcher.start();

		ResponseBestParameter resultGeneral = new ResponseBestParameter();

		JsonObject responseJSon = launcher.initMultiple(dataFilePairs, configuration.getAstmode());

		resultGeneral = computeBestCallingTPE(resultGeneral, handler, responseJSon,
				(ExecutionTPEConfiguration) configuration);

		// JsonArray infoEvaluations = this.launcher.retrieveInfoMultiple();
		//// MM temp code ignored:
		//// if (resultGeneral != null)
		//// resultGeneral.setInfoEvaluations(infoEvaluations);

		launcher.stop();
		System.out.println("End server");

		return resultGeneral;
	}

	/**
	 * New
	 * 
	 * @param dataFilePairs
	 * @param fitnessFunction
	 * @param configuration
	 * @return
	 * @throws Exception
	 */
	public ResponseBestParameter computeLocalGlobalCache(File dataFilePairs, ExecutionConfiguration configuration,
			GumtreeCacheHttpHandler handler, DiffServerLauncher launcher) throws Exception {

		ResponseBestParameter resultGeneral = new ResponseBestParameter();

		JsonObject responseJSon = launcher.initMultiple(dataFilePairs, configuration.getAstmode());

		this.launcher = launcher;

		resultGeneral = computeBestCallingTPE(resultGeneral, handler, responseJSon,
				(ExecutionTPEConfiguration) configuration);

		//
		return resultGeneral;
	}

	public ResponseBestParameter computeBestCallingTPE(ResponseBestParameter resultGeneral,
			GumtreeAbstractHttpHandler handlerP, JsonObject responseJSon, ExecutionTPEConfiguration configuration)
			throws IOException, InterruptedException {
		String status = responseJSon.get("status").getAsString();

		handlerP.setOutDirectory(configuration.getDirDiffTreeSerialOutput());

		if ("created".equals(status)) {

			// TPE always returns only one
			String[] response = queryBestConfigOnServer(handlerP, configuration);
			
					
			if (response != null) {

				String best = response[0];
				String environment = response[1];
				
				System.out.println("Checking obtaining Best: ");
				JsonObject responseBest = launcher.callRunWithHandle(best, handlerP);
				System.out.println(responseBest);

				JsonObject responseJSonFromBest = new Gson().fromJson(responseBest, JsonObject.class);

				String checkedBestParameters = responseJSonFromBest.get("parameters").getAsString();

				Double fitness = responseJSonFromBest.get("fitness").getAsDouble();
				Integer values = responseJSonFromBest.get("values").getAsInt();

				//
				// Retrieve values for the default an create a ResultsByConfig
				ResponseBestParameter result = new ResponseBestParameter();
				result.setBest(checkedBestParameters);
				// result.setNumberOfEvaluatedPairs(nrActions);
				result.setMetricValue(fitness);
				result.setNumberOfEvaluatedPairs(values);
				
				//
				result.setEnvironment(environment);

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
	public String[] queryBestConfigOnServer(GumtreeAbstractHttpHandler handler, ExecutionTPEConfiguration configuration) {
		// Call TPE

		Runtime rt = Runtime.getRuntime();

		// Create command
		String[] commandAndArguments = { configuration.getPythonpath(), configuration.getScriptpath(),
				configuration.getClasspath(), configuration.getJavahome(), handler.getHost(),
				Integer.toString(handler.getPort()), handler.getPath(), HEADER_RESPONSE_PYTHON,
				configuration.getSearchType().name().toLowerCase(),
				Integer.toString(configuration.getNumberOfAttempts()),
				Integer.toString(configuration.getRandomseed()) };
		try {
			Process p = rt.exec(commandAndArguments);
			
			//String response = readProcessOutput(p, HEADER_RESPONSE_PYTHON);

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			String bestConfig = null;
			String configLine = null;
			while ((line = reader.readLine()) != null && bestConfig == null) {
				// response += line + "\r\n";

				if (line.startsWith("Famework_Setup")) {
			
					configLine = line;
				} 

				if (line.startsWith(HEADER_RESPONSE_PYTHON)) {

					bestConfig = line.replace(HEADER_RESPONSE_PYTHON, "");

				}

			}
			
			reader.close();
			if (bestConfig!= null)
					bestConfig = bestConfig.replace(HEADER_RESPONSE_PYTHON, "");
			
			System.out.println("best: " + bestConfig);
			
			String error = readProcessError(p);
			System.err.println(error);
		

			return new String[] {bestConfig, configLine} ;

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
		// System.out.println(response);
		return last;
	}

	private String readProcessOutput(Process p, String toFind) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		String response = null;
		String configLine = null;
		while ((line = reader.readLine()) != null && response == null) {
			// response += line + "\r\n";

			if (line.startsWith("Famework_Setup")) {
		
				configLine = line;
			} 

			if (line.startsWith(HEADER_RESPONSE_PYTHON)) {

				response = line.replace(HEADER_RESPONSE_PYTHON, "");

			}

		}
		reader.close();
		
		System.out.println("-Config->"+ configLine);
		return response;
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
