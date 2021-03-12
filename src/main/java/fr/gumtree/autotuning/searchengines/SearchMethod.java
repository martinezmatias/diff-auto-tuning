package fr.gumtree.autotuning.searchengines;

import java.io.File;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;

public interface SearchMethod {

	/**
	 * Compute the best from a list of pairs contained in a file TODO: change to
	 * Pairs File-File?
	 * 
	 * @param dataFilePairs
	 * @return
	 * @throws Exception
	 */
	public ResponseBestParameter computeBestGlobal(File dataFilePairs) throws Exception;

	/**
	 * * Compute the best from a list of pairs contained in a file given a AST mode
	 * TODO: change to Pairs File-File?
	 * 
	 * @param dataFilePairs
	 * @param astmode
	 * @return
	 * @throws Exception
	 */
	public ResponseBestParameter computeBestGlobal(File dataFilePairs, ASTMODE astmode,
			ExecutionConfiguration configuration) throws Exception;

	/**
	 * Compute the best from a pair of files. It does local search
	 * 
	 * @throws Exception
	 */
	public ResponseBestParameter computeBestLocal(File left, File right) throws Exception;

	/**
	 * Compute the best from a pair of files. It does local search
	 * 
	 * @param dataFilePairs
	 * @return
	 * @throws Exception
	 */
	public ResponseBestParameter computeBestLocal(File left, File right, ASTMODE astmode,
			ExecutionConfiguration configuration) throws Exception;
}
