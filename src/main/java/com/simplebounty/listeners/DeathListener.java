package com.simplebounty.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import com.simplebounty.Database.BountyEntry;
import com.simplebounty.Database.TargetDataBase;

public class DeathListener implements Listener {

    // Datenbank-Instanz um Kopfgelder abzurufen
    private final TargetDataBase dataBase;

    // Konstruktor – bekommt die Datenbank-Instanz von der Hauptklasse übergeben
    public DeathListener(TargetDataBase dataBase) {
        this.dataBase = dataBase;
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

        // Kopfgeld abrufen – nur einmal ✓
        BountyEntry entry = dataBase.getBounty(player.getName());
        Material mat = Material.matchMaterial(entry.material);

        // Sicherheitscheck falls Material ungültig
        if (mat == null) {
            killer.sendMessage("§cFehler: Das Kopfgeld-Item ist ungültig!");
            dataBase.removeBounty(player.getName());
            return;
        }

        // Belohnung dem Killer geben
        ItemStack reward = new ItemStack(mat, entry.amount);
        killer.getInventory().addItem(reward);
        killer.sendMessage("§aDu hast das Kopfgeld auf §f" + player.getName() + " §aeingelöst! Belohnung: §f" + entry.amount + "x " + mat.name());
        player.sendMessage("§cDas Kopfgeld auf dich wurde von §f" + killer.getName() + " §ceingelöst!");

        // Kopfgeld aus der Datenbank entfernen
        dataBase.removeBounty(player.getName());
    }
}