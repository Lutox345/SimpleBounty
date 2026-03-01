package com.simplebounty;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import com.simplebounty.Commands.BountyCommands;
import com.simplebounty.Database.TargetDataBase;
import com.simplebounty.listeners.DeathListener;

/*
 * simplebounty java plugin
 */
public class Plugin extends JavaPlugin
{
  private static final Logger LOGGER=Logger.getLogger("simplebounty");

  public void onEnable()
  {
    LOGGER.info("SimpleBounty enabled");

    TargetDataBase database = new TargetDataBase(getDataFolder().toPath().resolve("bounties.json"));
    BountyCommands bountyCommands = new BountyCommands(database);
    getServer().getPluginManager().registerEvents(new DeathListener(database), this);

    getCommand("bounty").setExecutor(bountyCommands);
    getCommand("bounty").setTabCompleter(bountyCommands);


    
  }

  public void onDisable()
  {
    LOGGER.info("SimpleBounty disabled");
  }
}
