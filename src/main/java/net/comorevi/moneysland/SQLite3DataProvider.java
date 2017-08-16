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
 */

package net.comorevi.moneysland;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class SQLite3DataProvider {

    private MoneySLand plugin;
    private Statement statement;

    public SQLite3DataProvider(MoneySLand plugin) {
        this.plugin = plugin;
        try {
            Class.forName("org.sqlite.JDBC");
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("CREATE table if not exists land (id integer primary key autoincrement, owner text not null, startx integer not null, startz integer not null, endx integer not null, endz integer not null, size integer not null, world text not null)");
            statement.executeUpdate("CREATE table if not exists invite (id integer not null, name text not null)");
        } catch(SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void createLand(int id, String owner, int startx, int startz, int endx, int endz, int size, String world) {
        try {
            statement.executeUpdate("INSERT INTO land VALUES(" + id + ", '"+ owner +"', "+ startx +", "+ startz +", "+ endx +", "+ endz +", "+ size +", '"+ world +"')");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public int deleteLand(String name, int x, int z, String world) {
        Map<String, Object> land = getLand(x, z, world);
        int id;
        try{
        	id = (int) land.get("id");
        }catch(NullPointerException e){
        	plugin.getServer().getPlayer(name).sendMessage(plugin.translateString("error-notFoundLand"));
        	return 0;
        }
        String owner = (String) land.get("owner");
        int size;
        try {
            if(owner.equals(name)) {
            	size = (int) land.get("size");
                statement.executeUpdate("DELETE from land WHERE id = "+ id);
                return size;
            } else {
            	plugin.getServer().getPlayer(name).sendMessage(plugin.translateString("error-land-alreadyused"));
            	return 0;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return 0;
    }

    public Map<String, Object> getLand(int x, int z, String world) {
        Map<String, Object> list = new HashMap<String, Object>();
        try {
            ResultSet rs = statement.executeQuery("SELECT * from land WHERE (startx <= "+ x +" and endx >= "+ x +") and (startz <= "+ z +" and endz >= "+ z +") and world = '"+ world +"'");
            list.put("id", rs.getInt("id"));
            list.put("owner", rs.getString("owner"));
            list.put("startx", rs.getInt("startx"));
            list.put("startz", rs.getInt("startz"));
            list.put("endx", rs.getInt("endx"));
            list.put("endz", rs.getInt("endz"));
            list.put("size", rs.getInt("size"));
            list.put("world", rs.getString("world"));
            return list;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public Map<String, Object> getLandById(int id) {
        Map<String, Object> list = new HashMap<String, Object>();
        try {
            ResultSet rs = statement.executeQuery("SELECT * from land WHERE (id = "+ id +")");
            list.put("id", rs.getInt("id"));
            list.put("owner", rs.getString("owner"));
            list.put("startx", rs.getInt("startx"));
            list.put("startz", rs.getInt("startz"));
            list.put("endx", rs.getInt("endx"));
            list.put("endz", rs.getInt("endz"));
            list.put("world", rs.getString("world"));
            return list;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public int getAllLands() {
        try {
            ResultSet rs = statement.executeQuery("SELECT * from land");
            for(int i=0;i < rs.getFetchSize();i++){
                if(!(rs.getBoolean(i)) && (rs.getString("world") != null)) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return 0;
    }

    public boolean existsLand(int x, int z, String world) {
        Map<String, Object> list = new HashMap<String, Object>();
        try {
            ResultSet rs = statement.executeQuery("SELECT count * from land WHERE (startx <= "+ x +" and endx >= "+ x +") and (startz <= "+ z +" and endz >= "+ z +") and world = '"+ world +"'");
            list.put("x", rs.getInt("x"));
            list.put("z", rs.getInt("z"));
            list.put("world", rs.getString("world"));

            /*どっちかわからない。
             * 本家:
             * return $landCount[0] > 0;
             */
            //return (int)list.get(0) > 0;
            return (rs.getFetchSize() > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addGuest(int id, String name) {
        Map<String, Object> land = getLandById(id);

        if(land == null)return;

        try {
            statement.executeUpdate("INSERT INTO invite VALUES("+ id +", '"+ name +"'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
    }

    public boolean existsGuest(int id, String name) {
        Map<String, Object> list = new HashMap<String, Object>();
        try {
            ResultSet rs = statement.executeQuery("SELECT count * from invite WHERE id = " + id + "and name = " + name);
            list.put("id", rs.getInt("id"));
            list.put("name", rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
