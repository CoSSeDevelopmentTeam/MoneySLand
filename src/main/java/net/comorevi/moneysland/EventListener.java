package net.comorevi.moneysland;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockEvent;
import cn.nukkit.event.block.BlockPlaceEvent;

public class EventListener implements Listener {

    private MoneySLand plugin;

    public EventListener(MoneySLand plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        onEvent(event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        onEvent(event);
    }

    public void onEvent(Event event) {
        Player player = ((IPlayer) event).getPlayer();
        Block block = ((BlockEvent) event).getBlock();
        String world = player.getLevel().getFolderName();

        if(plugin.isEditable((int)block.x, (int)block.z, world, player)) {
            event.setCancelled();
            player.sendMessage("MoneySLand>>cancelled.");
        }

    }

}
