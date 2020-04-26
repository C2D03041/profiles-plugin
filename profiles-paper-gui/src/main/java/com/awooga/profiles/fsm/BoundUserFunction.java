package com.awooga.profiles.fsm;

@FunctionalInterface
public interface BoundUserFunction<U, S> {
	public void onEvent(U user, String stateName, S stateData);
}
