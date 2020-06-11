package fr.uphf.gpgt.example;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

//https://jenetics.io/manual/manual-5.2.0.pdf
public class ExampleJenetics {
	// 2.) Definition of the fitness function.
	private static int eval(Genotype<BitGene> gt) {
		return gt.chromosome().as(BitChromosome.class).bitCount();
	}

	public static void main(String[] args) {
		// 1.) Define the genotype (factory) suitable
		// for the problem.
		Factory<Genotype<BitGene>> gtf = Genotype.of(BitChromosome.of(10, 0.5));

		// 3.) Create the execution environment.
		Engine<BitGene, Integer> engine = Engine.builder(ExampleJenetics::eval, gtf).build();

		// 4.) Start the execution (evolution) and
		// collect the result.
		Genotype<BitGene> result = engine.stream().limit(100).collect(EvolutionResult.toBestGenotype());

		System.out.println("Hello World:\n" + result);
	}
}