package fr.gumtree.autotuning.fitness;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.github.gumtreediff.actions.Diff;

import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.outils.Constants;
import fr.gumtree.autotuning.searchengines.ResultByConfig;

public class LengthEditScriptFitness implements Fitness {

	@Override
	public Double getFitnessValue(SingleDiffResult aDiffResults, METRIC metric) {

		if (!aDiffResults.containsKey(Constants.NRACTIONS)) {
			return Double.MAX_VALUE;
		} else {
			int isize = (int) aDiffResults.get(Constants.NRACTIONS);

			if (isize == Integer.MAX_VALUE) {
				return Double.MAX_VALUE;
			} else {
				return new Double(isize);
			}
		}

	}

	@Override
	public List<?> getBests(List<ResultByConfig> all, METRIC metric) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double computeFitness(List<Double> values, METRIC metric) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (double aValue : values) {
			stats.addValue(aValue);
		}
		if (metric.equals(METRIC.MEAN))
			return stats.getMean();

		else if (metric.equals(METRIC.MEDIAN))
			return stats.getPercentile(50);

		System.err.println("Unknown metric: " + metric);
		return Double.MAX_VALUE;
	}

	@Override
	public Double getFitnessValue(Diff diff, METRIC metric) {

		if (diff == null) {
			return Double.MAX_VALUE;
		}

		int size = diff.editScript.asList().size();

		return new Double(size);
	}

}
