package net.minepos.daemondebug;

import net.minepos.daemondebug.objects.QueuedCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class DaemonClient extends WebSocketClient {

    public DaemonClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public DaemonClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send(new JSONObject().put("action","get_command_queue").toString());
        System.out.println("Connection to Daemon Opened");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received message: " + message);
        JSONObject jsonObject =  new JSONObject(message);
        if(jsonObject.has("action")){
            if(jsonObject.getString("action").equalsIgnoreCase("command")){
                String command = jsonObject.getString("command");

                if(jsonObject.has("player")) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(jsonObject.getString("player")));
                    if (!p.isOnline()) {
                        JSONObject resJson = new JSONObject();
                        resJson.put("command", jsonObject.get("command"));
                        resJson.put("queued_id", jsonObject.get("queued_id"));
                        resJson.put("action", "command-reply");
                        resJson.put("response", false);
                        resJson.put("reason", "Player (" + p.getName() + ") Not Online.");
                        send(resJson.toString());

                        if(!Main.getInstance().queuedCommands.containsKey(p.getUniqueId())){
                            Main.getInstance().queuedCommands.put(p.getUniqueId(), new HashMap<>());
                        }
                        HashMap<Integer,QueuedCommand> list = Main.getInstance().queuedCommands.get(p.getUniqueId());
                        list.put(jsonObject.getInt("queued_id"),new QueuedCommand(command, jsonObject.getInt("queued_id"),UUID.fromString(jsonObject.getString("player")), true));
                        Bukkit.getLogger().info(p.getName() +" now has "+list.size()+" commands queued to be ran when they join.");
                        return;
                    }
                }

                boolean res = false;
                try {
                    res = Bukkit.getScheduler().callSyncMethod( Main.getInstance(), () -> Bukkit.dispatchCommand(new MinePoSSender() , command ) ).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                JSONObject resJson = new JSONObject();
                resJson.put("command", jsonObject.get("command"));
                resJson.put("queued_id", jsonObject.get("queued_id"));
                resJson.put("action", "command-reply");
                resJson.put("response", res);
                if(!res) {
                    resJson.put("reason", "Command failed.");
                }
                send(resJson.toString());
                return;
            }
        }
    }

    @Override
    public void onMessage(ByteBuffer message) {
        System.out.println("received ByteBuffer");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }

    @Override
    public void onClosing(int code, String reason, boolean remote) {
        super.onClosing(code, reason, remote);
        if(remote){
            JavaPlugin main = JavaPlugin.getPlugin(Main.class);
            main.getLogger().info("Daemon closed connection, Disabling.");
            main.getServer().getPluginManager().disablePlugin(main);
        }
    }
}