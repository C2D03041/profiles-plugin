package com.awooga.profiles.fsm.core;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Builder
public class DumbFSM<S, E> {
	@Getter
	private String stateName;
	@Getter
	private S stateData;

	protected Supplier<List<Transition<S, E>>> transitionSupplier;

	public void fire(E event) {
		List<Transition<S, E>> transitions = this.transitionSupplier.get().stream()
			.filter(transition -> transition.getOnEvent().equals(event.getClass()))
			.filter(transition -> this.stateName.equals(transition.getFrom()))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		;
		if(transitions.size() == 0) {
			return;
		}
		for(Transition<S,E> transition : transitions) {
			OnTransitLambda<S, E> transitFn = transition.getOnTransit();
			S newState = null == transitFn ?
				this.stateData :
				transitFn.onTransit(this.stateName, transition.getTo(), this.stateData, event);
			if (newState != null) {
				//System.out.println("Updating stateName to: " + transition.getTo());
				this.stateData = newState;
				this.stateName = transition.getTo();
				return;
			}
		}
	}
}
