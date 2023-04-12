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

public class GetHighestBlockYAtHandler implements HttpHandler {
  private final Server minecraftServer;
  private final int port;
  private final Plugin plugin;

  public GetHighestBlockYAtHandler(Plugin plugin, int port, Server minecraftServer) {
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

  private static Optional<Integer> parseIntFromQuery(Map<String, String> query, String field) {
    return Optional.ofNullable(query.get(field)).flatMap(GetHighestBlockYAtHandler::parseInt);
  }

  @Override
  public void handle(HttpExchange header) {
    System.out.println("Calling [handle] for [HighestBlockYAtHandler].");
    OutputStream os = header.getResponseBody();
    URI uri = header.getRequestURI();
    Map<String, String> query = parseQuery(uri);
    Optional<Integer> optionalX = parseIntFromQuery(query, "x");
    Optional<Integer> optionalZ = parseIntFromQuery(query, "z");
    Optional<String> optionalMaterial = Optional.ofNullable(query.get("material"));

    Bukkit.getScheduler()
        .runTask(
            this.plugin,
            () -> {
              Optional<Integer> highestYBlock =
                optionalX.flatMap(x -> optionalZ.map(z -> minecraftServer.getWorld("world").getHighestBlockYAt(x, z)));


              if (highestYBlock.isPresent()) {
                String response = Integer.toString(highestYBlock.get());
                try {
                  header.sendResponseHeaders(200, response.length());
                  os.write(response.getBytes());
                  os.close();
                } catch (IOException e) {
                  plugin.getLogger().severe(e.toString());
                }
              } else {
                  try { 
                String response = "Not found :(";
                header.sendResponseHeaders(404, response.length());
                os.write(response.getBytes());
                os.close();
                  } catch (IOException e) {
                  plugin.getLogger().severe(e.toString());
                  }
              }

            });
  }
}
