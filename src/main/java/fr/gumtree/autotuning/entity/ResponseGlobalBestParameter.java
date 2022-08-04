package fr.gumtree.autotuning.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.gumtree.autotuning.searchengines.ResultByConfig;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResponseGlobalBestParameter extends ResponseBestParameter {

	ResultByConfig valuesPerConfig;
	// private Collection<String> allConfigs;
	private Map<String, Double> metricValueByConfiguration = new HashMap<>();

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

	public void setNumberOfEvaluatedPairs(int numberOfEvaluatedActions) {
		this.numberOfEvaluatedPairs = numberOfEvaluatedActions;
	}

	public void setBest(List<String> best) {
		this.bests = best;
	}

	@Override
	public String toString() {
		return "ResponseBestParameter [numberOfEvaluatedPairs=" + numberOfEvaluatedPairs + ", metricValue="
				+ metricValue + ", metricUnit=" + metricUnit + ", best=" + bests + "]";
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
