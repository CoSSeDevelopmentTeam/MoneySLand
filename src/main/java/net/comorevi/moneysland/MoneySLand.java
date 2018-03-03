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
 *       - 2.6(1.0.0)
 *         SQLite3DataProviderでのstatement使いまわしをしないよう変更
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
    public static int landPrice = 100;
    public static int maxLandSize = 500;

    private MoneySAPI money;
    private SQLite3DataProvider sql;
    private static MoneySLand instance;

    private Config translateFile;
    private Map<String, Object> configData = new HashMap<String, Object>();
    private Map<String, Object> pluginData = new HashMap<String, Object>();
    private Map<String, Integer[][]> setPos = new HashMap<String, Integer[][]>();
    private List<String> worldProtect = new ArrayList<String>();
    private List<String> NoBuyWorld = new ArrayList<String>();
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

    public void createLand(String owner, int[] start, int[] end, int size, String world) {
        sql.createLand(owner, start[0], start[1], end[0], end[1], size, world);
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

            String name = player.getName();

            if(land.get("owner").equals(name)){
                return true;
            }

            return this.getSQL().existsGuest((int)land.get("id"), name);
        }catch(NullPointerException e){
            return false;
        }
    }

    public boolean isNoBuyWorld(String worldname){
        return this.NoBuyWorld.contains(worldname);
    }

    /**************/
    /** 計算関連  */
    /**************/

    public int calculateLandPrice(Player player) {
        return calculateLandSize(player) * landPrice;
    }

    public int calculateLandSize(Player player) {
        int start[] = new int[2];
        int end[] = new int[2];

        Job job = Job.get(player);
        int[] pos1 = job.getStart();
        int[] pos2 = job.getEnd();

        start[0] = Math.min(pos1[0], pos2[0]); // x minimum
        start[1] = Math.min(pos1[1], pos2[1]); // z minimum
        end[0]   = Math.max(pos1[0], pos2[0]); // x maximum
        end[1]   = Math.max(pos1[1], pos2[1]); // z maximum

        return (end[0] + 1 - start[0]) * (end[1] + 1 - start[1]);
    }

    public boolean checkOverLap(int[] start, int[] end, String world) {

        return sql.checkOverTrap(start, end, world);
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

        if(maxLandSize < -1){
            this.getLogger().info(this.translateString(("message-onEnable2"), String.valueOf(landPrice), UNIT, "無制限"));
        }else{
            this.getLogger().info(this.translateString(("message-onEnable2"), String.valueOf(landPrice), UNIT, String.valueOf(maxLandSize) + "ブロック"));
        }

        try{
            this.money = (MoneySAPI) this.getServer().getPluginManager().getPlugin("MoneySAPI");
        }catch(Exception e){
            this.getLogger().alert(TextValues.ALERT + this.translateString("error-no-moneysapi"));
            this.getServer().getPluginManager().disablePlugin(this);
        }

        this.sql = new SQLite3DataProvider(this);

        instance = this;
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

            String name = sender.getName();

            Player p = (Player)sender;
            Job job;

            switch(args[0]){
                case "start":

                    if(isNoBuyWorld(p.getLevel().getName())){
                        p.sendMessage(TextValues.WARNING + this.translateString("error-cannotBuy"));
                        return true;
                    }

                    job = Job.create(p);

                    int startX = p.getFloorX();
                    int startZ = p.getFloorZ();

                    job.start(startX, startZ);

                    p.sendMessage(TextValues.INFO + this.translateString("player-setPosition", String.valueOf(1), String.valueOf(startX), String.valueOf(startZ)));

                    return true;

                case "end":

                    if(isNoBuyWorld(p.getLevel().getName())){
                        p.sendMessage(TextValues.WARNING + this.translateString("error-cannotBuy"));
                        return true;
                    }

                    job = Job.get(p);

                    if(job == null || job.getStatus() == Job.BOUGHT) { //設定されているか,または購入済みか?
                        p.sendMessage(TextValues.WARNING + this.translateString("error-not-selected"));
                        return true;
                    }

                    int endX = p.getFloorX();
                    int endZ = p.getFloorZ();
                    
                    job.end(endX, endZ);

                    p.sendMessage(TextValues.INFO + this.translateString("player-setPosition", String.valueOf(2), String.valueOf(endX), String.valueOf(endZ)));

                    if(job.isValidValue()) { // 値がすべて入力されているなら
                        int price = calculateLandPrice(p);
                        int size  = calculateLandSize(p);

                        if(maxLandSize != -1 && size >= maxLandSize) {
                            p.sendMessage(TextValues.ALERT + this.translateString("error-landSizeLimitOver", String.valueOf(size), String.valueOf(maxLandSize)));
                            return true;
                        }
                    }

                    return true;

                case "buy":

                    job = Job.get(p);

                    if(job == null || !(job.getStatus() == Job.IN_ENTRY)) { //選択されていないか、もしくは購入後か?
                        p.sendMessage(this.translateString("error-not-selected"));
                        return true;
                    }

                    switch(job.buy()) {
                        case Job.JOB_SUCCESSFUL:
                            int size = calculateLandSize(p);
                            p.sendMessage(translateString("player-landBuy", String.valueOf(size), String.valueOf(size * landPrice), UNIT));
                            return true;

                        case Job.JOB_ERROR:
                            p.sendMessage(job.getErrorMessage());
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
                            String guest = args[2];
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

                            maxLandSize = landS;

                            this.getConfig().set("landSize", maxLandSize);
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

    public boolean onCommandByConsole(ConsoleCommandSender sender, Command command, String label, String[] args) {

        return true;
    }

    public void errorHandle(Player player) {

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
            this.conf.set("NoBuyLand", new ArrayList<String>());
            this.conf.save();
        }

        this.conf = new Config(new File("./plugins/MoneySLand/Config.yml"), Config.YAML);
        this.conf.load("./plugins/MoneySLand/Config.yml");
        this.pluginData = this.conf.getAll();

        /*コンフィグからデータを取得*/
        landPrice = (int) pluginData.get("landPrice");
        maxLandSize = (int) pluginData.get("landSize");
        this.worldProtect = conf.getStringList("worldProtect");
        this.NoBuyWorld = conf.getStringList("NoBuyWorld");

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


}