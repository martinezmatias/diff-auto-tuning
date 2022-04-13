package fr.gumtree.autotuning.gumtree;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ExecutionTPEConfiguration extends ExecutionConfiguration {

	String pythonpath = "/Library/Frameworks/Python.framework/Versions/3.6/Resources/Python.app/Contents/MacOS/Python";
	String scriptpath = "/Users/matias/develop/gt-tuning/git-dat-experiment-runner/src/runners/TPEBridge.py";

	String classpath = System.getProperty("java.class.path");
	String javahome = System.getProperty("java.home");

	public String getPythonpath() {
		return pythonpath;
	}

	public void setPythonpath(String pythonpath) {
		this.pythonpath = pythonpath;
	}

	public String getScriptpath() {
		return scriptpath;
	}

	public void setScriptpath(String scriptpath) {
		this.scriptpath = scriptpath;
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

}
