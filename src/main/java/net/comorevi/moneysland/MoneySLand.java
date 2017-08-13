
package net.comorevi.moneysland;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;

public class MoneySLand extends PluginBase {

	private static MoneySLand instance;
	private SQLite3DataProvider sql;
	
	public static MoneySLand getInstance() {
		return instance;
	}
	
	public /*わからない*/ getLand(int x, int z, String world) {
		return sql.getLand(x, z, world);
	}
	
	public boolean existsLand(int x, int z, String World) {
		return sql.existsLand(x, z, world);
	}
	
	public void createLand(String owner, int[] start, int[] end, String world) {
		sql.createLand(owner, start[0], start[1], end[0], end[1], world);
	}
	
	public boolean isEditable(int x, int z, String world, Player player){
        if(player.isOp())return true;
        int land = this.getSQL().getLand(x, z, world);
        if(land == 0){
            return !(this.isWorldProtect());
        }

        String name = player.getName().toLowerCase();

        /* わからん()
        if(land["owner"].equals(name)){
            return true;
        }
        */

        //return this.getSQL().existsGuest(land["id"], name);
        return false;//仮。エラー回避
    }
	
	public boolean isWorldProtect(String world) {
		return false;
	}
	
	@Override
	public void onEnable() {
		getDataFolder().mkdir();
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
	}
	
	public boolean checkOverLap(int[] start, int[] end, String world) {
		
	}
	
	public String getMessage(String str, int[] param) {
		
	}
	
	public String parseMessage(String message) {
		
	}
	
	public int calculateLandPrice(String name) {
		
	}
	
	public void helpMessage(CommandSender sender) {
		
	}
	
	public SQLite3DataProvider getSQL() {
		return this.sql;
	}

}