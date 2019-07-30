package net.minepos.daemondebug.objects;

import net.minepos.daemondebug.Main;
import net.minepos.daemondebug.MinePoSSender;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class QueuedCommand {
    private String command;
    private int  queued_id;
    private UUID player;
    private boolean needPlayer;
    public QueuedCommand(String _command, int _queued_id){
        command = _command;
        queued_id = _queued_id;
        player = null;
        needPlayer = false;
    }

    public QueuedCommand(String _command, int _queued_id, UUID _player, boolean _needPlayer){
        command = _command;
        queued_id = _queued_id;
        player = _player;
        needPlayer = _needPlayer;
    }

    public boolean run() throws ExecutionException, InterruptedException {
        if(needPlayer) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(player);
            if(!p.isOnline()){
                return false;
            }
        }
        return Bukkit.dispatchCommand(new MinePoSSender() , command );
    }

    public boolean runAndReport(){
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        boolean res = false;
        try {
            res = run();

            JSONObject resJson = new JSONObject();
            resJson.put("command", command);
            resJson.put("queued_id", queued_id);
            resJson.put("action", "command-reply");
            if(!res) {
                resJson.put("response", false);
                resJson.put("reason", "Player (" + p.getName() + ") Not Online.");
            }else{
                resJson.put("response", true);
            }
            Main.getInstance().client.send(resJson.toString());

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }
}
