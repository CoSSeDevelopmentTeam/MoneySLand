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

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;

public class EventListener implements Listener {

    private MoneySLand plugin;

    public EventListener(MoneySLand plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        onEvent(event, event.getPlayer(), event.getBlock());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        onEvent(event, event.getPlayer(), event.getBlock());
    }

    public void onEvent(Event event, Player player, Block block) {
    	if(player.isOp())return;
    	
        String world = player.getLevel().getName();

        if(plugin.isEditable((int)block.x, (int)block.z, world, player) == false) {
            event.setCancelled();
            player.sendMessage(TextValues.ALERT + plugin.translateString("error-cannotChange"));
        }

    }

}
