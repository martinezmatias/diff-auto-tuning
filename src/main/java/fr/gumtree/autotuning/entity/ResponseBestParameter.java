package fr.gumtree.autotuning.entity;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResponseBestParameter {

	private List<String> best = new ArrayList<String>();

	int numberOfEvaluatedPairs;
	double median;
	JsonArray infoEvaluations;

	@Override
	public String toString() {
		return "ResponseBestParameter [best=" + best + ", numberOfEvaluatedActions=" + numberOfEvaluatedPairs
				+ ", median=" + median + "]";
	}

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

	public double getMedian() {
		return median;
	}

	public void setMedian(double median) {
		this.median = median;
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
}
