package fr.gumtree.autotuning.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DoubleParameterDomain extends NumericParameterDomain<Double> {

	public DoubleParameterDomain(String id, Class type, Double defaultValue, Double min, Double max, Double delta) {
		super(id, type, defaultValue, min, max, delta);
	}

	public DoubleParameterDomain(String id, Class type, Double defaultValue, Double[] interval) {
		// TODO: retrieve min and max
		super(id, type, defaultValue, -1d, -1d, -1d);
		this.id = id;
		this.type = type;
		this.defaultValue = defaultValue;
		this.interval = interval;
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
