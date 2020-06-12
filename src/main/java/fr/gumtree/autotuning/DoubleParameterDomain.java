package fr.gumtree.autotuning;

import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.matchers.ConfigurationOptions;

public class DoubleParameterDomain extends ParameterDomain<Double> {

	public DoubleParameterDomain(ConfigurationOptions id, Class type, Double defaultValue, Double min, Double max) {
		super(id, type, defaultValue, min, max);

	}

	public DoubleParameterDomain(ConfigurationOptions id, Class type, Double defaultValue, Double min, Double max,
			Double delta) {
		super(id, type, defaultValue, min, max, delta);
	}

	public DoubleParameterDomain(ConfigurationOptions id, Class type, Double defaultValue, Double[] interval) {
		super(id, type, defaultValue, interval[0], interval[interval.length - 1], interval);
	}

	@Override
	public Double[] computeInterval() {
		if (interval != null) {
			return interval;
		} else {

			List<Double> r = this.iterateStream(this.min, this.delta, this.max);

			Double[] arrayRange = new Double[r.size()];
			r.toArray(arrayRange);

			return arrayRange;
		}

	}

	List<Double> iterateStream(Double from, Double step, Double limit) {

		List<Double> dlist = new ArrayList<>();
		for (Double i = from; i <= limit; i += step) {
			i = Math.floor(i * 1000) / 1000;
			dlist.add(i);
		}
		return dlist;

	}
}
