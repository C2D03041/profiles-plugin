package com.awooga.profiles.fsm;

import com.awooga.profiles.fsm.core.DumbFSM;
import com.awooga.profiles.fsm.core.EventType;
import com.awooga.profiles.fsm.core.Transition;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class BukkitEventFSM<S> extends EventFSM<S, Event, Player> implements Listener {
	@Inject
	Plugin plugin;

	Set<Class<?>> registeredClasses = new HashSet<>();

	protected abstract Map<EventType, BoundUserFunction<Player, S>> getBoundEvents();
	private Map<EventType, BoundFunction<S, Event>> cachedEventMap;

	public void onEnable() {
		for(Transition<S, Event> transition : this.getTransitions()) {
			Class<? extends Event> eventClass = transition.getOnEvent();
			if (registeredClasses.contains(eventClass)) {
				continue;
			}
			System.out.println("Registering event handler for: "+eventClass);
			registeredClasses.add(eventClass);
			Bukkit.getServer().getPluginManager().registerEvent(eventClass, this, EventPriority.NORMAL, this::eventExecutor, this.plugin);
		}
	}

	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		this.removeUser(event.getPlayer());
	}

	@SneakyThrows
	private void eventExecutor(@NotNull Listener listener, @NotNull Event event) {
		System.out.println("EventExecutor got "+event);
		if(event instanceof PlayerEvent) {
			this.fire(((PlayerEvent)event).getPlayer(), event);
		}
		else if(event instanceof InventoryInteractEvent) {
			InventoryInteractEvent invEvent = (InventoryInteractEvent) event;
			HumanEntity entity = invEvent.getWhoClicked();
			if (entity instanceof Player) {
				this.fire((Player) entity, event);
			}
		}
		else if(event instanceof InventoryCloseEvent) {
			InventoryCloseEvent invEvent = (InventoryCloseEvent) event;
			HumanEntity entity = invEvent.getPlayer();
			if (entity instanceof Player) {
				this.fire((Player) entity, event);
			}
		}
		else if(event instanceof InventoryOpenEvent) {
			InventoryOpenEvent invEvent = (InventoryOpenEvent) event;
			HumanEntity entity = invEvent.getPlayer();
			if (entity instanceof Player) {
				this.fire((Player) entity, event);
			}
		} else {
			throw new Exception("Don't know how to handle this event: "+event);
		}
	}

	protected Map<EventType, BoundFunction<S, Event>> getBoundPlainEvents() {
		if(this.cachedEventMap != null) {
			return this.cachedEventMap;
		}
		this.cachedEventMap = new HashMap<>();
		Map<EventType, BoundUserFunction<Player, S>> events = this.getBoundEvents();
		for(EventType event : events.keySet()) {
			BoundUserFunction<Player, S> fn = events.get(event);
			this.cachedEventMap.put(event, (fsm) -> {
				Player p = this.getUserByFSM(fsm);
				fn.onEvent(p, fsm.getStateName(), fsm.getStateData());
			});
		}
		return this.cachedEventMap;
	}

	protected S getStateByPlayer(Player user) {
		DumbFSM<S, Event> fsm = this.getFsm(user);
		if(fsm == null) { return null; }
		return fsm.getStateData();
	}
}
