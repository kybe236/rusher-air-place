package org.kybe;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

public class Main extends Plugin {
	
	@Override
	public void onLoad() {
		final GrimAirPlace grimAirPlace = new GrimAirPlace();
		RusherHackAPI.getModuleManager().registerFeature(grimAirPlace);

	}
	
	@Override
	public void onUnload() {
	}
	
}