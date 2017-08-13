
package net.comorevi.moneysland;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.plugin.PluginBase;

public class MoneySLand extends PluginBase {

    private SQLite3DataProvider sql;
    private static MoneySLand instance;
    private String messages[];

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

    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args){

        if(command.getName().equals("land")){

            if(sender instanceof ConsoleCommandSender){
                sender.sendMessage("MoneySLand>>注意 landコマンドはゲーム内からの実行のみ許可されています。");
                return true;
            }

            try{if(args[0] != null){}}
            catch(ArrayIndexOutOfBoundsException e){
                this.helpMessage(sender);
                return true;
            }

            String name = sender.getName().toLowerCase();

            switch(args[0]){
                case "start":
                    return true;

                case "end":
                    return true;

                case "buy":
                    return true;

                case "sell":
                    return true;

                case "invite":
                    return true;

                case "info":
                    return true;

                case "help":
                    this.helpMessage(sender);
                    return true;
            }

        }
        return false;
    }

    public void helpMessage(CommandSender sender){
        //適宜書いておいてください。
        //getMessage()は使用しなくても大丈夫だと思ったので。
        sender.sendMessage("MoneySLand>>ヘルプ");
    }
}