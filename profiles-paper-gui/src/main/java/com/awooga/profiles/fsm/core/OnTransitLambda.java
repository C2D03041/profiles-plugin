package com.awooga.profiles.fsm.core;

@FunctionalInterface
public interface OnTransitLambda<S, E> {
	public S onTransit(String from, String to, S state, E event);
}