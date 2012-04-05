package me.quaz3l.qQuests;

import java.util.logging.Logger;

import me.quaz3l.qQuests.API.QuestAPI;
import me.quaz3l.qQuests.API.TaskTypes.Damage;
import me.quaz3l.qQuests.API.TaskTypes.Destroy;
import me.quaz3l.qQuests.API.TaskTypes.Distance;
import me.quaz3l.qQuests.API.TaskTypes.Enchant;
import me.quaz3l.qQuests.API.TaskTypes.GoTo;
import me.quaz3l.qQuests.API.TaskTypes.Kill;
import me.quaz3l.qQuests.API.TaskTypes.Kill_Player;
import me.quaz3l.qQuests.API.TaskTypes.Place;
import me.quaz3l.qQuests.API.TaskTypes.Tame;
import me.quaz3l.qQuests.Plugins.Commands;
import me.quaz3l.qQuests.Plugins.Signs;
import me.quaz3l.qQuests.Util.Chat;
import me.quaz3l.qQuests.Util.Config;
import me.quaz3l.qQuests.Util.Interwebs;
import me.quaz3l.qQuests.Util.LegacyConverter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class qQuests extends JavaPlugin
{
	// General Setup
	public static qQuests plugin;
	public final Logger logger = Logger.getLogger(("Minecraft"));
	public Config Config;
	public QuestAPI qAPI;
	
	
	// Economy
	public Economy economy = null;
	public Permission permission = null;
	
	// Prefixes
	public final String chatPrefix = ChatColor.AQUA + "[" + ChatColor.LIGHT_PURPLE + "qQuests" + ChatColor.AQUA + "] " + ChatColor.LIGHT_PURPLE + " ";
	public final String prefix = "[qQuests] ";	
	
	public qQuests() {
		super();
		qQuests.plugin = this;
	}
	
	@Override
	public void onDisable() 
	{
		Chat.logger("info", "v" + this.getDescription().getVersion() + " by Quaz3l: Disabled");
	}

	@Override
	public void onEnable() 
	{
		// Setup The Config Class For Later Use
		this.setupConfig();
		
		// Get The API
		this.qAPI = new QuestAPI();
		
		// Setup Player Profiles
		this.qAPI.getProfiles().initializePlayerProfiles();
		
		// Find Economy
		this.setupEconomy();
		
		// Setup Permissions
		this.setupPermissions();
		
		// Setup Task Types
		this.setupTaskTypes();
		
		// Build Quests
		this.qAPI.getQuestWorker().buildQuests();
		
		//Start Stock qPlugins
		this.stockPlugins();
		
		// Check For Updates And Ping Server
		Interwebs.repeatCheck();
		
		// Notify Logger
		Chat.logger("info", "by Quaz3l: Enabled");
	}
	
	// Setup The Config Class For Later Use
	private void setupConfig()
	{
		// Get The Config
		this.Config = new Config();
		
		// Converts Old quests.yml
		if(Config.getQuestConfig().getString("0.info.name") != null)
			LegacyConverter.convert();
		
		// Initialize The Configuration File
		Config.initializeConfig();
		Config.initializeQuestConfig();
		
		// Debugging
		// Config.dumpQuestConfig();
	}
	// Hooks Into The Economy Plugin
	private void setupEconomy()
	{
		if(getServer().getPluginManager().isPluginEnabled("Vault")) {
			RegisteredServiceProvider<Economy> economyProvider =
					getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				this.economy = economyProvider.getProvider();
				Chat.logger("info", "[Economy] Enabled - Vault And Economy Plugin Found.");
			}
			else
			{
				Chat.logger("warning", "################################################################");
				Chat.logger("warning", "[Economy] Disabled - Economy Plugin Not Found! Go Dowmnload An Economy Plugin From: http://dev.bukkit.org/server-mods/iconomy");
				Chat.logger("warning", "################################################################");
			}
		}
		else
		{
			Chat.logger("warning", "################################################################");
			Chat.logger("warning", "[Economy] Disabled - Vault Not Found! Go Download Vault From: http://dev.bukkit.org/server-mods/vault");
			Chat.logger("warning", "################################################################");
		}
	}
	// Hook Into The Permissions Plugin
	private void setupPermissions()
    {
		if(getServer().getPluginManager().isPluginEnabled("Vault")) {
			RegisteredServiceProvider<Permission> permissionProvider =
					getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if (permissionProvider != null) {
				this.permission = permissionProvider.getProvider();
				Chat.logger("info", "[Permissions] Enabled - Vault And Permissions Plugin Found.");
			}
			else
			{
				Chat.logger("warning", "################################################################");
				Chat.logger("warning", "[Permissions] Disabled - Permissions Plugin Not Found! Go Dowmnload An Permissions Plugin From: http://dev.bukkit.org/server-mods/bpermissions");
				Chat.logger("warning", "################################################################");
			}
		}
		else
		{
			Chat.logger("warning", "################################################################");
			Chat.logger("warning", "[Permissions] Disabled - Vault Not Found! Go Download Vault From: http://dev.bukkit.org/server-mods/vault");
			Chat.logger("warning", "################################################################");
		}
    }
	
	// Setup Task Types
	private void setupTaskTypes()
	{
		// Collect Is Handled By The Command
		getServer().getPluginManager().registerEvents(new Damage(), this);
		getServer().getPluginManager().registerEvents(new Destroy(), this);
		getServer().getPluginManager().registerEvents(new Place(), this);
		
		getServer().getPluginManager().registerEvents(new Distance(), this);
		getServer().getPluginManager().registerEvents(new GoTo(), this);
		
		getServer().getPluginManager().registerEvents(new Kill_Player(), this);
		getServer().getPluginManager().registerEvents(new Kill(), this);
		getServer().getPluginManager().registerEvents(new Tame(), this);
		
		getServer().getPluginManager().registerEvents(new Enchant(), this);
	}
	
	// Starts The Stock Plugins
	private void stockPlugins()
	{
		qPluginCommands();
		qPluginSigns();
		//qPluginNPCs();
	}
	private void qPluginCommands()
	{
		// Setup Command Executors
		CommandExecutor cmd = new Commands();
			getCommand("Q").setExecutor(cmd);
			getCommand("QU").setExecutor(cmd);
			getCommand("QUEST").setExecutor(cmd);
			getCommand("QUESTS").setExecutor(cmd);
			getCommand("qQUESTS").setExecutor(cmd);
	}
	private void qPluginSigns()
	{
		// Sign Listener
		getServer().getPluginManager().registerEvents(new Signs(), this);
	}
	
	// To connect to qQuests put this function in your plugin
	// And "depend: [qQuests]" in your plugin.yml
	/*
	public qQuests qQuests = null;
	public QuestAPI qAPI = null;
	
	public void setupQQuests()
	  {
	    this.qQuests = (qQuests) getServer().getPluginManager().getPlugin("qQuests");

	    if (this.qAPI == null)
	      if (this.qQuests != null) {
	        this.qAPI = qQuests.qAPI;
	        System.out.println("qQuests found!");
	      } else {
	        System.out.println("qQuests not found. Disabling plugin.");
	        getServer().getPluginManager().disablePlugin(this);
	      }
	  }
	  */
}
