package it.versy.stellarlevels.commands;

import it.versy.stellarlevels.StellarLevels;
import it.versy.stellarlevels.managers.DiscordWebhook;
import it.versy.stellarlevels.managers.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final StellarLevels plugin;
    private final LevelManager levelManager;
    private final DiscordWebhook discordWebhook;

    public CommandHandler(StellarLevels plugin) {
        this.plugin = plugin;
        this.levelManager = plugin.getLevelManager();

        // Inizializza il webhook di Discord con l'URL dal config
        String webhookUrl = plugin.getConfig().getString("discord.webhook-url");
        this.discordWebhook = new DiscordWebhook(webhookUrl);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("reloadls")) {
            plugin.reloadConfig();
            plugin.reloadRewardsConfig();
            sender.sendMessage("§aPlugin reloadato correttamente");

            return true;
        }

        if (command.getName().equalsIgnoreCase("livello")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int livello = levelManager.getLevel(player);
                double exp = levelManager.getExp(player);
                double expRequired = levelManager.getExpRequired(livello);
                player.sendMessage(plugin.getMessage("current-level")
                        .replace("%level%", String.valueOf(livello))
                        .replace("%current_exp%", String.format("%.2f", exp))
                        .replace("%exp_required%", String.format("%.2f", expRequired)));
            } else {
                sender.sendMessage(plugin.getMessage("non-player-command"));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("bossworld")) {
            if (sender instanceof Player player) {

                if (args.length > 0) {
                    // Recupera il mondo dal nome passato negli argomenti
                    World world = Bukkit.getWorld(args[0]);

                    if (world != null) {
                        // Creazione della mappa delle arene (probabilmente da configurazione)
                        Map<String, Integer> arene = new HashMap<>();

                        // Popolazione della mappa 'arene' dalla configurazione
                        for (String key : plugin.getConfig().getConfigurationSection("boss-worlds").getKeys(false)) {
                            // Presumo che tu abbia i livelli delle arene nella configurazione, li aggiungo alla mappa
                            int livelloArena = plugin.getConfig().getInt("boss-worlds." + key + ".livello");
                            arene.put(key, livelloArena);
                        }

                        // Controllo se la chiave esiste nella mappa delle arene
                        if (arene.containsKey(world.getName())) {
                            // Ottieni il livello del giocatore
                            int playerLevel = plugin.getLevelManager().getLevel(player);

                            // Controllo se il giocatore ha il livello richiesto per accedere al mondo
                            if (playerLevel >= arene.get(world.getName())) {
                                // Teletrasporto il giocatore alla posizione desiderata
                                Location loc = new Location(world, 10.0, 10.0, 10.0);
                                player.teleport(loc);
                                player.sendMessage("Ti sei teletrasportato in " + world.getName());
                            } else {
                                player.sendMessage("Non sei del livello idoneo. Livello necessario: " + arene.get(world.getName()));
                                player.sendMessage("Il tuo livello attuale è: " + playerLevel);
                            }
                        } else {
                            player.sendMessage("Il mondo " + world.getName() + " non è configurato come arena.");
                        }
                    } else {
                        player.sendMessage("Il mondo " + args[0] + " non esiste.");
                    }
                } else {
                    player.sendMessage("Devi specificare il nome del mondo.");
                }
            }
        }


        if (command.getName().equalsIgnoreCase("stellarlevels")) {
            if (args.length < 4) {
                sender.sendMessage(plugin.getMessage("command-usage-exp"));
                return true;
            }

            String targetType = args[0].toLowerCase();
            String operation = args[1].toLowerCase();
            double amount;
            Player target = Bukkit.getPlayer(args[3]);

            if (target == null) {
                sender.sendMessage(plugin.getMessage("player-not-found"));
                return true;
            }

            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("invalid-exp-amount"));
                return true;
            }

            switch (targetType) {
                case "level":
                    handleLevelCommand(sender, operation, amount, target);
                    break;
                case "exp":
                    handleExpCommand(sender, operation, amount, target);
                    break;
                default:
                    sender.sendMessage(plugin.getMessage("command-usage-exp"));
                    break;
            }

            // Invia il log al webhook di Discord
            logToDiscord(sender.getName(), targetType, operation, amount, target.getName());

            return true;
        }
        return false;
    }

    // Metodo per loggare il comando al webhook di Discord
    private void logToDiscord(String sender, String targetType, String operation, double amount, String targetPlayer) {
        String message = "**" + sender + "** ha eseguito il comando `/stellarlevels " +
                targetType + " " + operation + " " + amount + " " + targetPlayer + "`";
        discordWebhook.sendMessage(message);
    }

    // Gestione del comando /stellarlevels level
    private void handleLevelCommand(CommandSender sender, String operation, double amount, Player target) {
        switch (operation) {
            case "set":
                levelManager.setLevel(target, (int) amount);
                sender.sendMessage("Il livello di " + target.getName() + " è stato impostato a " + (int) amount + ".");
                break;
            case "add":
                int newLevel = levelManager.getLevel(target) + (int) amount;
                levelManager.setLevel(target, newLevel);
                sender.sendMessage("Il livello di " + target.getName() + " è stato incrementato di " + (int) amount + ".");
                break;
            case "take":
                int reducedLevel = levelManager.getLevel(target) - (int) amount;
                levelManager.setLevel(target, Math.max(1, reducedLevel));
                sender.sendMessage("Il livello di " + target.getName() + " è stato ridotto di " + (int) amount + ".");
                break;
            case "reset":
                levelManager.setLevel(target, 1);
                sender.sendMessage("Il livello di " + target.getName() + " è stato resettato.");
                break;
            default:
                sender.sendMessage(plugin.getMessage("command-usage-exp"));
                break;
        }
    }

    // Gestione del comando /levelsystem exp
    private void handleExpCommand(CommandSender sender, String operation, double amount, Player target) {
        switch (operation) {
            case "set":
                levelManager.setLevel(target, (int) amount); // Setta il livello e resetta l'exp
                levelManager.addExp(target, amount);
                sender.sendMessage("L'EXP di " + target.getName() + " è stata impostata a " + amount + ".");
                break;
            case "add":
                levelManager.addExp(target, amount);
                sender.sendMessage(amount + " EXP aggiunta a " + target.getName() + ".");
                break;
            case "take":
                double newExp = Math.max(0, levelManager.getExp(target) - amount);
                levelManager.addExp(target, newExp - levelManager.getExp(target));
                sender.sendMessage(amount + " EXP rimossa da " + target.getName() + ".");
                break;
            case "reset":
                levelManager.setLevel(target, 1); // Resetta anche l'EXP
                sender.sendMessage("L'EXP di " + target.getName() + " è stata resettata.");
                break;
            default:
                sender.sendMessage(plugin.getMessage("command-usage-exp"));
                break;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("stellarlevels")) {
            if (args.length == 1) {
                return Arrays.asList("level", "exp");
            }
            if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
                return Arrays.asList("set", "add", "take", "reset");
            }
            if (args.length == 3 && !args[0].equalsIgnoreCase("reload")) {
                if(!(args[2].equalsIgnoreCase("reset"))) {
                    return Collections.singletonList("<amount>");
                } else {
                    List<String> playerNames = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        playerNames.add(player.getName());
                    }
                    return playerNames;
                }
            }
            if (args.length == 4 && !(args[0].equalsIgnoreCase("reload") || args[2].equalsIgnoreCase("reset"))) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return playerNames;
            }
        }
        return null;
    }
}
