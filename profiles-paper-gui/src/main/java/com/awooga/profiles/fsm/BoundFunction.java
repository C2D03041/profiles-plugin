package com.awooga.profiles.fsm;

import com.awooga.profiles.fsm.core.DumbFSM;

@FunctionalInterface
public interface BoundFunction<S, E> {
	public void onEvent(DumbFSM<S, E> fsm);
}
