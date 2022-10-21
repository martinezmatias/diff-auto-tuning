package fr.gumtree.autotuning.entity;

public class BestOfFile {

	double minBest;
	double minDefault;

	public BestOfFile(double minBest, double minDefault) {
		super();

		this.minBest = minBest;
		this.minDefault = minDefault;
	}

	public double getMinBest() {
		return minBest;
	}

	public void setMinBest(double minBest) {
		this.minBest = minBest;
	}

	public double getMinDefault() {
		return minDefault;
	}

	public void setMinDefault(double minDefault) {
		this.minDefault = minDefault;
	}

	@Override
	public String toString() {
		return "BestOfFile [minBest=" + minBest + ", minDefault=" + minDefault + "]";
	}
}