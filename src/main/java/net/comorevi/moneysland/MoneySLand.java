
package net.comorevi.moneysland;

import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

public class MoneySLand extends PluginBase {

    private SQLite3DataProvider sql;
    private static MoneySLand instance;
    private String messages[];
    private Config translateFile;
    private Map<String, Object> configData;

    /**************/
    /** Plug関連  */
    /**************/

    public static MoneySLand getInstance() {
        return instance;
    }

    public SQLite3DataProvider getSQL() {
        return this.sql;
    }

    /**************/
    /** Land関連  */
    /**************/

    /*
    public わからない getLand(int x, int z, String world) {
        return sql.getLand(x, z, world);
    }
    */

    public boolean existsLand(int x, int z, String world) {
        //return sql.existsLand(x, z, world);
    }

    public void createLand(String owner, int[] start, int[] end, String world) {
        sql.createLand(owner, start[0], start[1], end[0], end[1], world);
    }

    public boolean isWorldProtect(String world) {
        return false;
    }

    public boolean isWorldProtect(){
        return false;
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

    /**************/
    /** 金銭関連  */
    /**************/

    public int calculateLandPrice(String name) {
        return 0;
    }

    public boolean checkOverLap(int[] start, int[] end, String world) {
        return false;
    }

    /**************/
    /** 起動関連  */
    /**************/

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {

    }

    /**************/
    /** コマンド  */
    /**************/

    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args){

        if(command.getName().equals("land")){

            if(sender instanceof ConsoleCommandSender){
                sender.sendMessage(TextValues.WARNING + this.translateString("send-command-console"));
                return true;
            }

            try{if(args[0] != null){}}
            catch(ArrayIndexOutOfBoundsException e){
                this.helpMessage(sender);
                return true;
            }

            String name = sender.getName().toLowerCase();

            Player p = (Player)sender;

            switch(args[0]){
                case "start":
                    int x = (int)p.getX();
                    int z = (int)p.getZ();
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

    /****************/
    /** メッセージ **/
    /****************/

    public void helpMessage(CommandSender sender){
        //適宜書いておいてください。
        //getMessage()は使用しなくても大丈夫だと思ったので。
        sender.sendMessage(TextValues.HELP);
    }

    public String parseMessage(String message) {
        return "";
    }

    public void initMessageConfig(){
        this.translateFile = new Config();
        this.translateFile.load(this.getClass().getClassLoader().getResourceAsStream("Message.yml"));
        this.configData = this.translateFile.getAll();
        return;
    }

    public String translateString(String key, String... args){
        String src = (String) configData.get(key);
        for(int i=0;i < args.length;i++){
            src = src.replaceAll("{%" + i + "}", args[i]);
        }
        return src;
    }

}