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
		System.out.println("va r" +System.getenv("path"));
		String pythonRoot = System.getProperty("python.home");
		if (pythonRoot != null) {
			return pythonRoot;
		}

		return "/Users/matias/miniconda3/envs/torch-gpu/bin/python";
	}

	String classpath = System.getProperty("java.class.path");
	String javahome = System.getProperty("java.home");
	String TPEScriptPath;

	int numberOfAttempts = 100;
	int randomseed = 0;
	
	public enum HPOFramework{
		
		HYPEROPT, OPTUNA, HYPEROPTFG;
		
	}

	public enum HPOSearchType {
		// Modes from Hyperopt
		TPE_HYPEROPT(HPOFramework.HYPEROPT), RANDOM_HYPEROPT(HPOFramework.HYPEROPT), ADAPTIVE(HPOFramework.HYPEROPT),
		Annealing(HPOFramework.HYPEROPT), TPE_HYPEROPTFG(HPOFramework.HYPEROPTFG),
		
		//Modes from Optuna
		GRID(HPOFramework.OPTUNA), RANDOM_OPTUNA(HPOFramework.OPTUNA), TPE_OPTUNA(HPOFramework.OPTUNA), CMAES(HPOFramework.OPTUNA), PARTIALFIXED(HPOFramework.OPTUNA), NSGAII(HPOFramework.OPTUNA), QMC(HPOFramework.OPTUNA);
		
		private final HPOFramework framework;
		
		HPOSearchType(HPOFramework fm){
			this.framework=fm;
		}

		public HPOFramework getFramework() {
			return framework;
		}

	}
	
	

	HPOSearchType searchType;

	
	public ExecutionTPEConfiguration(METRIC metric, ASTMODE astmode, Fitness fitnessFunction, HPOSearchType searchType) throws Exception {
		super(metric, astmode, fitnessFunction);
		this.searchType = searchType;
		
		String frameworkname = searchType.getFramework() == HPOFramework.HYPEROPT?  "HyperOptBridge.py" : "OptunaBridge.py";
		
		if (searchType.getFramework() == HPOFramework.HYPEROPT) {
			frameworkname  =   "HyperOptBridge.py";
		}else
			if (searchType.getFramework() == HPOFramework.OPTUNA) {
				frameworkname =  "OptunaBridge.py";
			}else
				if (searchType.getFramework() == HPOFramework.HYPEROPTFG) {
					frameworkname  =   "HyperOptBridgeFineGrain.py";
			}
		
		File fbrige = getFileFromResource( frameworkname);
		TPEScriptPath = fbrige.getAbsolutePath();
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

	public HPOSearchType getSearchType() {
		return searchType;
	}

	public void setSearchType(HPOSearchType searchType) {
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
