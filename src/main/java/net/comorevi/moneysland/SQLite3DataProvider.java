
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLite3DataProvider {

    private MoneySLand plugin;

    public SQLite3DataProvider(MoneySLand plugin) {
        this.plugin = plugin;
        this.connect();
    }

    public void createLand(String owner, int startx, int startz, int endx, int endz, int size, String world) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate(
                    "insert into land" +
                            "(owner, startx, startz, endx, endz, size, world)" +
                            " VALUES("+ owner +"', "+ startx +", "+ startz +", "+ endx +", "+ endz +", "+ size +", '"+ world +"')"
            );
            this.printAllData();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int deleteLand(String name, int x, int z, String world) {
        Connection connection = null;
        Map<String, Object> land = getLand(x, z, world);
        int id;
        try{
            id = (int) land.get("id");
        }catch(NullPointerException e){
            plugin.getServer().getPlayer(name).sendMessage(TextValues.ALERT + plugin.translateString("error-notFoundLand"));
            return 0;
        }
        String owner = (String) land.get("owner");
        int size;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            if(owner.equals(name)) {
                size = (int) land.get("size");
                statement.executeUpdate("DELETE from land WHERE id = "+ id);
                return size;
            } else {
                plugin.getServer().getPlayer(name).sendMessage(plugin.translateString("error-land-alreadyused"));
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    public Map<String, Object> getLand(int x, int z, String world) {
        Connection connection = null;
        Map<String, Object> list = new HashMap<String, Object>();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery(
                    "SELECT * from land WHERE (startx <= "+ x +" and endx >= "+ x +")" +
                            " and " +
                            "(startz <= "+ z +" and endz >= "+ z +") and world = '"+ world +"'");
            while(rs.next()) {
                list.put("id", rs.getInt("id"));
                list.put("owner", rs.getString("owner"));
                list.put("startx", rs.getInt("startx"));
                list.put("startz", rs.getInt("startz"));
                list.put("endx", rs.getInt("endx"));
                list.put("endz", rs.getInt("endz"));
                list.put("size", rs.getInt("size"));
                list.put("world", rs.getString("world"));
            }
            if(!list.isEmpty()){
                rs.close();
                return (list.size() > 0) ? list : null;
            }else{
                list = new HashMap<String, Object>();
                //!Fami //ResultSet rs1 = statement.executeQuery("SELECT * from land WHERE (startx >= "+ x +" and endx <= "+ x +") and (startz >= "+ z +" and endz <= "+ z +") and world = '"+ world +"'");
                ResultSet rs1 = statement.executeQuery("SELECT * from land WHERE (startx >= "+ x +" and startz <= "+ z +") and (endx >= "+ x +" and endz >= "+ z +") and world = '"+ world +"'");
                //EconomyLand //ResultSet rs1 = statement.executeQuery("SELECT * from land WHERE (endx > "+ x +" and endz > "+ z +") and (startx < "+ x +" and startz <= "+ z +") and world = '"+ world +"'");
                while(rs1.next()) {
                    list.put("id", rs.getInt("id"));
                    list.put("owner", rs.getString("owner"));
                    list.put("startx", rs.getInt("startx"));
                    list.put("startz", rs.getInt("startz"));
                    list.put("endx", rs.getInt("endx"));
                    list.put("endz", rs.getInt("endz"));
                    list.put("size", rs.getInt("size"));
                    list.put("world", rs.getString("world"));
                }
                rs1.close();
                return (list.size() > 0) ? list : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Map<String, Object> getLandById(int id) {
        Connection connection = null;
        Map<String, Object> list = new HashMap<String, Object>();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("SELECT * from land WHERE (id = "+ id +")");
            while(rs.next()) {
                list.put("id", rs.getInt("id"));
                list.put("owner", rs.getString("owner"));
                list.put("startx", rs.getInt("startx"));
                list.put("startz", rs.getInt("startz"));
                list.put("endx", rs.getInt("endx"));
                list.put("endz", rs.getInt("endz"));
                list.put("size", rs.getInt("size"));
                list.put("world", rs.getString("world"));
            }
            rs.close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public List<Integer> getAllLands() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            List<Integer> ids = new ArrayList<Integer>();
            ResultSet rs = statement.executeQuery("SELECT * from land");
            while(rs.next()){
                ids.add(rs.getInt("id"));
            }
            return ids;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public boolean existsLand(int x, int z, String world) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            Map<String, Object> list = new HashMap<String, Object>();
            ResultSet rs = statement.executeQuery("SELECT * from land WHERE (startx <= "+ x +" and endx >= "+ x +") and (startz <= "+ z +" and endz >= "+ z +") and world = '"+ world +"'");
            while(rs.next()) {
                list.put("id", rs.getInt("id"));
                list.put("owner", rs.getString("owner"));
                list.put("startx", rs.getInt("startx"));
                list.put("startz", rs.getInt("startz"));
                list.put("endx", rs.getInt("endx"));
                list.put("endz", rs.getInt("endz"));
                list.put("size", rs.getInt("size"));
                list.put("world", rs.getString("world"));
            }
            if(!list.isEmpty()){
                return (int) list.get("id") > 0;
            }else{
                list = new HashMap<String, Object>();
                //!Fami //ResultSet rs1 = statement.executeQuery("SELECT * from land WHERE (startx >= "+ x +" and endx <= "+ x +") and (startz >= "+ z +" and endz <= "+ z +") and world = '"+ world +"'");
                ResultSet rs1 = statement.executeQuery("SELECT * from land WHERE (startx >= "+ x +" and startz <= "+ z +") and (endx >= "+ x +" and endz >= "+ z +") and world = '"+ world +"'");
                //EconomyLand //ResultSet rs1 = statement.executeQuery("SELECT * from land WHERE (endx > "+ x +" and endz > "+ z +") and (startx < "+ x +" and startz <= "+ z +") and world = '"+ world +"'");
                //ResultSet rs1 = statement.executeQuery("SELECT * from land WHERE (startx < "+ x +" and endx > "+ x +") and (startz < "+ z +" and endz > "+ z +") and world = '"+ world +"'");
                while(rs1.next()) {
                    list.put("id", rs.getInt("id"));
                    list.put("owner", rs.getString("owner"));
                    list.put("startx", rs.getInt("startx"));
                    list.put("startz", rs.getInt("startz"));
                    list.put("endx", rs.getInt("endx"));
                    list.put("endz", rs.getInt("endz"));
                    list.put("size", rs.getInt("size"));
                    list.put("world", rs.getString("world"));
                }
                return (int) list.get("id") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void addGuest(int id, String name) {
        Connection connection = null;
        Map<String, Object> land = getLandById(id);

        if(land == null)return;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("INSERT INTO invite(id, name) VALUES("+ id +", '"+ name +"'");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }

    public boolean existsGuest(int id, String name) {
        Connection connection = null;
        Map<String, Object> list = new HashMap<String, Object>();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("SELECT * from invite WHERE id = " + id + " and name = '" + name + "'");
            while(rs.next()) {
                list.put("id", rs.getInt("id"));
                list.put("name", rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void connect(){
        try {
            Class.forName("org.sqlite.JDBC");
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("create table if not exists land " +
                    "(" +
                    "id integer primary key autoincrement, " +
                    "owner text not null, " +
                    "startx integer not null, " +
                    "startz integer not null, " +
                    "endx integer not null, " +
                    "endz integer not null, " +
                    "size integer not null, " +
                    "world text not null" +
                    ")"
            );
            statement.executeUpdate(
                    "create table if not exists invite " +
                            "(" +
                            "id integer not null, " +
                            "name text not null" +
                            ")"
            );
        } catch(SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean checkOverTrap(int[] start, int[] end, String world) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery(
                    "select * from land " +
                    "WHERE (start[0] <= startx AND start[1] <= startz AND end[0] >= startx AND end[1] >= startz) OR " +
                    "(start[0] <= endx AND start[1] <= startz AND end[0] >= endx AND end[1] >= startz) OR " +
                    "(start[0] <= startx AND start[1] <= endz AND end[0] >= startx AND end[1] >= endz) OR " +
                    "(start[0] <= endx AND start[1] <= endz AND end[0] >= endx AND end[1] >= endz)"
            );
            return rs.next(); //次の要素があるか。即ち土地の被りがあるか
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void printAllData() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toString() + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from land");
            while(rs.next()) {
                System.out.println("-----------------------");
                System.out.println("id = " + rs.getInt("id"));
                System.out.println("owner = " + rs.getString("owner"));
                System.out.println("startx = " + rs.getInt("startx"));
                System.out.println("startz = " + rs.getInt("startx"));
                System.out.println("endx = " + rs.getInt("endx"));
                System.out.println("endz = " + rs.getInt("endz"));
                System.out.println("size = " + rs.getInt("size"));
                System.out.println("world = " + rs.getString("world"));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
