package net.comorevi.moneysland;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/DataDB.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("create table if not exists land (id integer primary key autoincrement, owner text not null, startx integer not null, startz integer not null, endx integer not null, endz integer not null, world text not null)");
            statement.executeUpdate("create table if not exists invite (id integer not null, name text not null)");
        } catch(SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void createLand(String owner, int startx, int startz, int endx, int endz, String world) {
        try {
            statement.executeUpdate("insert into land values('"+ owner +"', "+ startx +", "+ startz +", "+ endx +", "+ endz +", '"+ world +"')");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public int getLand(int x, int z, String world) {
        try {
            ResultSet rs = statement.executeQuery("select * from land where (startx <= "+ x +" and endx >= "+ x +") and (startz <= "+ z +" and endz >= "+ z +") and world = '"+ world +"'");
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


    public ArrayList<Object> getLandById(int id) {
        ArrayList<Object> list = new ArrayList<Object>();
        try {
            ResultSet rs = statement.executeQuery("select * from land where (id = "+ id +")");
            list.add(rs.getInt("id"));
            list.add(rs.getShort("owner"));
            list.add(rs.getInt("startx"));
            list.add(rs.getInt("startz"));
            list.add(rs.getInt("endx"));
            list.add(rs.getInt("endz"));
            list.add(rs.getShort("world"));
            return list;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public int getAllLands() {
    	try {
            ResultSet rs = statement.executeQuery("select * from land");
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
    	
    }
    
    public void addGuest(int id, String name) {
    	ArrayList<Object> land = getLandById(id);
    	
    	if(land = null) return false;
    	
    	statement.executeUpdate("insert into invite values("+ id +", '"+ name +"'");
    }
    
    public boolean existsGuest(int id, String name) {
    	return false;
    }

}
