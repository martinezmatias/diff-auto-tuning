package fr.gumtree.autotuning;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import fr.gumtree.autotuning.entity.CaseResult;
import fr.gumtree.autotuning.experimentrunner.MegadiffRunner;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine.PARALLEL_EXECUTION;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;
import fr.gumtree.autotuning.treebuilder.JDTTreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/**
 * 
 * @author Matias Martinez
 *
 */
public class Main implements Callable<Integer> {

	@Option(names = "-out", required = true)
	String out;
	@Option(names = "-path", required = true)
	File pathMegadiff;
	@Option(names = "-subset", required = true)
	int[] subsets;
	@Option(names = "-begin")
	int begin;
	@Option(names = "-stop", defaultValue = "10000000")
	int stop;
	@Option(names = "-astmodel", required = false, defaultValue = "GTSPOON")
	String astmodel;
	@Option(names = "-paralleltype", defaultValue = "NONE")
	String paralleltype;

	@Option(names = "-nrthreads", defaultValue = "10")
	int nrthreads;

	// in seconds
	@Option(names = "-timeout", defaultValue = "3000", descriptionKey = "timeout for a matcher (all config) in seconds")
	long timeout;
	@Option(names = "-matchers", required = false)
	String[] matchers;
	@Option(names = "-overwriteresults", defaultValue = "false", required = false)
	boolean overwriteresults;

	List<CaseResult> resultsExecution;

	public static void main(String[] args) {
		System.out.println("Arguments received: " + Arrays.toString(args));
		Main m = new Main();
		m.execute(args);
	}

	public static List<CaseResult> mainAndResults(String[] args) {
		System.out.println("Arguments received: " + Arrays.toString(args));
		Main m = new Main();
		m.execute(args);
		return m.getResultsExecution();
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

	public File getPath() {
		return pathMegadiff;
	}

	public void setPath(File path) {
		this.pathMegadiff = path;
	}

	public int[] getSubsets() {
		return subsets;
	}

	public void setSubsets(int[] subsets) {
		this.subsets = subsets;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getStop() {
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
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

	@Override
	public Integer call() throws Exception {

		System.out.println("Command:  " + toString());

		ExhaustiveEngine engine = new ExhaustiveEngine();

		MegadiffRunner megadiff = new MegadiffRunner(engine);
		// engine.setOverwriteResults(overwriteresults);

		PARALLEL_EXECUTION execution = PARALLEL_EXECUTION.valueOf(this.paralleltype.toUpperCase());

		ExecutionConfiguration configuration = new ExecutionConfiguration();
		configuration.setNumberOfThreads(nrthreads);
		configuration.setTimeOut(timeout);
		configuration.setParalelisationMode(execution);

		ASTMODE model = ASTMODE.valueOf(this.astmodel);
		ITreeBuilder treebuilder = null;
		if (ASTMODE.GTSPOON.equals(model)) {
			treebuilder = new SpoonTreeBuilder();
		} else if (ASTMODE.JDT.equals(model)) {
			treebuilder = new JDTTreeBuilder();
		} else {
			System.err.println("Mode not configured " + model);
		}

		// We store the results of the execution
		this.resultsExecution = megadiff.navigateMegaDiff(treebuilder, out, pathMegadiff, subsets, begin, stop,
				configuration, this.matchers);

		System.out.println("-END-");
		return null;
	}

	@Override
	public String toString() {
		return "Main [out=" + out + ", pathMegadiff=" + pathMegadiff + ", subsets=" + Arrays.toString(subsets)
				+ ", begin=" + begin + ", stop=" + stop + ", astmodel=" + astmodel + ", parallel=" + paralleltype
				+ ", timeout=" + timeout + ", matchers=" + Arrays.toString(this.matchers) + ", overwriteresults="
				+ this.overwriteresults + ", nrThreads=" + this.nrthreads + "]";
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

	public List<CaseResult> getResultsExecution() {
		return resultsExecution;
	}

}
