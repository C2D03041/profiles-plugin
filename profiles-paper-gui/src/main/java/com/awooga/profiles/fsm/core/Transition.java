package com.awooga.profiles.fsm.core;

import com.awooga.profiles.fsm.core.OnTransitLambda;
import lombok.Builder;
import lombok.Value;

import java.util.function.Function;

@Value
@Builder
public class Transition<S, E> {
	String from;
	String to;
	Class<? extends E> onEvent;
	OnTransitLambda<S, E> onTransit;
}
