/**
 * MoneySLand
 *
 *
 * CosmoSunriseServerPluginEditorsTeam
 *
 * HP: http://info.comorevi.net
 * GitHub: https://github.com/CosmoSunriseServerPluginEditorsTeam
 *
 *
 * @author itsu
 * @author popkechupki
 *
 *
 * 機能版 アップデート履歴
 *
 * - 1.0
 *    基本的な機能を追加。ほぼ動作するように。
 */

package net.comorevi.moneysland;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;
import net.comorevi.moneyapi.MoneySAPI;

public class MoneySLand extends PluginBase {

    private static final String UNIT = "MS";
    private static int landPrice = 100;
    private static int landSize = 500;

    private MoneySAPI money;
    private SQLite3DataProvider sql;
    private static MoneySLand instance;

    private String messages[];
    private Config translateFile;
    private Map<String, Object> configData = new HashMap<String, Object>();
    private Map<String, Object> pluginData = new HashMap<String, Object>();
    private Map<String, Integer[][]> setPos = new HashMap<String, Integer[][]>();
    private Config conf;


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

    public Map<String, Object> getLand(int x, int z, String world) {
        return sql.getLand(x, z, world);
    }

    public boolean existsLand(int x, int z, String world) {
        return false;//sql.existsLand(x, z, world);
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
        try{
            Map<String, Object> land = this.getSQL().getLand(x, z, world);
            int landId = (int)land.get("id");
            if(landId == 0){
                return !(this.isWorldProtect());
            }

            String name = player.getName().toLowerCase();

            if(land.get("owner").equals(name)){
                return true;
            }

            return this.getSQL().existsGuest((int)land.get("id"), name);
        }catch(NullPointerException e){
            return false;
        }
    }

    /**************/
    /** 金銭関連  */
    /**************/

    public int calculateLandPrice(String name) {
        int start[] = new int[2];
        int end[] = new int[2];

        start[0] = Math.min(this.setPos.get(name)[0][0], this.setPos.get(name)[1][0]);
        start[1] = Math.max(this.setPos.get(name)[0][0], this.setPos.get(name)[1][0]);
        end[0] = Math.min(this.setPos.get(name)[0][1], this.setPos.get(name)[1][1]);
        end[1] = Math.max(this.setPos.get(name)[0][1], this.setPos.get(name)[1][1]);

        return (start[1] + 1 - start[0]) * (end[1] + 1 - end[0]) * landPrice;
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

        this.initMessageConfig();
        this.initMoneySLandConfig();

        this.getLogger().info(this.translateString("message-onEnable"));

        try{
            this.money = (MoneySAPI) this.getServer().getPluginManager().getPlugin("MoneySAPI");
        }catch(Exception e){
            this.getLogger().alert(TextValues.ALERT + this.translateString("error-no-moneysapi"));
            this.getServer().getPluginManager().disablePlugin(this);
        }

        this.sql = new SQLite3DataProvider(this);
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
                sender.sendMessage(TextValues.WARNING + this.translateString("error-command-console"));
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

                    try{
                        if(this.setPos.get(name) == null){
                            this.setPos.put(name, new Integer[2][2]);
                            this.setPos.get(name)[0][0] = 999999999;
                            this.setPos.get(name)[0][1] = 999999999;
                            this.setPos.get(name)[1][0] = 999999999;
                            this.setPos.get(name)[1][1] = 999999999;
                        }
                    }catch(NullPointerException e){
                        this.setPos.put(name, new Integer[2][2]);
                        this.setPos.get(name)[0][0] = 999999999;
                        this.setPos.get(name)[0][1] = 999999999;
                        this.setPos.get(name)[1][0] = 999999999;
                        this.setPos.get(name)[1][1] = 999999999;
                    }

                    this.setPos.get(name)[0][0] = startX;
                    this.setPos.get(name)[0][1] = startZ;
                    p.sendMessage(TextValues.INFO + this.translateString("player-setPosition", String.valueOf(1), String.valueOf(startX), String.valueOf(startZ)));

                    if(!(this.setPos.get(name)[0][0] == 999999999) && !(this.setPos.get(name)[0][1] == 999999999) && !(this.setPos.get(name)[1][0] == 999999999) && !(this.setPos.get(name)[1][1] == 999999999) && this.setPos.get(name).length >= 2){
                        int price = this.calculateLandPrice(name);
                        p.sendMessage(TextValues.INFO + this.translateString("player-landPrice", String.valueOf(price), UNIT));
                    }
                    return true;

                case "end":
                    int endX = (int)p.getX();
                    int endZ = (int)p.getZ();

                    try{
                        if(this.setPos.get(name) == null){
                            this.setPos.put(name, new Integer[2][2]);
                            this.setPos.get(name)[0][0] = 999999999;
                            this.setPos.get(name)[0][1] = 999999999;
                            this.setPos.get(name)[1][0] = 999999999;
                            this.setPos.get(name)[1][1] = 999999999;
                        }
                    }catch(NullPointerException e){
                        this.setPos.put(name, new Integer[2][2]);
                        this.setPos.get(name)[0][0] = 999999999;
                        this.setPos.get(name)[0][1] = 999999999;
                        this.setPos.get(name)[1][0] = 999999999;
                        this.setPos.get(name)[1][1] = 999999999;
                    }

                    this.setPos.get(name)[1][0] = endX;
                    this.setPos.get(name)[1][1] = endZ;
                    p.sendMessage(TextValues.INFO + this.translateString("player-setPosition", String.valueOf(2), String.valueOf(endX), String.valueOf(endZ)));

                    if(!(this.setPos.get(name)[0][0] == 999999999) && !(this.setPos.get(name)[0][1] == 999999999) && !(this.setPos.get(name)[1][0] == 999999999) && !(this.setPos.get(name)[1][1] == 999999999) && this.setPos.get(name).length >= 2){
                        int price = this.calculateLandPrice(name);
                        p.sendMessage(TextValues.INFO + this.translateString("player-landPrice", String.valueOf(price), UNIT));
                    }
                    return true;

                case "buy":
                    if(this.setPos.get(name).length == 0 || this.setPos.get(name).length < 2){
                        p.sendMessage(TextValues.ALERT + this.translateString("error-not-selected"));
                        return true;
                    }

                    String worldName = p.getLevel().getName();

                    int[] start = new int[2];
                    int[] end = new int[2];

                    start[0] = Math.min(this.setPos.get(name)[0][0], this.setPos.get(name)[1][0]);
                    start[1] = Math.max(this.setPos.get(name)[0][0], this.setPos.get(name)[1][0]);
                    end[0] = Math.min(this.setPos.get(name)[0][1], this.setPos.get(name)[1][1]);
                    end[1] = Math.max(this.setPos.get(name)[0][1], this.setPos.get(name)[1][1]);

                    if(!this.checkOverLap(start, end, worldName)){
                        int price = (start[1] + 1 - start[0]) * (end[1] + 1 - end[0]) * landPrice;
                        int s = (start[1] + 1 - start[0]) * (end[1] + 1 - end[0]) * 1;

                        String nameB = p.getName().toLowerCase();
                        if(this.money.getMoney(p) >=price){
                            this.createLand(nameB, start, end, worldName);
                            p.sendMessage(TextValues.INFO + this.translateString("player-landBuy", String.valueOf(s), String.valueOf(price), UNIT));
                            this.money.grantMoney(p, price);

                            this.setPos.get(name)[0][0] = 999999999;
                            this.setPos.get(name)[0][1] = 999999999;
                            this.setPos.get(name)[1][0] = 999999999;
                            this.setPos.get(name)[1][1] = 999999999;
                            return true;
                        }else{
                            p.sendMessage(TextValues.ALERT + this.translateString("error-no-money"));
                            return true;
                        }
                    }else{
                        p.sendMessage(TextValues.ALERT + this.translateString("error-land-alreadyused"));
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
                                p.sendMessage(TextValues.INFO + this.translateString("player-landInvited", name, guest));
                                if(this.getServer().getPlayer(guest) != null){
                                    this.getServer().getPlayer(guest).sendMessage(TextValues.INFO + this.translateString("player-landInvited", name, guest));
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
                    try{
                        Level level = p.getLevel();
                        int x = (int)p.getX();
                        int z = (int)p.getZ();
                        String world = level.getName();
                        int landId = -1;

                        if(this.existsLand(x, z, world)){
                            landId = (int)this.getLand(x, z, world).get("id");
                        }

                        if(landId == -1){
                            p.sendMessage(TextValues.INFO + this.translateString("player-noSuchLandId"));
                        }else{
                            p.sendMessage(TextValues.INFO + this.translateString("player-landId", String.valueOf(landId)));
                        }
                        return true;
                    }catch(NullPointerException e){
                        p.sendMessage(TextValues.INFO + this.translateString("error-all"));
                        return true;
                    }

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
        sender.sendMessage("/land <start | end | buy | sell | invite | info>");
    }

    public String parseMessage(String message) {
        return "";
    }

    private void initMessageConfig(){
        if(!new File("./plugins/MoneySLand/Message.yml").exists()){
            try {
                FileWriter fw = new FileWriter(new File("./plugins/MoneySLand/Message.yml"), true);//trueで追加書き込み,falseで上書き
                PrintWriter pw = new PrintWriter(fw);
                pw.println("");
                pw.close();
                Utils.writeFile(new File("./plugins/MoneySLand/Message.yml"), this.getClass().getClassLoader().getResourceAsStream("Message.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.translateFile = new Config(new File("./plugins/MoneySLand/Message.yml"), Config.YAML);
        this.translateFile.load("./plugins/MoneySLand/Message.yml");
        this.configData = this.translateFile.getAll();
        return;
    }

    public void initMoneySLandConfig(){
        if(!new File("./plugins/MoneySLand/Config.yml").exists()){
            try {
                FileWriter fw = new FileWriter(new File("./plugins/MoneySLand/Config.yml"), true);
                PrintWriter pw = new PrintWriter(fw);
                pw.println("");
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.conf = new Config(new File("./plugins/MoneySLand/Config.yml"), Config.YAML);
            this.conf.load("./plugins/MoneySLand/Config.yml");
            this.conf.set("landPrice", 100);
            this.conf.set("landSize", 500);
            this.conf.save();
        }

        this.conf = new Config(new File("./plugins/MoneySLand/Config.yml"), Config.YAML);
        this.conf.load("./plugins/MoneySLand/Config.yml");
        this.pluginData = this.conf.getAll();

        landPrice = (int) pluginData.get("landPrice");
        landSize = (int) pluginData.get("landSize");

        return;
    }

    public String translateString(String key, String... args){
        if(configData != null || !configData.isEmpty()){
            String src = (String) configData.get(key);
            for(int i=0;i < args.length;i++){
                src = src.replace("{%" + i + "}", args[i]);
            }
            return src;
        }
        return null;
    }

}