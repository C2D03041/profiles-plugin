package com.awooga.profiles.fsm;

import com.awooga.profiles.fsm.core.DumbFSM;
import com.awooga.profiles.fsm.core.EventType;
import com.awooga.profiles.fsm.core.Transition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.*;
import java.util.function.BiFunction;

public abstract class EventFSM<S, E, U> {
	protected abstract String getDefaultStateName();
	protected abstract S getDefaultStateData();
	protected abstract List<Transition<S,E>> getTransitions();
	protected abstract Map<EventType, BoundFunction<S, E>> getBoundPlainEvents();

	BidiMap<U, DumbFSM<S, E>> stateByUser = new DualHashBidiMap<>();

	protected U getUserByFSM(DumbFSM<S, E> fsm) {
		return stateByUser.getKey(fsm);
	}

	protected DumbFSM<S, E> getFsm(U user) {
		if(!stateByUser.containsKey(user)) {
			DumbFSM<S, E> fsm = DumbFSM.<S, E>builder()
				.stateName(this.getDefaultStateName())
				.stateData(this.getDefaultStateData())
				.transitionSupplier(this::getTransitions)
			.build();
			stateByUser.put(user, fsm);
		}
		return stateByUser.get(user);
	}

	public void removeUser(U user) {
		this.stateByUser.remove(user);
	}

	public void fire(U user, E event) {
		DumbFSM<S, E> fsm = this.getFsm(user);
		String from = fsm.getStateName();
		S fromState = fsm.getStateData();
		//System.out.println("Got from fsm"+fsm+" - "+fsm.getStateName()+" - "+fsm.getStateData());
		fsm.fire(event);
		String to = fsm.getStateName();
		S toState = fsm.getStateData();

		if(from.equals(to) && fromState.equals(toState)) {
			return;
		}

		//System.out.println("DETECTED STATE TRANSITION - "+from+"->"+to+" stateData: "+fsm.getStateData());
		final Map<EventType, BoundFunction<S, E>> events = this.getBoundPlainEvents();

		if(events == null) {
			System.out.println("getBoundPlainEvents() returned null?");
			return;
		}

		events.forEach((eventType, function) -> {
			//System.out.println("Checking shouldfire "+eventType+" fn="+function);
			if(eventType.shouldFire(from, to)) {
				function.onEvent(fsm);
			}
		});
	}
}
