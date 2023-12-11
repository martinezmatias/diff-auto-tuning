package fr.gumtree.autotuning;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.fitness.LengthEditScriptFitness;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.gumtree.ExecutionExhaustiveConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration.HPOSearchType;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine.PARALLEL_EXECUTION;
import fr.gumtree.autotuning.searchengines.OptimizationMethod;
import fr.gumtree.autotuning.searchengines.TPEEngine;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/**
 * 
 * @author Matias Martinez
 *
 */
public class Main implements Callable<Integer> {

	@Option(names = "-out", required = false)
	String out;
	@Option(names = "-astmodel", required = false, defaultValue = "GTSPOON")
	String astmodel;
	@Option(names = "-paralleltype", defaultValue = "PROPERTY_LEVEL")
	String paralleltype;

	@Option(names = "-mode", defaultValue = "exahustive")
	String mode;

	@Option(names = "-scope", defaultValue = "local")
	String scope;

	@Option(names = "-nrAttempts", defaultValue = "25")
	int nrAttempts;

	@Option(names = "-nrthreads", defaultValue = "10")
	int nrthreads;
	// in seconds
	@Option(names = "-timeout", defaultValue = "3000", descriptionKey = "timeout for a matcher (all config) in seconds")
	long timeout;
	@Option(names = "-matchers", required = false)
	String[] matchers;
	@Option(names = "-overwriteresults", defaultValue = "true", required = false)
	boolean overwriteresults;

	// Inputs for local
	@Option(names = "-left", required = false)
	String left;

	@Option(names = "-right", required = false)
	String right;

	// Inout for global
	@Option(names = "-listpairs", required = false)
	String listpairs;

	enum DAT_METHOD {
		EXHAUSTIVE, TPE_HYPEROPT, TPE_OPTUNA;
	}

	enum SCOPE {
		GLOBAL, LOCAL;
	}

	public OptimizationMethod getMethod() {

		OptimizationMethod engine = null;
		if (mode.toLowerCase().equals(DAT_METHOD.EXHAUSTIVE.toString().toLowerCase())) {
			engine = new ExhaustiveEngine();
		} else if (mode.toLowerCase().equals(DAT_METHOD.TPE_HYPEROPT.toString().toLowerCase())) {
			engine = new TPEEngine();
		}
		return engine;

	}

	public ExecutionConfiguration getConfiguration() throws Exception {

		METRIC mean = METRIC.MEAN;

		if (mode.toLowerCase().equals(DAT_METHOD.EXHAUSTIVE.toString().toLowerCase())) {

			ExecutionExhaustiveConfiguration configuration = new ExecutionExhaustiveConfiguration(mean, getMetamodel(),
					getFitness());
			configuration.setNumberOfThreads(nrthreads);
			configuration.setTimeOut(timeout);
			PARALLEL_EXECUTION execution = PARALLEL_EXECUTION.valueOf(this.paralleltype.toUpperCase());

			configuration.setParalelisationMode(execution);
			configuration.setOverwriteResults(overwriteresults);
			return configuration;

		} else if (mode.toLowerCase().equals(DAT_METHOD.TPE_HYPEROPT.toString().toLowerCase())) {

			ExecutionTPEConfiguration configuration = new ExecutionTPEConfiguration(mean, getMetamodel(), getFitness(),HPOSearchType.TPE_HYPEROPT );
			configuration.setNumberOfAttempts(nrAttempts);

			return configuration;
		}
		else if (mode.toLowerCase().equals(DAT_METHOD.TPE_OPTUNA.toString().toLowerCase())) {

			ExecutionTPEConfiguration configuration = new ExecutionTPEConfiguration(mean, getMetamodel(), getFitness(),HPOSearchType.TPE_OPTUNA);
			configuration.setNumberOfAttempts(nrAttempts);

			return configuration;
		}
		return null;

	}

	private LengthEditScriptFitness getFitness() {
		return new LengthEditScriptFitness();
	}

	@Override
	public Integer call() throws Exception {

		ExecutionConfiguration configuration = getConfiguration();
		OptimizationMethod method = getMethod();
		LengthEditScriptFitness fitness = getFitness();
		ResponseBestParameter result = null;
		if (scope.toLowerCase().equals(SCOPE.GLOBAL.toString().toLowerCase())) {

			if (listpairs == null) {
				throw new IllegalArgumentException("parameter -listpair must be passed");
			}

			File flistpairs = new File(listpairs);
			if (!flistpairs.exists()) {
				throw new IllegalArgumentException("parameters -flistpairs must have existing files");
			}

			result = method.computeBestGlobal(flistpairs, fitness, configuration);

		} else if (scope.toLowerCase().equals(SCOPE.LOCAL.toString().toLowerCase())) {

			if (left == null || right == null) {
				throw new IllegalArgumentException("parameters -left and -right must be passed");
			}

			File fl = new File(left);
			File fr = new File(right);

			if (!fl.exists() || !fr.exists()) {
				throw new IllegalArgumentException("parameters -left and -right must have existing files");
			}

			result = method.computeBestLocal(fl, fr, fitness, configuration);

		}

		if (result != null) {
			System.out.println("End search, best found: ");
			System.out.println(result.getBest());
		}

		return null;
	}

	private ASTMODE getMetamodel() {
		ASTMODE model = ASTMODE.valueOf(this.astmodel);
		return model;
	}

	public static void main(String[] args) {
		System.out.println("Arguments received: " + Arrays.toString(args));
		Main m = new Main();
		m.execute(args);
	}

	public void execute(String[] args) {
		CommandLine cl = new CommandLine(this);
		cl.execute(args);
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public String getAstmodel() {
		return astmodel;
	}

	public void setAstmodel(String astmodel) {
		this.astmodel = astmodel;
	}

	public String getParallel() {
		return paralleltype;
	}

	public void setParallel(String parallel) {
		this.paralleltype = parallel;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String[] getMatchers() {
		return matchers;
	}

	public void setMatchers(String[] matchers) {
		this.matchers = matchers;
	}

}
