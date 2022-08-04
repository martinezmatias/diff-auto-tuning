package fr.gumtree.autotuning.gumtree;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import fr.gumtree.autotuning.fitness.Fitness;
import fr.gumtree.autotuning.fitness.LengthEditScriptFitness;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine.PARALLEL_EXECUTION;

/**
 * Stores the properties
 * 
 * @author Matias Martinez
 *
 */
public abstract class ExecutionConfiguration extends HashMap<Object, Object> {

	private PARALLEL_EXECUTION paralelisationMode = PARALLEL_EXECUTION.NONE;
	private int numberOfThreads = 16;

	private boolean saveScript = true;

	private boolean overwriteResults = false;

	private File dirDiffTreeSerialOutput = new File("./out/");

	public enum METRIC {
		MEDIAN, MEAN
	}

	private METRIC metric;

	public ExecutionConfiguration(METRIC metric, ASTMODE astmode, Fitness fitnessFunction) {
		super();
		this.metric = metric;
		this.astmode = astmode;
		this.fitnessFunction = fitnessFunction;
	}

	private long timeOutDiffExecution = 1000;
	TimeUnit timeUnitDiffExecution = TimeUnit.MILLISECONDS;

	private long timeOutD = 60; // 60 min
	TimeUnit timeUnit = TimeUnit.MINUTES;

	private ASTMODE astmode;

	Fitness fitnessFunction = new LengthEditScriptFitness();

	public PARALLEL_EXECUTION getParalelisationMode() {
		return paralelisationMode;
	}

	public void setParalelisationMode(PARALLEL_EXECUTION paralelisationMode) {
		this.paralelisationMode = paralelisationMode;
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public long getTimeOut() {
		return timeOutDiffExecution;
	}

	public void setTimeOut(long timeOutSeconds) {
		this.timeOutDiffExecution = timeOutSeconds;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public METRIC getMetric() {
		return metric;
	}

	public void setMetric(METRIC metric) {
		this.metric = metric;
	}

	public boolean isSaveScript() {
		return saveScript;
	}

	public void setSaveScript(boolean saveScript) {
		this.saveScript = saveScript;
	}

	public File getDirDiffTreeSerialOutput() {
		return dirDiffTreeSerialOutput;
	}

	public void setDirDiffTreeSerialOutput(File dirDiffTreeSerialOutput) {
		this.dirDiffTreeSerialOutput = dirDiffTreeSerialOutput;
	}

	public boolean isOverwriteResults() {
		return overwriteResults;
	}

	public void setOverwriteResults(boolean overwriteResults) {
		this.overwriteResults = overwriteResults;
	}

	public ASTMODE getAstmode() {
		return astmode;
	}

	public void setAstmode(ASTMODE astmode) {
		this.astmode = astmode;
	}

	@Override
	public String toString() {
		return "ExecutionConfiguration [astmode=" + astmode + ", paralelisationMode=" + paralelisationMode
				+ ", numberOfThreads=" + numberOfThreads + ", metric=" + metric + ", timeOut=" + timeOutDiffExecution
				+ ", timeUnit=" + timeUnit + "]";
	}

	public long getTimeOutDiffExecution() {
		return timeOutDiffExecution;
	}

	public void setTimeOutDiffExecution(long timeOutDiffExecution) {
		this.timeOutDiffExecution = timeOutDiffExecution;
	}

	public TimeUnit getTimeUnitDiffExecution() {
		return timeUnitDiffExecution;
	}

	public void setTimeUnitDiffExecution(TimeUnit timeUnitDiffExecution) {
		this.timeUnitDiffExecution = timeUnitDiffExecution;
	}

	public Fitness getFitnessFunction() {
		return fitnessFunction;
	}

	public void setFitnessFunction(Fitness fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}
}
