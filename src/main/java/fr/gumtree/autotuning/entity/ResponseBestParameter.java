package fr.gumtree.autotuning.entity;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResponseBestParameter {

	String best;
	int numberOfEvaluatedPairs;
	double median;

	@Override
	public String toString() {
		return "ResponseBestParameter [best=" + best + ", numberOfEvaluatedActions=" + numberOfEvaluatedPairs
				+ ", median=" + median + "]";
	}

	public String getBest() {
		return best;
	}

	public void setBest(String best) {
		this.best = best;
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
}
