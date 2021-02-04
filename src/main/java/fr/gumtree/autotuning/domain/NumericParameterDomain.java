package fr.gumtree.autotuning.domain;

import fr.gumtree.autotuning.ParameterDomain;

/**
 * 
 * @author Matias Martinez
 *
 * @param <T>
 */
public class NumericParameterDomain<T extends Number> extends ParameterDomain<T> {

	protected T min;
	protected T max;
	protected T delta;

	public NumericParameterDomain(String id, Class type, T defaultValue, T min, T max, T delta) {
		super(id, type, defaultValue);
		this.min = min;
		this.max = max;
		this.delta = delta;
	}

	public NumericParameterDomain(String id, Class type, T defaultValue, T[] interval) {
		super(id, type, defaultValue, interval);
	}

	public T getMin() {
		return min;
	}

	public void setMin(T min) {
		this.min = min;
	}

	public T getMax() {
		return max;
	}

	public void setMax(T max) {
		this.max = max;
	}

	public T getDelta() {
		return delta;
	}

	public void setDelta(T delta) {
		this.delta = delta;
	}

	@Override
	public String toString() {
		return "ParametersDomain [id=" + id + ", type=" + type + ", defaultValue=" + defaultValue + ", min=" + min
				+ ", max=" + max + ", delta=" + delta + "]";
	}
}
