package fr.gumtree.autotuning.fitness;

import java.util.ArrayList;
import java.util.Collections;
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
	public Double computeFitness(List<Double> values, METRIC metric) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (double aValue : values) {
			stats.addValue(aValue);
		}
		
		if (metric.equals(METRIC.MEAN))
			return stats.getMean();
		else if (metric.equals(METRIC.MEDIAN))
			return stats.getPercentile(50); //median(values);// stats.getPercentile(50);
		else if (metric.equals(METRIC.PERCENTILE75))
			return stats.getPercentile(75); 
	
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

	public static double median(List<Double> valuesOriginal) {

		List<Double> values = new ArrayList<>(valuesOriginal);
		Collections.sort(values);

		if (values.size() % 2 == 1)
			return values.get((values.size() + 1) / 2 - 1);
		else {
			double lower = values.get(values.size() / 2 - 1);
			double upper = values.get(values.size() / 2);

			return (lower + upper) / 2.0;
		}
	}

}
