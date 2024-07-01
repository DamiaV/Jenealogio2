package net.darmo_creations.jenealogio2.utils;

import com.google.gson.*;
import net.darmo_creations.jenealogio2.model.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class provides a {@link #geoCode(String)} method that returns the coordinates of any address.
 * <p>
 * Coordinates are cached.
 */
public final class GeoCoder {
  private static final String BASE_URL = "https://nominatim.openstreetmap.org/search?q=%s&format=json";
  private static final HashMap<String, LatLon> LAT_LON_CACHE = new HashMap<>();
  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .followRedirects(HttpClient.Redirect.NORMAL)
      .connectTimeout(Duration.ofSeconds(30))
      .build();

  /**
   * Get the latitude and longitude of the given address.
   * <p>
   * Coordinates are cached.
   *
   * @param address The address to get the coordinates of.
   * @return A {@link CompletableFuture} that upon completion provides the address’ coordinates,
   * or an empty value if none were found or a network error occured.
   */
  @SuppressWarnings("unchecked")
  public static CompletableFuture<Optional<LatLon>> geoCode(@NotNull String address) {
    final String strippedAddress = address.strip();
    if (LAT_LON_CACHE.containsKey(strippedAddress))
      return CompletableFuture.completedFuture(Optional.of(LAT_LON_CACHE.get(strippedAddress)));

    final CompletableFuture<HttpResponse<String>> future;
    try {
      future = getHttp(new URL(BASE_URL.formatted(URLEncoder.encode(strippedAddress, StandardCharsets.UTF_8))));
    } catch (final IOException e) {
      return CompletableFuture.completedFuture(Optional.empty());
    }

    return future.thenApply(resp -> {
      if (resp.statusCode() != 200)
        return Optional.empty();
      final Gson gson = new Gson();
      final List<Map<String, ?>> data = gson.fromJson(resp.body(), List.class);
      for (final var entry : data)
        if (entry.containsKey("lat") && entry.containsKey("lon")) {
          final double lat = Double.parseDouble(String.valueOf(entry.get("lat")));
          final double lon = Double.parseDouble(String.valueOf(entry.get("lon")));
          final LatLon latLon = new LatLon(lat, lon);
          LAT_LON_CACHE.put(strippedAddress, latLon);
          return Optional.of(latLon);
        }
      return Optional.empty();
    });
  }

  private static CompletableFuture<HttpResponse<String>> getHttp(@NotNull URL url) throws IOException {
    final HttpRequest request;
    try {
      request = HttpRequest.newBuilder()
          .uri(url.toURI())
          .timeout(Duration.ofMinutes(1))
          .GET()
          .build();
    } catch (final URISyntaxException e) {
      throw new IOException(e);
    }
    return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
  }

  private GeoCoder() {
  }
}
