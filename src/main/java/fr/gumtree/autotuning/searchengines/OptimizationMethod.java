package fr.gumtree.autotuning.searchengines;

import java.io.File;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.fitness.Fitness;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;

/**
 * Interface of the optimization method
 * 
 * @author Matias Martinez
 *
 */
public interface OptimizationMethod {

	/**
	 * * Compute the best from a list of pairs contained in a file given a AST mode
	 * TODO: change to Pairs File-File?
	 * 
	 * @param dataFilePairs
	 * @param astmode
	 * @return
	 * @throws Exception
	 */
	public ResponseBestParameter computeBestGlobal(File dataFilePairs, Fitness fitnessFunction,
			ExecutionConfiguration configuration) throws Exception;

	/**
	 * Compute the best from a pair of files. It does local search
	 * 
	 * @param dataFilePairs
	 * @return
	 * @throws Exception
	 */
	public ResponseBestParameter computeBestLocal(File left, File right, Fitness fitnessFunction,
			ExecutionConfiguration configuration) throws Exception;
}
