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
 * このプラグインはMasterF氏開発のMyLandプラグインを開発者の承諾のもと、Javaに移植をしたものです。
 *
 *
 * [Java版]
 * @author itsu
 * @author popkechupki
 *
 * [PHP版]
 * @author MasterF
 *
 *
 * 機能版 アップデート履歴
 *
 * - 1.0
 *    基本的な機能を追加。ほぼ動作するように。
 *  - 1.1
 *     著作権表記並びにコードの位置を修正
 *   - 1.2
 *      購入可能土地サイズの制限に対応。
 *    - 1.3
 *       細かな調整/エラー回避
 *     - 1.4
 *        土地制限の無制限(-1)に対応
 * - 2.0
 *    イベント関連の修正/コードの調整/ワールドプロテクトの実装/ヘルプの実装
 *  - 2.1
 *     ヘルプで::op, ::allタグ, ##コメントアウトが使えるように/SQL文の修正
 *   - 2.2
 *      OPコマンドの実装(/land set, /land deleteworldprotect, /land addworldprotect
 *    - 2.3
 *       /land sellの実装/致命的なバグの修正
 *     - 2.4
 *        大幅なバグ修正/いろいろな実装(自分でもよくわからん)
 *      - 2.5
 *         バグ修正
 *
 */

package net.comorevi.moneysland;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static int landId;

    private MoneySAPI money;
    private SQLite3DataProvider sql;
    private static MoneySLand instance;

    private Config translateFile;
    private Map<String, Object> configData = new HashMap<String, Object>();
    private Map<String, Object> pluginData = new HashMap<String, Object>();
    private Map<String, Integer[][]> setPos = new HashMap<String, Integer[][]>();
    private List<String> worldProtect = new ArrayList<String>();
    private List<String> help = new ArrayList<String>();
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

    public Config getConfig(){
        return this.conf;
    }

    /**************/
    /** Land関連  */
    /**************/

    public Map<String, Object> getLand(int x, int z, String world) {
        return sql.getLand(x, z, world);
    }

    public boolean existsLand(int x, int z, String world) {
        return sql.existsLand(x, z, world);
    }

    public void createLand(int id, String owner, int[] start, int[] end, int size, String world) {
        sql.createLand(id, owner, start[0], start[1], end[0], end[1], size, world);
    }

    public int deleteLand(String name, int x, int z, String world){
        return sql.deleteLand(name, x, z, world);
    }

    public boolean isWorldProtect(String world) {
        return this.worldProtect.contains(world);
    }

    public boolean isEditable(int x, int z, String world, Player player){
        if(player.isOp())return true;
        try{
            Map<String, Object> land = this.getSQL().getLand(x, z, world);

            if(land == null){
                return !(this.isWorldProtect(world));
            }

            String name = player.getName().toLowerCase();

            if((int) land.get("startx") < (int) land.get("endx") && (int) land.get("startz") < (int) land.get("endz")){
                return false;
            }

            if(land.get("owner").equals(name)){
                return true;
            }

            return this.getSQL().existsGuest((int)land.get("id"), name);
        }catch(NullPointerException e){
            return false;
        }
    }

    /**************/
    /** 計算関連  */
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

    public int calculateLandSize(String name) {
        int start[] = new int[2];
        int end[] = new int[2];

        start[0] = Math.min(this.setPos.get(name)[0][0], this.setPos.get(name)[1][0]);
        start[1] = Math.max(this.setPos.get(name)[0][0], this.setPos.get(name)[1][0]);
        end[0] = Math.min(this.setPos.get(name)[0][1], this.setPos.get(name)[1][1]);
        end[1] = Math.max(this.setPos.get(name)[0][1], this.setPos.get(name)[1][1]);

        return (start[1] + 1 - start[0]) * (end[1] + 1 - end[0]) * 1;
    }

    public boolean checkOverLap(int[] start, int[] end, String world) {
        Map<String, Object> map;
        for(int id : this.getSQL().getAllLands()){
            if(this.sql.getLandById(id).get("world").equals(world)){
                map = this.sql.getLandById(id);
                if((int) map.get("startx") >= start[0] && (int) map.get("startz") >= start[1]
                        && (int) map.get("endx") <= end[0] && (int) map.get("endz") <= end[1]){
                    return true;
                }
            }
        }
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
        this.initHelpFile();

        this.getLogger().info(this.translateString("message-onEnable"));
        if(landSize == -1){
            this.getLogger().info(this.translateString(("message-onEnable2"), String.valueOf(landPrice), UNIT, "無制限"));
        }else{
            this.getLogger().info(this.translateString(("message-onEnable2"), String.valueOf(landPrice), UNIT, String.valueOf(landSize) + "ブロック"));
        }

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
                            this.resetLandData(name);
                        }
                    }catch(NullPointerException e){
                        this.setPos.put(name, new Integer[2][2]);
                        this.resetLandData(name);
                    }

                    this.setPos.get(name)[0][0] = startX;
                    this.setPos.get(name)[0][1] = startZ;
                    p.sendMessage(TextValues.INFO + this.translateString("player-setPosition", String.valueOf(1), String.valueOf(startX), String.valueOf(startZ)));

                    if(!(this.setPos.get(name)[0][0] == 999999999) && !(this.setPos.get(name)[0][1] == 999999999) && !(this.setPos.get(name)[1][0] == 999999999) && !(this.setPos.get(name)[1][1] == 999999999) && this.setPos.get(name).length >= 2){
                        int size = this.calculateLandSize(name);
                        if(!(landSize == -1)){
                            if(size > landSize){
                                p.sendMessage(TextValues.ALERT + this.translateString("error-landSizeLimitOver", String.valueOf(size), String.valueOf(landSize)));
                                return true;
                            }else{
                                int price = this.calculateLandPrice(name);
                                p.sendMessage(TextValues.INFO + this.translateString("player-landPrice", String.valueOf(price), UNIT));
                                return true;
                            }
                        }else{
                            int price = this.calculateLandPrice(name);
                            p.sendMessage(TextValues.INFO + this.translateString("player-landPrice", String.valueOf(price), UNIT));
                        }
                    }
                    return true;

                case "end":
                    int endX = (int)p.getX();
                    int endZ = (int)p.getZ();

                    try{
                        if(this.setPos.get(name) == null){
                            this.setPos.put(name, new Integer[2][2]);
                            this.resetLandData(name);
                        }
                    }catch(NullPointerException e){
                        this.setPos.put(name, new Integer[2][2]);
                        this.resetLandData(name);
                    }

                    this.setPos.get(name)[1][0] = endX;
                    this.setPos.get(name)[1][1] = endZ;
                    p.sendMessage(TextValues.INFO + this.translateString("player-setPosition", String.valueOf(2), String.valueOf(endX), String.valueOf(endZ)));

                    if(!(this.setPos.get(name)[0][0] == 999999999) && !(this.setPos.get(name)[0][1] == 999999999) && !(this.setPos.get(name)[1][0] == 999999999) && !(this.setPos.get(name)[1][1] == 999999999) && this.setPos.get(name).length >= 2){
                        int size = this.calculateLandSize(name);
                        if(!(landSize == -1)){
                            if(size > landSize){
                                p.sendMessage(TextValues.ALERT + this.translateString("error-landSizeLimitOver", String.valueOf(size), String.valueOf(landSize)));
                                return true;
                            }else{
                                int price = this.calculateLandPrice(name);
                                p.sendMessage(TextValues.INFO + this.translateString("player-landPrice", String.valueOf(price), UNIT));
                                return true;
                            }
                        }else{
                            int price = this.calculateLandPrice(name);
                            p.sendMessage(TextValues.INFO + this.translateString("player-landPrice", String.valueOf(price), UNIT));
                        }
                    }
                    return true;

                case "buy":
                    try{
                        if(this.setPos.get(name).length == 0 || this.setPos.get(name).length < 2){
                            p.sendMessage(TextValues.ALERT + this.translateString("error-not-selected"));
                            return true;
                        }
                    }catch(NullPointerException e){
                        p.sendMessage(TextValues.ALERT + this.translateString("error-not-selected"));
                        return true;
                    }

                    String worldName = p.getLevel().getName();

                    if(isWorldProtect(worldName)){
                         p.sendMessage(TextValues.INFO + this.translateString("error-cannotBuy"));
                         return true;
                    }

                    int[] start = new int[2];
                    int[] end = new int[2];

                    start[0] = Math.min(this.setPos.get(name)[0][0], this.setPos.get(name)[1][0]);
                    start[1] = Math.max(this.setPos.get(name)[0][0], this.setPos.get(name)[1][0]);
                    end[0] = Math.min(this.setPos.get(name)[0][1], this.setPos.get(name)[1][1]);
                    end[1] = Math.max(this.setPos.get(name)[0][1], this.setPos.get(name)[1][1]);

                    if(!this.checkOverLap(start, end, worldName)){

                        if(start[0] < end[0] && start[1] < end[1]){//例の条件式
                            p.sendMessage(TextValues.INFO + this.translateString("error-cannotBuy"));
                            return true;
                        }

                        int price = (start[1] + 1 - start[0]) * (end[1] + 1 - end[0]) * landPrice;
                        int s = (start[1] + 1 - start[0]) * (end[1] + 1 - end[0]) * 1;

                        String nameB = p.getName().toLowerCase();
                        if(this.money.getMoney(p) >=price){
                            int id = this.getConfig().getInt("landId");
                            this.createLand(id, nameB, start, end, s, worldName);
                            id++;
                            this.getConfig().set("landId", id);
                            this.getConfig().save();
                            p.sendMessage(TextValues.INFO + this.translateString("player-landBuy", String.valueOf(s), String.valueOf(price), UNIT));
                            this.money.setMoney(p, this.money.getMoney(p) - price);
                            this.resetLandData(name);
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
                    try{if(args[1] != null){}}
                    catch(ArrayIndexOutOfBoundsException e){
                        p.sendMessage(TextValues.ALERT + this.translateString(("error-command-message1")));
                        return true;
                    }

                    int landid = -1;
                    Map<String, Object> sellData = new HashMap<String, Object>();

                    try{
                        landid = Integer.parseInt(args[1]);
                    }catch(NumberFormatException e){
                        p.sendMessage(TextValues.ALERT + this.translateString(("error-command-message"), String.valueOf(2)));
                        return true;
                    }

                    try{
                        sellData = this.getSQL().getLandById(landid);
                    }catch(NullPointerException e){
                        p.sendMessage(TextValues.ALERT + this.translateString("player-noSuchLandId"));
                        return true;
                    }

                    int money = this.deleteLand(name, (int) p.getX(), (int) p.getZ(), p.getLevel().getName()) / 2 * 100;
                    this.money.addMoney(p, money);
                    p.sendMessage(TextValues.INFO + this.translateString("player-sell", String.valueOf(money), UNIT, String.valueOf(landid)));

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
                        p.sendMessage(TextValues.ALERT + this.translateString(("error-command-message"), String.valueOf(2)));
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
                                p.sendMessage(TextValues.ALERT + this.translateString("error-landInvite"));
                                return true;
                            }
                        }else{
                            p.sendMessage(TextValues.ALERT + this.translateString("error-landInvite"));
                            return true;
                        }
                    }else{
                        p.sendMessage(TextValues.ALERT + this.translateString(("error-notFoundLandKey"), String.valueOf(id)));
                        return true;
                    }
                    return true;

                case "info":
                    try{
                        Level level = p.getLevel();
                        int x = (int) p.getX();
                        int z = (int) p.getZ();
                        String world = level.getName();
                        int landId = -1;
                        String owner = "";
                        Map<String, Object> map = new HashMap<String, Object>();

                        if(this.existsLand(x, z, world)){
                            map = this.getLand(x, z, world);
                            landId = (int) map.get("id");
                            owner = (String) map.get("owner");
                        }

                        if(landId == -1){
                            p.sendMessage(TextValues.INFO + this.translateString("player-noSuchLandId"));
                        }else{
                            p.sendMessage(TextValues.INFO + this.translateString("player-landId", String.valueOf(landId), owner));
                        }
                        return true;
                    }catch(NullPointerException e){
                        p.sendMessage(TextValues.INFO + this.translateString("player-noSuchLandId"));
                        return true;
                    }

                case "help":
                    this.helpMessage(sender);
                    return true;

                case "set":
                    if(!p.isOp()){
                        p.sendMessage(TextValues.ALERT + this.translateString("player-isNotOP"));
                        return true;
                    }

                    try{if(args[1] != null){}}
                    catch(ArrayIndexOutOfBoundsException e){
                        p.sendMessage(TextValues.ALERT + this.translateString(("error-command-message1")));
                        return true;
                    }

                    switch(args[1]){
                        case "landPrice":
                            try{if(args[2] != null){}}
                            catch(ArrayIndexOutOfBoundsException e){
                                p.sendMessage(TextValues.ALERT + this.translateString(("error-command-message1")));
                                return true;
                            }

                            int landP;

                            try{
                                landP = Integer.parseInt(args[2]);
                            }catch(NumberFormatException e){
                                p.sendMessage(TextValues.ALERT + TextValues.ALERT + this.translateString("error-command-message2", String.valueOf(3)));
                                return true;
                            }

                            landPrice = landP;

                            this.getConfig().set("landPrice", landPrice);
                            this.getConfig().save();

                            return true;

                        case "landSize":
                            try{if(args[2] != null){}}
                            catch(ArrayIndexOutOfBoundsException e){
                                p.sendMessage(TextValues.ALERT + this.translateString(("error-command-message1")));
                                return true;
                            }

                            int landS;

                            try{
                                landS = Integer.parseInt(args[2]);
                            }catch(NumberFormatException e){
                                p.sendMessage(TextValues.ALERT + this.translateString("error-command-message2", String.valueOf(3)));
                                return true;
                            }

                            landSize = landS;

                            this.getConfig().set("landSize", landSize);
                            this.getConfig().save();

                            return true;
                    }
                    return true;

                case "deleteworldprotect":
                    try{if(args[1] != null){}}
                    catch(ArrayIndexOutOfBoundsException e){
                        p.sendMessage(TextValues.ALERT + this.translateString(("error-command-message1")));
                        return true;
                    }

                    this.worldProtect.remove(args[1]);

                    this.getConfig().set("worldProtect", this.worldProtect);
                    this.getConfig().save();

                    return true;

                case "addworldprotect":
                    try{if(args[1] != null){}}
                    catch(ArrayIndexOutOfBoundsException e){
                        p.sendMessage(TextValues.ALERT + this.translateString(("error-command-message1")));
                        return true;
                    }

                    if(this.worldProtect.contains(args[1])){
                        p.sendMessage(TextValues.ALERT + this.translateString("error-already-set"));
                        return true;
                    }

                    this.worldProtect.add(args[1]);

                    this.getConfig().set("worldProtect", this.worldProtect);
                    this.getConfig().save();

                    return true;
            }
        }
        return false;
    }

    /****************/
    /** メッセージ **/
    /****************/

    public void helpMessage(CommandSender sender){
        Thread th = new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./plugins/MoneySLand/Help.txt")), "UTF-8"));
                    String txt;
                    boolean op = (boolean) sender.isOp();
                    boolean send = true;
                    while(true){
                        txt = br.readLine();
                        if(txt == null)break;
                        if(txt.startsWith("##"))continue;
                        if(txt.equals("::op")){
                            send = false;
                            continue;
                        }
                        if(op)send = true;
                        if(txt.equals("::all")){
                            send = true;
                            continue;
                        }
                        if(send) sender.sendMessage(txt);
                    }
                    br.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
                return;
            }
        });
        th.start();
    }

    public String translateString(String key, String... args){
        if(configData != null || !configData.isEmpty()){
            String src = (String) configData.get(key);
            if(src == null || src.equals("")) return TextValues.ALERT + (String) configData.get("error-notFoundKey");
            for(int i=0;i < args.length;i++){
                src = src.replace("{%" + i + "}", args[i]);
            }
            return src;
        }
        return null;
    }

    public String parseMessage(String message) {
        return "";
    }

    /**************/
    /**  その他  **/
    /**************/

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

    private void initMoneySLandConfig(){
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
            this.conf.set("landSize", -1);
            this.conf.set("worldProtect", new ArrayList<String>());
            this.conf.set("landId", 0);
            this.conf.save();
        }

        this.conf = new Config(new File("./plugins/MoneySLand/Config.yml"), Config.YAML);
        this.conf.load("./plugins/MoneySLand/Config.yml");
        this.pluginData = this.conf.getAll();

        /*コンフィグからデータを取得*/
        landPrice = (int) pluginData.get("landPrice");
        landSize = (int) pluginData.get("landSize");
        landId = (int) pluginData.get("landId");
        this.worldProtect = conf.getStringList("worldProtect");

        return;
    }

    public void initHelpFile(){
        if(!new File("./plugins/MoneySLand/Help.txt").exists()){
            try {
                FileWriter fw = new FileWriter(new File("./plugins/MoneySLand/Help.txt"), true);
                PrintWriter pw = new PrintWriter(fw);
                pw.println("");
                pw.close();

                Utils.writeFile(new File("./plugins/MoneySLand/Help.txt"), this.getClass().getClassLoader().getResourceAsStream("Help.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void resetLandData(String name){
        /*マイクラにはない座標(999999999)を代入しておくことでコマンドが実行されたか判定*/
        this.setPos.get(name)[0][0] = 999999999;
        this.setPos.get(name)[0][1] = 999999999;
        this.setPos.get(name)[1][0] = 999999999;
        this.setPos.get(name)[1][1] = 999999999;
        return;
    }

}