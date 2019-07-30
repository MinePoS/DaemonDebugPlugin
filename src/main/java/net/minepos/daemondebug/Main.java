package net.minepos.daemondebug;

import net.minepos.daemondebug.events.JoinHandler;
import net.minepos.daemondebug.objects.QueuedCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin {



    public static Main instance;
    public WebSocketClient client;
    public static Main getInstance(){return instance;}

    public HashMap<UUID, HashMap<Integer ,QueuedCommand>> queuedCommands;

    @Override
    public void onEnable() {
        queuedCommands = new HashMap<>();
        super.onEnable();
        instance = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new JoinHandler(), this);
        if(getConfig().getString("apikey") == ""){
            getLogger().info("API Key not set, Disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }

        String apikey = getConfig().getString("apikey");
        String hostname = getConfig().getString("daemon.host");
        int port = getConfig().getInt("daemon.port");

        client = null;
        try {
            client = new DaemonClient(new URI("ws://"+hostname+":"+port+"/Server:"+apikey));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        client.connect();

    }
}
