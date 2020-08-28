package com.awooga.profiles.dao;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder(toBuilder = true)
@Value
public class ProfileEntity {
	Long id;
	UUID playerUuid;
	UUID profileUuid;
	boolean deleted;
	String cachedPlaceholderTitle;
	String cachedPlaceholderBody;
}
