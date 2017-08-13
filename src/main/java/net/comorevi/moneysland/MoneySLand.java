package net.comorevi.moneysland;

import cn.nukkit.plugin.PluginBase;
import net.comorevi.moneysland.SQLite3DataProvider;

public class MoneySLand extends PluginBase {
	
	private static MoneySLand instance;
	private SQLite3DataProvider sql;
	
	public static MoneySLand getInstance() {
		return instance;
	}
	
	public Land getLand(int x, int z, String world) {
		return sql.getLand(x, z, world);
	}
	
	public boolean existsLand(int x, int z, String World) {
		return sql.existsLand(x, z, world);
	}
	
	public void createLand(String owner, int[] start, int[] end, String world) {
		sql.createLand(owner, start[0], start[1], end[0], end[1], world);
	}
	
	@Override
	public void onEnable() {
		getDataFolder().mkdir();
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public SQLite3DataProvider getSQL() {
		return this.sql;
	}
}
