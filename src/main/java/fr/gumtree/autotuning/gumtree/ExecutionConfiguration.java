package fr.gumtree.autotuning.gumtree;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import fr.gumtree.autotuning.searchengines.ExhaustiveEngine.PARALLEL_EXECUTION;

/**
 * Stores the properties
 * 
 * @author Matias Martinez
 *
 */
public class ExecutionConfiguration extends HashMap<Object, Object> {

	private PARALLEL_EXECUTION paralelisationMode = PARALLEL_EXECUTION.NONE;
	private int numberOfThreads = 16;
	private long timeOut = 60 * 60; // 60 min

	public enum METRIC {
		MEDIAN, MEAN
	}

	private METRIC metric = METRIC.MEDIAN;

	TimeUnit timeUnit = TimeUnit.SECONDS;

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
		return timeOut;
	}

	public void setTimeOut(long timeOutSeconds) {
		this.timeOut = timeOutSeconds;
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
}
