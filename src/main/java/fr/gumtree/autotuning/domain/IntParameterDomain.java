package fr.gumtree.autotuning.domain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.gumtreediff.matchers.ConfigurationOptions;

/**
 * 
 * @author Matias Martinez
 *
 */
public class IntParameterDomain extends NumericParameterDomain<Integer> {

	public IntParameterDomain(ConfigurationOptions id, Class type, Integer defaultValue, Integer min, Integer max,
			Integer delta) {
		super(id, type, defaultValue, min, max, delta);

	}

	public IntParameterDomain(ConfigurationOptions id, Class type, Integer defaultValue, Integer[] interval) {
		super(id, type, defaultValue, defaultValue, defaultValue, defaultValue);
		this.id = id;
		this.type = type;
		this.defaultValue = defaultValue;
		this.interval = interval;
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
