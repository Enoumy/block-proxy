package me.enoumy.hellomcplugin2;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class BlockProxy {
  HttpServer httpServer;

  public BlockProxy(Plugin plugin, int httpServerPort, Server minecraftServer) throws IOException {
    this.httpServer = HttpServer.create(new InetSocketAddress(httpServerPort), 0);
    httpServer.createContext(
        "/setBlock", new SetBlockHandler(plugin, httpServerPort, minecraftServer));
    httpServer.createContext(
        "/getHighestBlockYAt", new GetHighestBlockYAtHandler(plugin, httpServerPort, minecraftServer));
  }

  public void start() {
    httpServer.setExecutor(null);
    httpServer.start();
  }

  public void stop() {
    httpServer.stop(0);
  }
}
