package com.simplebounty.Commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.simplebounty.Database.BountyEntry;
import com.simplebounty.Database.TargetDataBase;
import org.jetbrains.annotations.Nullable;

public class BountyCommands implements CommandExecutor, TabCompleter {

    // Alle verfügbaren Subcommands
    private final List<String> subCommands = List.of("set", "list");

    // Instanz der Datenbank um Kopfgelder zu speichern und abzurufen
    private final TargetDataBase dataBase;

    // Konstruktor – bekommt die Datenbank-Instanz von der Hauptklasse übergeben
    public BountyCommands(TargetDataBase dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Konsole kann den Befehl nicht ausführen
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen!");
            return true;
        }

        // Kein Argument angegeben → Hilfe anzeigen
        if (args.length == 0) {
            player.sendMessage("§e--- SimpleBounty Hilfe ---");
            player.sendMessage("§f/bounty set <spieler> + Items in deiner Hand -> Preis §7- Setzt ein Kopfgeld");
            player.sendMessage("§f/bounty list §7- Zeigt alle aktiven Kopfgelder");
            return true;
        }

        switch (args[0].toLowerCase()) {

            // Kopfgeld auf einen Spieler setzen
            case "set" -> {

                // Überprüft ob ein Spielername angegeben wurde
                if (args.length < 2) {
                    player.sendMessage("§fUsage: /bounty set <spieler>");
                    return true;
                }

                // Ziel-Spieler anhand des Namens suchen
                String targetName = args[1];
                Player targetPlayer = player.getServer().getPlayer(targetName);

                // Überprüft ob der Zielspieler online ist
                if (targetPlayer == null) {
                    player.sendMessage("§fDer Spieler ist nicht online!");
                    return true;
                }

                // Überprüft ob der Spieler versucht ein Kopfgeld auf sich selbst zu setzen
                if (targetPlayer.equals(player)) {
                    player.sendMessage("§fDu kannst kein Kopfgeld auf dich selbst setzen!");
                    return true;
                }

                // Item in der Hand auslesen
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                Material prize = itemInHand.getType();
                int amount = itemInHand.getAmount();

                // AIR-Check VOR dem Entfernen ✓
                if (prize == Material.AIR) {
                    player.sendMessage("§fDu musst den Kopfgeld-Preis in der Hand halten!");
                    return true;
                }

                // Item entfernen damit man es nicht verdoppeln kann
                itemInHand.setAmount(0);

                // Kopfgeld in der Datenbank speichern – material als String, + Menge
                dataBase.setBounty(targetName, prize.name(), amount);
                player.sendMessage("§aKopfgeld auf §f" + targetPlayer.getName() + " §agesetzt! Preis: §f" + amount + "x " + prize.name());
                targetPlayer.sendMessage("§cEin Kopfgeld von §f" + amount + "x " + prize.name() + " §cwurde auf dich gesetzt!");
            }

            // Alle aktiven Kopfgelder auflisten
            case "list" -> {
                var bounties = dataBase.getAllBounties();

                // Überprüft ob überhaupt Kopfgelder vorhanden sind
                if (bounties.isEmpty()) {
                    player.sendMessage("§fEs gibt noch keine aktiven Kopfgelder!");
                } else {
                    player.sendMessage("§e--- Aktive Kopfgelder ---");
                    // Jeden Eintrag als "Spieler -> Menge x Material" ausgeben
                    bounties.forEach((target, entry) ->
                        player.sendMessage("§f" + target + " §7-> §e" + entry.amount + "x " + entry.material)
                    );
                }
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // /bounty <tab> → Subcommands vorschlagen
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    suggestions.add(sub);
                }
            }

        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            // /bounty set <tab> → Online-Spieler vorschlagen (außer sich selbst)
            if (sender instanceof Player player) {
                for (Player p : player.getServer().getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase()) && !p.equals(player)) {
                        suggestions.add(p.getName());
                    }
                }
            }
        }

        return suggestions;
    }
}