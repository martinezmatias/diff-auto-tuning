package fr.gumtree.autotuning;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.gumtreediff.matchers.ConfigurationOptions;

//TODO: this class can be move to GumTree
public class ParameterDomain<T extends Number> {

	protected ConfigurationOptions id;
	protected Class type;
	protected T defaultValue;
	protected T min;
	protected T max;
	protected T delta;
	protected T[] interval;

	public ParameterDomain(ConfigurationOptions id, Class type, T defaultValue, T min, T max, T delta) {
		super();
		this.id = id;
		this.type = type;
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		this.delta = delta;
	}

	public ParameterDomain(ConfigurationOptions id, Class type, T defaultValue, T min, T max) {
		super();
		this.id = id;
		this.type = type;
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
	}

	public ParameterDomain(ConfigurationOptions id, Class type, T defaultValue, T min, T max, T[] interval) {
		super();
		this.id = id;
		this.type = type;
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		this.interval = interval;
	}

	public T[] computeInterval() {
		if (interval != null) {
			return interval;
		} else {
			return null;
		}

	}

	List<Integer> iterateStream(int from, int step, int limit) {
		return IntStream.iterate(from, i -> i + step) // next int
				.limit(limit / step) // only numbers in range
				.boxed().collect(Collectors.toList());
	}

	public ConfigurationOptions getId() {
		return id;
	}

	public void setId(ConfigurationOptions id) {
		this.id = id;
	}

	public Class getType() {
		return type;
	}

	public void setType(Class type) {
		this.type = type;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
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
