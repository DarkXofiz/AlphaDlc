package miranware.util.party.connection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.Generated;
import net.minecraft.util.Formatting;
import miranware.util.IMinecraft;
import miranware.util.chat.ChatUtil;
import miranware.util.party.PartyPlayerPos;

public final class PartyApiClient implements IMinecraft {
   private static final HttpClient HTTP = HttpClient.newHttpClient();
   private static final String BASE_URL = "http://bybybybyvich.pythonanywhere.com";
   private static final ExecutorService PARTY_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "Party-API-Thread");
      t.setDaemon(true);
      return t;
   });
   private static volatile List<PartyPlayerPos> cached = List.of();

   public static void postAsync(String path, JsonObject body, Consumer<JsonObject> callback) {
      HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create("http://bybybybyvich.pythonanywhere.com" + path))
         .POST(BodyPublishers.ofString(body.toString()))
         .header("Content-Type", "application/json")
         .build();
      HTTP.sendAsync(request, BodyHandlers.ofString()).thenApplyAsync(HttpResponse::body, PARTY_EXECUTOR).thenAcceptAsync(text -> {
         try {
            if (!text.startsWith("{")) {
               return;
            }

            JsonObject json = JsonParser.parseString(text).getAsJsonObject();
            mc.execute(() -> callback.accept(json));
         } catch (Exception var3x) {
         }
      }, PARTY_EXECUTOR).exceptionally(e -> {
         e.printStackTrace();
         return null;
      });
   }

   public static void fetchPartyStateAsync() {
      if (mc.player != null) {
         JsonObject req = new JsonObject();
         req.addProperty("player", mc.player.getNameForScoreboard());
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://bybybybyvich.pythonanywhere.com/party/state"))
            .POST(BodyPublishers.ofString(req.toString()))
            .header("Content-Type", "application/json")
            .build();
         HTTP.sendAsync(request, BodyHandlers.ofString()).thenAcceptAsync(response -> {
            try {
               if (response.statusCode() != 200) {
                  return;
               }

               String body = response.body().trim();
               if (!body.startsWith("{")) {
                  return;
               }

               JsonObject json = JsonParser.parseString(body).getAsJsonObject();
               JsonArray arr = json.getAsJsonArray("members");
               List<PartyPlayerPos> list = new ArrayList<>();

               for (JsonElement e : arr) {
                  JsonObject o = e.getAsJsonObject();
                  list.add(new PartyPlayerPos(o.get("playerId").getAsString(), o.get("x").getAsDouble(), o.get("y").getAsDouble(), o.get("z").getAsDouble()));
               }

               cached = list;
            } catch (Exception var8) {
            }
         });
      }
   }

   public static void fetchInvitesAsync() {
      if (mc.player != null) {
         JsonObject req = new JsonObject();
         req.addProperty("player", mc.player.getNameForScoreboard());
         postAsync(
            "/party/invites",
            req,
            json -> {
               for (JsonElement e : json.getAsJsonArray("invites")) {
                  String party = e.getAsString();
                  ChatUtil.send(
                     Formatting.GRAY
                        + "You were invited to the party "
                        + Formatting.WHITE
                        + party
                        + Formatting.GRAY
                        + ", type "
                        + Formatting.WHITE
                        + ".party join "
                        + party
                  );
               }
            }
         );
      }
   }

   @Generated
   public static List<PartyPlayerPos> getCached() {
      return cached;
   }
}
