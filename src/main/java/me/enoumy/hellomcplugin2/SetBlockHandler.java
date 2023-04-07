package me.enoumy.hellomcplugin2;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

public class SetBlockHandler implements HttpHandler {
  private final Server minecraftServer;
  private final int port;
  private final Plugin plugin;

  public SetBlockHandler(Plugin plugin, int port, Server minecraftServer) {
    this.minecraftServer = minecraftServer;
    this.port = port;
    this.plugin = plugin;
  }

  private static Optional<Integer> parseInt(String s) {
    try {
      return Optional.of(Integer.parseInt(s));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private static Map<String, String> parseQuery(URI uri) {
    Stream<Optional<Tuple2<String, String>>> optionalStream =
        Arrays.stream(uri.getQuery().split("&"))
            .map(
                (queryElement) -> {
                  String[] parts = queryElement.split("=");
                  if (parts.length != 2) {
                    return Optional.empty();
                  }
                  String key = parts[0];
                  String value = parts[1];
                  return Optional.of(new Tuple2<>(key, value));
                });

    Stream<Optional<Tuple2<String, String>>> optionalStream2 =
        optionalStream.filter(Optional::isPresent);

    var optionalStream3 =
        optionalStream2.map(
            x -> {
              assert x.isPresent();
              return x.get();
            });

    return optionalStream3.collect(Collectors.toMap(Tuple2::fst, Tuple2::snd));
  }

  private void setBlock(int x, int y, int z, String materialName) {
    String barMessage = String.format("(%d, %d): %s", x, y, materialName);
    minecraftServer.broadcastMessage(barMessage);

    Optional<World> optionalWorld = Optional.ofNullable(minecraftServer.getWorld("world"));
    optionalWorld.ifPresent(
        world -> {
          Location location = new Location(world, x, y, z);
          Block oldBlock = world.getBlockAt(location);
          Material material = Material.matchMaterial(materialName);
          assert material != null;
          oldBlock.setType(material, true);
        });
  }

  private static Optional<Integer> parseIntFromQuery(Map<String, String> query, String field) {
    return Optional.ofNullable(query.get(field)).flatMap(SetBlockHandler::parseInt);
  }

  @Override
  public void handle(HttpExchange header) {
    System.out.println("Calling [handle] for http response!");
    OutputStream os = header.getResponseBody();
    URI uri = header.getRequestURI();
    Map<String, String> query = parseQuery(uri);
    Optional<Integer> optionalX = parseIntFromQuery(query, "x");
    Optional<Integer> optionalY = parseIntFromQuery(query, "y");
    Optional<Integer> optionalZ = parseIntFromQuery(query, "z");
    Optional<String> optionalMaterial = Optional.ofNullable(query.get("material"));

    Bukkit.getScheduler()
        .runTask(
            this.plugin,
            () -> {
              optionalX.ifPresent(
                  x ->
                      optionalY.ifPresent(
                          y ->
                              optionalZ.ifPresent(
                                  z ->
                                      optionalMaterial.ifPresent(
                                          material -> setBlock(x, y, z, material)))));

              String response =
                  "<h1>/setblock</h1>"
                      + "<h3>Port: "
                      + port
                      + "</h3>"
                      + "<p> Query is: "
                      + query
                      + "</p>"
                      + "(x, y): ("
                      + optionalX
                      + ", "
                      + optionalY
                      + ")";
              try {
                header.sendResponseHeaders(200, response.length());
                os.write(response.getBytes());
                os.close();
              } catch (IOException e) {
                plugin.getLogger().severe(e.toString());
              }
            });
  }
}
