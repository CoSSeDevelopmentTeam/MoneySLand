
package net.comorevi.moneysland;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;

public class MoneySLand extends PluginBase {

    private SQLite3DataProvider sql;
    private static MoneySLand instance;

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

    public static MoneySLand getInstance(){
        return instance;
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

    public boolean isWorldProtect(){
        return false;
    }
}