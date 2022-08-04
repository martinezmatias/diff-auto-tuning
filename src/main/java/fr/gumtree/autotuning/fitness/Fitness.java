package fr.gumtree.autotuning.fitness;

import java.util.List;

import com.github.gumtreediff.actions.Diff;

import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.searchengines.ResultByConfig;

/**
 * 
 * @author Matias Martinez
 *
 * @param <I>
 */
public interface Fitness {

	public Double getFitnessValue(Diff candidate, METRIC metric);

	public Double getFitnessValue(SingleDiffResult candidate, METRIC metric);

	public List<String> getBests(List<ResultByConfig> all, METRIC metric);

	public Double computeFitness(List<Double> values, METRIC metric);

}
