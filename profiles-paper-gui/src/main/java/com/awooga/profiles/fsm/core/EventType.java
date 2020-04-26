package com.awooga.profiles.fsm.core;

import lombok.Value;

@FunctionalInterface
public interface EventType {
	public boolean shouldFire(String from, String to);

	@Value
	public static class FromState implements EventType {
		String state;

		public FromState(String state) {
			this.state = state;
		}

		@Override
		public boolean shouldFire(String from, String to) {
			if(from.equals(to)) {
				return false;
			}
			return from.equals(this.state);
		}
	}

	@Value
	public static class ToState implements EventType {
		String state;

		public ToState(String state) {
			this.state = state;
		}

		@Override
		public boolean shouldFire(String from, String to) {
			if(from.equals(to)) {
				return false;
			}
			return to.equals(this.state);
		}
	}

	@Value
	public static class ToAndFromState implements EventType {
		String from;
		String to;

		public ToAndFromState(String from, String to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean shouldFire(String from, String to) {
			return from.equals(this.from) && to.equals(this.to);
		}
	}


	@Value
	public static class All implements EventType {
		@Override
		public boolean shouldFire(String from, String to) {
			return true;
		}
	}
}
