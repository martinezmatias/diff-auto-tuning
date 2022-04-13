package fr.gumtree.autotuning.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;

import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine.BestOfFile;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResponseBestParameter {

	int numberOfEvaluatedPairs;

	protected List<String> bests = new ArrayList<String>();

	protected Map<File, BestOfFile> resultPerFile = new HashMap<>();

	double metricValue = Integer.MIN_VALUE;
	METRIC metricUnit;

	JsonArray infoEvaluations;

	public String getBest() {
		if (bests.size() > 0)
			return bests.get(0);
		return null;
	}

	public List<String> getAllBest() {

		return this.bests;
	}

	public void setBest(String best) {
		this.bests = new ArrayList<String>();
		this.bests.add(best);
	}

	public int getNumberOfEvaluatedPairs() {
		return numberOfEvaluatedPairs;
	}

	public void setNumberOfEvaluatedPairs(int numberOfEvaluatedPairs) {
		this.numberOfEvaluatedPairs = numberOfEvaluatedPairs;
	}

	public List<String> getBests() {
		return bests;
	}

	public void setBests(List<String> bests) {
		this.bests = bests;
	}

	public double getMetricValue() {
		return metricValue;
	}

	public void setMetricValue(double metricValue) {
		this.metricValue = metricValue;
	}

	public METRIC getMetricUnit() {
		return metricUnit;
	}

	public void setMetricUnit(METRIC metricUnit) {
		this.metricUnit = metricUnit;
	}

	public Map<File, BestOfFile> getResultPerFile() {
		return resultPerFile;
	}

	public void setResultPerFile(Map<File, BestOfFile> resultPerFile) {
		this.resultPerFile = resultPerFile;
	}

	public JsonArray getInfoEvaluations() {
		return infoEvaluations;
	}

	public void setInfoEvaluations(JsonArray infoEvaluations) {
		this.infoEvaluations = infoEvaluations;
	}
}
