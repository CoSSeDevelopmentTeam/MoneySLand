package net.comorevi.moneysland;

import cn.nukkit.Player;
import net.comorevi.moneyapi.MoneySAPI;

import java.util.HashMap;

public class Job {

    public static final int WAITING  = 0;
    public static final int IN_ENTRY = 1;
    public static final int CHECKING = 2;
    public static final int BOUGHT   = 3;

    public static final int JOB_SUCCESSFUL   = 0;
    public static final int JOB_ERROR        = 1;

    public static final int ERROR_INVAILD_VALUE = 1;

    // Singleton
    private static HashMap<String, Job> Job_data;

    public static Job create(Player player) {
        Job job = new Job(player);
        Job_data.put(player.getName(), job);
        return job;
    }

    public static Job get(Player player) {
        return Job_data.get(player.getName());
    }

    // Objective
    private int status;
    private Player player;

    private int[] start = new int[2]; //[0 => x, 1 => z]
    private int[] end   = new int[2];   //[0 => x, 1 => z]
    private String world;

    private Job(Player player) {
        status = Job.WAITING;
        this.player = player;
    }

    public void start(int x, int z) {
        start[0] = x;
        start[1] = z;
        world = player.getLevel().getFolderName();
        status = Job.IN_ENTRY;
    }

    public void end(int x, int z) {
        end[0] = x;
        end[1] = z;
        world = player.getLevel().getFolderName();
        status = Job.IN_ENTRY;
    }

    public int buy() {
        MoneySLand main = MoneySLand.getInstance();
        if(!isValidValue()) {
            return Job.JOB_ERROR;
        }

        if (MoneySAPI)
        main.checkOverLap(start, end, world);


    }

    public boolean isValidValue() {
        boolean result =
                (start[0] == 0 && start[1] == 0) && (end[0] == 0 && end[1] == 0); // 配列の値が初期値か?
        return result;
    }

    public int[] getStart() {
        return start;
    }

    public int[] getEnd() {
        return end;
    }

}
