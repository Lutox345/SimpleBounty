package com.simplebounty.listeners;

import java.util.Base64;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import com.simplebounty.Database.BountyEntry;
import com.simplebounty.Database.TargetDataBase;

public class DeathListener implements Listener {

    private final TargetDataBase dataBase;

    public DeathListener(TargetDataBase dataBase) {
        this.dataBase = dataBase;
    }

    // Base64-String → ItemStack
    private ItemStack deserialize(String data) {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(data));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Überprüft ob der gestorbene Spieler ein Kopfgeld hat
        if (!dataBase.hasBounty(player.getName())) {
            return;
        }

        // Killer ermitteln – kann null sein (z.B. Fallschaden)
        Player killer = player.getKiller();
        if (killer == null) {
            return;
        }

        // Kopfgeld abrufen und deserialisieren – Enchantments bleiben erhalten ✓
        BountyEntry entry = dataBase.getBounty(player.getName());
        ItemStack reward = deserialize(entry.itemStackJson);

        // Belohnung dem Killer geben
        killer.getInventory().addItem(reward);
        killer.sendMessage("§aDu hast das Kopfgeld auf §f" + player.getName() + " §aeingelöst! Belohnung: §f" + entry.amount + "x " + reward.getType().name());
        player.sendMessage("§cDas Kopfgeld auf dich wurde von §f" + killer.getName() + " §ceingelöst!");

        // Kopfgeld aus der Datenbank entfernen
        dataBase.removeBounty(player.getName());
    }
}