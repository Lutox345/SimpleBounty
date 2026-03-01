package com.simplebounty.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import com.simplebounty.Database.TargetDataBase;

public class DeathListener implements Listener {

    // Datenbank-Instanz um Kopfgelder abzurufen
    private final TargetDataBase dataBase;

    // Konstruktor – bekommt die Datenbank-Instanz von der Hauptklasse übergeben
    public DeathListener(TargetDataBase dataBase) {
        this.dataBase = dataBase;
    }

    @EventHandler // ← fehlte, ohne das wird das Event nie aufgerufen
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity(); // final hier nicht nötig

        // Überprüft ob der gestorbene Spieler ein Kopfgeld hat
        if (!dataBase.hasBounty(player.getName())) {
            return;
        }

        // Killer ermitteln – kann null sein (z.B. Fallschaden)
        Player killer = player.getKiller();
        if (killer == null) {
            return;
        }

        // Kopfgeld abrufen und dem Killer geben
        var prize = dataBase.getBounty(player.getName());
        killer.getInventory().addItem(new org.bukkit.inventory.ItemStack(prize));
        killer.sendMessage("§aDu hast das Kopfgeld auf §f" + player.getName() + " §aeingelöst!");
        player.sendMessage("§cDas Kopfgeld auf dich wurde eingelöst!");

        // Kopfgeld aus der Datenbank entfernen
        dataBase.removeBounty(player.getName());
    }
}