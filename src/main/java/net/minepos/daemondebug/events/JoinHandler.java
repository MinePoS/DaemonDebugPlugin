package net.minepos.daemondebug.events;

import net.minepos.daemondebug.Main;
import net.minepos.daemondebug.objects.QueuedCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;

public class JoinHandler implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if(Main.getInstance().queuedCommands.containsKey(e.getPlayer().getUniqueId())){
            HashMap<Integer, QueuedCommand> commands = Main.getInstance().queuedCommands.get(e.getPlayer().getUniqueId());
            for (Map.Entry<Integer, QueuedCommand> entry : commands.entrySet()) {
                boolean res = entry.getValue().runAndReport();
                if(res){
                    commands.remove(entry.getKey());
                }
            }
            if(commands.values().size() == 0){
                Main.getInstance().queuedCommands.remove(e.getPlayer().getUniqueId());
            }
        }
    }

}
