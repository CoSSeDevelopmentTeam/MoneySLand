
package net.comorevi.moneysland;

import java.util.HashMap;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import net.comorevi.moneyapi.MoneySAPI;

public class MoneySLand extends PluginBase {

    private static final String UNIT = "MS";
    private static final int landPrice = 100;
    private MoneySAPI money;

    private SQLite3DataProvider sql;
    private static MoneySLand instance;
    private String messages[];
    private Config translateFile;
    private Map<String, Object> configData;
    private Map<String, Integer[][]> setPos = new HashMap<String, Integer[][]>();


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
        this.getDataFolder().mkdir();
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

        try{
            this.money = (MoneySAPI) this.getServer().getPluginManager().getPlugin("MoneySAPI");
        }catch(Exception e){
            this.getLogger().alert(TextValues.ALERT + this.translateString("no-moneysapi"));
            this.getServer().getPluginManager().disablePlugin(this);
        }


        this.initMessageConfig();
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
                    int startX = (int)p.getX();
                    int startZ = (int)p.getZ();

                    this.setPos.get(name)[0][0] = startX;
                    this.setPos.get(name)[0][1] = startZ;
                    p.sendMessage(TextValues.INFO + this.translateString("player-setPosition", String.valueOf(1), String.valueOf(startX), String.valueOf(startZ)));

                    if(!(this.setPos.get(name).length == 0) && this.setPos.get(name).length >= 2){
                        int price = this.calculateLandPrice(name);
                        p.sendMessage(TextValues.INFO + this.translateString("player-landPrice", String.valueOf(price), UNIT));
                    }
                    return true;

                case "end":
                    int endX = (int)p.getX();
                    int endZ = (int)p.getZ();

                    this.setPos.get(name)[1][0] = endX;
                    this.setPos.get(name)[1][1] = endZ;
                    p.sendMessage(TextValues.INFO + this.translateString("player-setPosition", String.valueOf(2), String.valueOf(endX), String.valueOf(endZ)));

                    if(!(this.setPos.get(name).length == 0) && this.setPos.get(name).length >= 2){
                        int price = this.calculateLandPrice(name);
                        p.sendMessage(TextValues.INFO + this.translateString("player-landPrice", String.valueOf(price), UNIT));
                    }
                    return true;

                case "buy":
                    if(this.setPos.get(name).length == 0 || this.setPos.get(name).length < 2){
                        p.sendMessage(TextValues.ALERT + this.translateString("player-buyError1"));
                        return true;
                    }

                    String worldName = p.getLevel().getName();

                    int[] start = new int[]{};
                    int[] end = new int[]{};

                    start[0] = Math.min(this.setPos.get(name)[0][0], this.setPos.get(name)[1][0]);
                    start[1] = Math.max(this.setPos.get(name)[0][0], this.setPos.get(name)[1][0]);
                    end[0] = Math.min(this.setPos.get(name)[0][1], this.setPos.get(name)[1][1]);
                    end[1] = Math.max(this.setPos.get(name)[0][1], this.setPos.get(name)[1][1]);

                    if(!this.checkOverLap(start, end, worldName)){
                        int price = (start[1] + 1 - start[0]) * (end[1] + 1 - end[0]) * landPrice;

                        String nameB = p.getName().toLowerCase();
                        if(this.money.getMoney(p) >=price){
                            this.createLand(nameB, start, end, worldName);
                            p.sendMessage(TextValues.INFO + this.translateString("player-landBuy", String.valueOf(price), UNIT));
                            this.money.grantMoney(p, price);
                            return true;
                        }else{
                            p.sendMessage(TextValues.ALERT + this.translateString("no-money-message"));
                            return true;
                        }
                    }else{
                        p.sendMessage(TextValues.ALERT + this.translateString("land-already-used"));
                        return true;
                    }

                case "sell":
                    return true;

                case "invite":
                    if(!(args.length >= 3)){
                        p.sendMessage(TextValues.ALERT + this.translateString("error-command-message1"));
                        return true;
                    }

                    int id;

                    try{
                        id = Integer.parseInt(args[1]);
                    }catch(NumberFormatException e){
                        p.sendMessage(TextValues.ALERT + this.translateString("error-command-message2"));
                        return true;
                    }

                    Map<String, Object> land = this.getSQL().getLandById(id);

                    if(land != null){
                        if(land.get("owner").equals(name)){
                            String guest = args[2].toLowerCase();
                            if(!(this.getSQL().existsGuest(id, guest))){
                                p.sendMessage(TextValues.INFO + this.translateString("invite-land-message", name, guest));
                                if(this.getServer().getPlayer(guest) != null){
                                    this.getServer().getPlayer(guest).sendMessage(TextValues.INFO + this.translateString("invite-land-message", name, guest));
                                }
                            }else{
                                p.sendMessage(TextValues.ALERT + this.translateString("error-invite"));
                                return true;
                            }
                        }else{
                            p.sendMessage(TextValues.ALERT + this.translateString("error-invite"));
                            return true;
                        }
                    }else{
                        p.sendMessage(TextValues.ALERT + this.translateString("error-all"));
                        return true;
                    }
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