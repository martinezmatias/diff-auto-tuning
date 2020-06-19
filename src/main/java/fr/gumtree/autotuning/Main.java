package fr.gumtree.autotuning;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;

import fr.gumtree.autotuning.TuningEngine.ASTMODE;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/**
 * 
 * @author Matias Martinez
 *
 */
public class Main implements Callable<Integer> {

	@Option(names = "-out", required = true)
	String out;
	@Option(names = "-path", required = true)
	File pathMegadiff;
	@Option(names = "-subset", required = true)
	int[] subsets;
	@Option(names = "-begin")
	int begin;
	@Option(names = "-stop", defaultValue = "10000000")
	int stop;
	@Option(names = "-astmodel", required = false, defaultValue = "GTSPOON")
	String astmodel;
	@Option(names = "-parallel", defaultValue = "true")
	boolean parallel;

	public static void main(String[] args) {
		System.out.println("Command: " + Arrays.toString(args));
		Main m = new Main();
		CommandLine cl = new CommandLine(m);
		cl.execute(args);
	}

	public String getOut() {
		return out;
	}

	public void setOut(String out) {
		this.out = out;
	}

	public File getPath() {
		return pathMegadiff;
	}

	public void setPath(File path) {
		this.pathMegadiff = path;
	}

	public int[] getSubsets() {
		return subsets;
	}

	public void setSubsets(int[] subsets) {
		this.subsets = subsets;
	}

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getStop() {
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}

	public String getAstmodel() {
		return astmodel;
	}

	public void setAstmodel(String astmodel) {
		this.astmodel = astmodel;
	}

	public boolean isParallel() {
		return parallel;
	}

	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}

	@Override
	public Integer call() throws Exception {

		System.out.println("out:  " + toString());

		ASTMODE mode = ASTMODE.valueOf(this.astmodel);

		TuningEngine engine = new TuningEngine();
		engine.navigateMegaDiff(out, pathMegadiff, subsets, begin, stop, mode, parallel);
		System.out.println("-END-");
		return null;
	}

	@Override
	public String toString() {
		return "Main [out=" + out + ", pathMegadiff=" + pathMegadiff + ", subsets=" + Arrays.toString(subsets)
				+ ", begin=" + begin + ", stop=" + stop + ", astmodel=" + astmodel + ", parallel=" + parallel + "]";
	}

}
