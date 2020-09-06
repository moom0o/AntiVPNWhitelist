package me.moomoo.antivpnwhitelist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;

public class Main extends JavaPlugin implements Listener {
    public FileConfiguration config = getConfig();
    public static HashSet cachedvpnips = new HashSet();
    public static HashSet cachedlegitips = new HashSet();
    public static HashSet<Player> allowed = new HashSet<Player>();
    private HashSet<Player> moved = new HashSet<Player>();
    private HashSet<Player> silentleave = new HashSet<Player>();
    public static String mitigation = "false";
    public static String mitigation_toggle = "false";

    public static int vpnusers = 0;
    public void onEnable() {
        saveDefaultConfig();
        System.out.println("[ENABLED] AnarchyExploitFixes - Made by moomoo");
        Bukkit.getServer().getPluginManager().registerEvents((Listener) this, this);
        int pluginId = 8768; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);
    }

    @EventHandler
    public void onPlayerJoinPre(AsyncPlayerPreLoginEvent evt) throws IOException, ParseException {
        if(mitigation == "true"){
            System.out.println("Mitigation is currently enabled");
            String ip = evt.getAddress().toString().replace("/", "").replaceAll(":(.*)", "");
            String player = evt.getName();

            if(checkVPN(ip, player)) {
                evt.setKickMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.BotAttackMode")));
                evt.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                System.out.println(evt.getName() + " failed to login because mitigation mode is enabled");
            }

        }
    }
    @EventHandler
    public void onMessage(AsyncPlayerChatEvent evt){
        Player player = evt.getPlayer();
        if(!allowed.contains(player)){
            getConfig().getList("messages.UnableToChatMessage").forEach(b -> {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', (String) b));
            });
            evt.setCancelled(true);
            System.out.println(evt.getPlayer().getName() + " failed to say " + evt.getMessage() + " because vpn");
        }
        if(!moved.contains(player)){
            if(getConfig().getBoolean("options.MoveToChat")){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.MoveToChat")));
                evt.setCancelled(true);
                System.out.println(evt.getPlayer().getName() + " failed to say " + evt.getMessage() + " because hasnt moved");
            }
        }
    }
    @EventHandler
    public void onMove(PlayerMoveEvent evt){
        if(getConfig().getBoolean("options.MoveToChat")){
            if(!moved.contains(evt.getPlayer())){
                moved.add(evt.getPlayer());
            }
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) throws IOException, ParseException {
        if(getConfig().getBoolean("options.DoNotSendJoinMessage")) {
            evt.setJoinMessage("");
        }
        allowed.add(evt.getPlayer());
        if(mitigation == "false"){
            String ip = evt.getPlayer().getAddress().toString().replace("/", "").replaceAll(":(.*)", "");
            String player = evt.getPlayer().getName();

            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        boolean isVPN = checkVPN(ip, player);
                        if(isVPN) {
                            if(getConfig().getBoolean("options.DoNotSendJoinMessage")) {
                                System.out.println("§4[VPN] " + "§7" + evt.getPlayer().getName() + " joined");
                            }
                            System.out.println(player + " is not allowed to talk because " + ip + " is a vpn.");
                            allowed.remove(evt.getPlayer());
                            vpnusers = vpnusers + 1;
                            if(vpnusers > 9){
                                if(mitigation_toggle == "false"){
                                    mitigation = "true";
                                }
                            }
                        } else {
                            if(getConfig().getBoolean("options.DoNotSendJoinMessage")) {
                                if (Bukkit.getOnlinePlayers().size() < getConfig().getInt("options.MaxPlayersJoinmessages")) {
                                    Bukkit.broadcastMessage("§7" + evt.getPlayer().getName() + " joined the game.");
                                }
                            }
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }

                }
            });

            t.start();


        }
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent evt){
        if(getConfig().getBoolean("options.DoNotSendJoinMessage")){
            evt.setQuitMessage("");
        }
        if(!allowed.contains(evt.getPlayer())){
            vpnusers = vpnusers - 1;
            if(vpnusers < 9){
                if(mitigation_toggle == "false"){
                    mitigation = "false";
                }
            }
        }
        if(getConfig().getBoolean("options.DoNotSendJoinMessage")) {
            if (allowed.contains(evt.getPlayer()) && !silentleave.contains(evt.getPlayer())) {
                if (Bukkit.getOnlinePlayers().size() < getConfig().getInt("options.MaxPlayersJoinmessages")) {
                    Bukkit.broadcastMessage("§7" + evt.getPlayer().getName() + " left the game.");
                }
            } else {
                System.out.println("§4[VPN] " + "§7" + evt.getPlayer().getName() + " left");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent evt){
        Player player = evt.getPlayer();
        if(!allowed.contains(player)){
            getConfig().getList("messages.UnableToChatMessage").forEach(b -> {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', (String) b));
            });
            evt.setCancelled(true);
            System.out.println(evt.getPlayer().getName() + " failed to run " + evt.getMessage() + " because vpn");
        }
        if(!moved.contains(player)){
            if(getConfig().getBoolean("options.MoveToChat")){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.MoveToChat")));
                evt.setCancelled(true);
                System.out.println(evt.getPlayer().getName() + " failed to run " + evt.getMessage() + " because hasnt moved");
            }
        }
    }

    public boolean checkVPN(String ip, String username) throws IOException, ParseException {
        if(checkWhitelist(username)){
            return false;
        }
        if(checkipcache(ip) == "vpn"){
            return true;
        }
        if(checkipcache(ip) == "good"){
            return false;
        } else {
            URL url = new URL("https://proxycheck.io/v2/" + ip + "?vpn=1&asn=1&key=" + getConfig().getString("proxycheck_api_key"));
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) connection;
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response.toString());
            JSONObject data  = (JSONObject) json.get(ip);
            if(data.get("proxy").toString().toLowerCase().startsWith("yes")){
                cachedvpnips.add(ip);
                return true;
            } else {
                cachedlegitips.add(ip);
                return false;
            }
        }
    }

    static boolean checkWhitelist(String username) throws IOException, ParseException {
        URL url = new URL("https://whitelist.1b1t.tk/checkifwhitelisted?player=" + username);
        URLConnection connection = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) connection;
        BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONParser parser = new JSONParser();
        System.out.println(response.toString());

        JSONObject json = (JSONObject) parser.parse(response.toString());
        if(json.get("proxy") == "yes"){
            return true;
        } else {
            return false;
        }
    }

    static String checkipcache(String ip){
        if(cachedvpnips.contains(ip)){
            return "vpn"; // is vpn
        } else {
            if(cachedlegitips.contains(ip)){
                return "good";
            }
        }
        return "not_cached";
    }
}
