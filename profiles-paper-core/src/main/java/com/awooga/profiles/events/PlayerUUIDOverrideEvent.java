package com.awooga.profiles.events;


import lombok.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Called when a player has changed servers.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
@Builder
public class PlayerUUIDOverrideEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	@Getter
	private UUID originalUuid;
	@Getter
	private UUID currentUuid;
	@Getter
	private Player player;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}