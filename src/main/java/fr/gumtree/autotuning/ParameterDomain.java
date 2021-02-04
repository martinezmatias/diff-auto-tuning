package fr.gumtree.autotuning;

/**
 * 
 * @author Matias Martinez
 */
public class ParameterDomain<T> {

	protected String id;
	protected Class type;
	protected T defaultValue;
	protected T[] interval;

	public ParameterDomain(String id, Class type, T defaultValue) {
		super();
		this.id = id;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public ParameterDomain(String id, Class type, T defaultValue, T[] interval) {
		super();
		this.id = id;
		this.type = type;
		this.defaultValue = defaultValue;
		this.interval = interval;
	}

	public T[] computeInterval() {
		return interval;

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

}
