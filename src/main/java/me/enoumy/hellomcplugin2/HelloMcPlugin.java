package me.enoumy.hellomcplugin2;

import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;

public class HelloMcPlugin extends JavaPlugin {

  private BlockProxy blockProxy;

  @Override
  public void onEnable() {
    try {
      blockProxy = new BlockProxy(this, 9191, getServer());
      blockProxy.start();
    } catch (IOException e) {
      getLogger().severe(e.toString());
    }
  }

  @Override
  public void onDisable() {
    try {
      blockProxy.stop();
    } catch (Exception e) {
      getLogger().severe(e.toString());
    }
  }
}
