package fr.uphf;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.gumtreediff.matchers.ConfigurationOptions;

public class IntParameterDomain extends ParameterDomain<Integer> {

	public IntParameterDomain(ConfigurationOptions id, Class type, Integer defaultValue, Integer min, Integer max,
			Integer delta) {
		super(id, type, defaultValue, min, max, delta);

	}

	public IntParameterDomain(ConfigurationOptions id, Class type, Integer defaultValue, Integer min, Integer max) {
		super(id, type, defaultValue, min, max);

	}

	public IntParameterDomain(ConfigurationOptions id, Class type, Integer defaultValue, Integer[] interval) {

		super(id, type, defaultValue, interval[0], interval[interval.length - 1], interval);

	}

	@Override
	public Integer[] computeInterval() {
		if (interval != null) {
			return interval;
		} else {

			List<Integer> r = this.iterateStream(this.min, this.delta, this.max);

			Integer[] arrayRange = new Integer[r.size()];
			r.toArray(arrayRange);

			return arrayRange;
		}

	}

	List<Integer> iterateStream(int from, int step, int limit) {
		return IntStream.iterate(from, i -> i + step).limit(limit / step).boxed().collect(Collectors.toList());
	}
}
