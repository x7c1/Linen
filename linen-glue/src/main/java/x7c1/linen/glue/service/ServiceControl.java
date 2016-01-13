package x7c1.linen.glue.service;

public interface ServiceControl {
	void startServiceOf(ServiceLabel label);
	Class<?> getClassOf(ServiceLabel label);
}
