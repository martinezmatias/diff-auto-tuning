package fr.gumtree.autotuning.gumtree;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import fr.gumtree.autotuning.fitness.Fitness;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ExecutionTPEConfiguration extends ExecutionConfiguration {

	String pythonpath = getPythonDir();

	private String getPythonDir() {

		String pythonRoot = System.getProperty("python.home");
		if (pythonRoot != null) {
			return pythonRoot;
		}

		return "Python";
	}

	String classpath = System.getProperty("java.class.path");
	String javahome = System.getProperty("java.home");
	String TPEScriptPath;

	int numberOfAttempts = 100;
	int randomseed = 0;

	public enum TPESearch {
		TPE, RANDOM
	}

	TPESearch searchType = TPESearch.TPE;

	public ExecutionTPEConfiguration(METRIC metric, ASTMODE astmode, Fitness fitnessFunction) throws Exception {
		super(metric, astmode, fitnessFunction);

		File fbrige = getFileFromResource("TPEBridge.py");
		TPEScriptPath = fbrige.getAbsolutePath();
	}

	public ExecutionTPEConfiguration(METRIC metric, ASTMODE astmode, Fitness fitnessFunction, String pythonpath,
			String scriptpath) {
		super(metric, astmode, fitnessFunction);
		this.pythonpath = pythonpath;
		this.TPEScriptPath = scriptpath;
	}

	public String getPythonpath() {
		return pythonpath;
	}

	public void setPythonpath(String pythonpath) {
		this.pythonpath = pythonpath;
	}

	public String getScriptpath() {
		return TPEScriptPath;
	}

	public void setScriptpath(String scriptpath) {
		this.TPEScriptPath = scriptpath;
	}

	public String getClasspath() {
		return classpath;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	public String getJavahome() {
		return javahome;
	}

	public void setJavahome(String javahome) {
		this.javahome = javahome;
	}

	public int getNumberOfAttempts() {
		return numberOfAttempts;
	}

	public void setNumberOfAttempts(int numberOfAttempts) {
		this.numberOfAttempts = numberOfAttempts;
	}

	public TPESearch getSearchType() {
		return searchType;
	}

	public void setSearchType(TPESearch searchType) {
		this.searchType = searchType;
	}

	public int getRandomseed() {
		return randomseed;
	}

	public void setRandomseed(int randomseed) {
		this.randomseed = randomseed;
	}

	private File getFileFromResource(String fileName) throws URISyntaxException {

		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource(fileName);
		if (resource == null) {
			throw new IllegalArgumentException("file not found! " + fileName);
		} else {
			return new File(resource.toURI());
		}

	}

}
