package fr.gumtree.autotuning.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;

import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.searchengines.ResultByConfig;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResponseBestParameter {

	private List<String> best = new ArrayList<String>();

	int numberOfEvaluatedPairs;
	double metricValue = Integer.MIN_VALUE;
	METRIC metricUnit;
	ResultByConfig valuesPerConfig;
	private Collection<String> allConfigs;
	private Map<String, Double> metricValueByConfiguration = new HashMap<>();

	JsonArray infoEvaluations;

	public String getBest() {
		if (best.size() > 0)
			return best.get(0);
		return null;
	}

	public List<String> getAllBest() {

		return this.best;
	}

	public void setBest(String best) {
		this.best = new ArrayList<String>();
		this.best.add(best);
	}

	public int getNumberOfEvaluatedPairs() {
		return numberOfEvaluatedPairs;
	}

	public void setNumberOfEvaluatedPairs(int numberOfEvaluatedActions) {
		this.numberOfEvaluatedPairs = numberOfEvaluatedActions;
	}

	public JsonArray getInfoEvaluations() {
		return infoEvaluations;
	}

	public void setInfoEvaluations(JsonArray infoEvaluations) {
		this.infoEvaluations = infoEvaluations;
	}

	public void setBest(List<String> best) {
		this.best = best;
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

	public Collection<String> getAllConfigs() {
		return allConfigs;
	}

	public void setAllConfigs(Collection<String> allConfigs) {
		this.allConfigs = allConfigs;
	}

	@Override
	public String toString() {
		return "ResponseBestParameter [numberOfEvaluatedPairs=" + numberOfEvaluatedPairs + ", metricValue="
				+ metricValue + ", metricUnit=" + metricUnit + ", best=" + best + "]";
	}

	public Map<String, Double> getMetricValueByConfiguration() {
		return metricValueByConfiguration;
	}

	public void setMetricValueByConfiguration(Map<String, Double> metricValueByConfiguration) {
		this.metricValueByConfiguration = metricValueByConfiguration;
	}

	public ResultByConfig getValuesPerConfig() {
		return valuesPerConfig;
	}

	public void setValuesPerConfig(ResultByConfig valuesPerConfig) {
		this.valuesPerConfig = valuesPerConfig;
	}
}
