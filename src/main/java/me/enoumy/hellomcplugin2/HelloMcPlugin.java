package me.enoumy.hellomcplugin2;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class HelloMcPlugin extends JavaPlugin {

    private BlockProxy blockProxy;

    @Override
    public void onEnable() {
        getLogger().info("onEnable is called!");
        try {
            blockProxy = new BlockProxy(9191, getServer());
            blockProxy.start();
        } catch (IOException e) {
            getLogger().severe(e.toString());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("onDisable is called!");
        try {
            blockProxy.stop();
        } catch (Exception e) {
            getLogger().severe(e.toString());
        }
    }
}
