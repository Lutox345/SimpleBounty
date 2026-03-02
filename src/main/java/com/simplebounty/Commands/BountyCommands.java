package com.simplebounty.Commands;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import com.simplebounty.Database.BountyEntry;
import com.simplebounty.Database.TargetDataBase;

public class BountyCommands implements CommandExecutor, TabCompleter {

    private final List<String> subCommands = List.of("set", "list");
    private final TargetDataBase dataBase;

    public BountyCommands(TargetDataBase dataBase) {
        this.dataBase = dataBase;
    }

    // ItemStack → Base64-String (inkl. Enchantments, NBT etc.)
    private String serialize(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    // Base64-String → ItemStack
    private ItemStack deserialize(String data) {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(data));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§e--- SimpleBounty Hilfe ---");
            player.sendMessage("§f/bounty set <spieler> + Items in deiner Hand -> Preis §7- Setzt ein Kopfgeld");
            player.sendMessage("§f/bounty list §7- Zeigt alle aktiven Kopfgelder");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "set" -> {
                if (args.length < 2) {
                    player.sendMessage("§fUsage: /bounty set <spieler>");
                    return true;
                }

                String targetName = args[1];
                Player targetPlayer = player.getServer().getPlayer(targetName);

                if (targetPlayer == null) {
                    player.sendMessage("§fDer Spieler ist nicht online!");
                    return true;
                }

                if (targetPlayer.equals(player)) {
                    player.sendMessage("§fDu kannst kein Kopfgeld auf dich selbst setzen!");
                    return true;
                }

                ItemStack itemInHand = player.getInventory().getItemInMainHand();

                // AIR-Check vor dem Entfernen
                if (itemInHand.getType() == Material.AIR) {
                    player.sendMessage("§fDu musst den Kopfgeld-Preis in der Hand halten!");
                    return true;
                }

                int amount = itemInHand.getAmount();
                String serialized = serialize(itemInHand); // inkl. Enchantments ✓

                // Item entfernen
                itemInHand.setAmount(0);

                dataBase.setBounty(targetName, serialized, amount);
                player.sendMessage("§aKopfgeld auf §f" + targetPlayer.getName() + " §agesetzt! Preis: §f" + amount + "x " + itemInHand.getType().name());
                targetPlayer.sendMessage("§cEin Kopfgeld von §f" + amount + "x " + itemInHand.getType().name() + " §cwurde auf dich gesetzt!");
            }

            case "list" -> {
                var bounties = dataBase.getAllBounties();

                if (bounties.isEmpty()) {
                    player.sendMessage("§fEs gibt noch keine aktiven Kopfgelder!");
                } else {
                    player.sendMessage("§e--- Aktive Kopfgelder ---");
                    bounties.forEach((target, entry) -> {
                        // ItemStack deserialisieren um den Namen anzuzeigen
                        ItemStack item = deserialize(entry.itemStackJson);
                        player.sendMessage("§f" + target + " §7-> §e" + entry.amount + "x " + item.getType().name());
                    });
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
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    suggestions.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
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