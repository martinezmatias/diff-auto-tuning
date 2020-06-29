package fr.gumtree.autotuning;

import org.junit.Test;

public class MainTest {

	@Test
	public void testMain1() {
		String[] command = new String[] {
				"-path=/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded", //
				"-out=./out/", //
				"-stop=1", //
				"-subset=1", "-parallel=true" };
		Main.main(command);
	}

	@Test
	public void testMain2() {
		String[] command = new String[] {
				"-path=/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded", //
				"-out=./out/", //
				"-stop=1", //
				"-astmodel=JDT", //
				"-subset=1", "-parallel=true" };
		Main.main(command);
	}

	@Test
	public void testMain3() {
		String[] command = new String[] {
				"-path=/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded", //
				"-out=./out/", //
				"-stop=1", //
				"-subset=1", "-timeout=10" };
		Main.main(command);
	}
}
