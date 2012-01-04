package me.quaz3l.qQuests;

import me.quaz3l.qQuests.Util.Econ;
import me.quaz3l.qQuests.Util.cmd_qQuests;
import me.quaz3l.qQuests.listeners.bListener;
import me.quaz3l.qQuests.listeners.eListener;
import net.milkbowl.vault.economy.Economy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class qQuests extends JavaPlugin 
{
	public static qQuests plugin;
	private cmd_qQuests cmdExe;
	
	// Hashmaps to store temporary data on player quests
	public Map<Player, Object> currentQuests = new HashMap<Player, Object>();
	public Map<Player, Integer> doneItems = new HashMap<Player, Integer>();
	
	// Player Quest Data
	public Map<Player, List<Integer>> destroyed = new HashMap<Player, List<Integer>>();
	public Map<Player, List<Integer>> damaged = new HashMap<Player, List<Integer>>();
	public Map<Player, List<Integer>> placed = new HashMap<Player, List<Integer>>();
	public Map<Player, List<Integer>> killed = new HashMap<Player, List<Integer>>();
	
	// Quest Hashes
	public Map<Player, List<Integer>> hasCollect = new HashMap<Player, List<Integer>>();
	public Map<Player, List<Integer>> hasDestroy = new HashMap<Player, List<Integer>>();
	public Map<Player, List<Integer>> hasDamage = new HashMap<Player, List<Integer>>();
	public Map<Player, List<Integer>> hasPlace = new HashMap<Player, List<Integer>>();
	public Map<Player, List<Integer>> hasKill = new HashMap<Player, List<Integer>>();
	
	// Get The Logger
	public final Logger logger = Logger.getLogger(("Minecraft"));
	
	// Configuration Files Variables
	private FileConfiguration qConfig = null;
	private File qConfigFile = null;
	private FileConfiguration cConfig = null;
	private File cConfigFile = null;
	
	// If the economy is enabled
	public boolean econEnabled = false;
	
	private final bListener blockListener = new bListener(this);
	private final eListener entityListener = new eListener(this);
	
	//private String newVersion;
    //private String currentVersion;

	@Override
	public void onDisable() 
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info( "[" + getDescription().getName() + "] Version " + pdfFile.getVersion() + " by Quaz3l: Disabled");
	}

	@Override
	public void onEnable() 
	{
		PluginDescriptionFile pdfFile = this.getDescription();
		
		// Check For Updates
		// ***TODO*** Will enable on first release
		/*
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

            @Override
            public void run() {
                try {
                    newVersion = updateCheck(currentVersion);
                    String oldVersion = getDescription().getVersion().substring(0, 5);
                    if (!newVersion.contains(oldVersion)) {
                        plugin.logger.warning(newVersion + " is out! You are running " + oldVersion);
                        plugin.logger.warning("Update Vault at: http://dev.bukkit.org/server-mods/quests");
                    }
                } catch (Exception e) {
                    // ignore exceptions
                }
            }
            
        }, 0, 432000);
		try {
            newVersion = updateCheck(currentVersion);
            String oldVersion = getDescription().getVersion().substring(0, 5);
            if (!newVersion.contains(oldVersion)) {
                plugin.logger.warning(newVersion + " is out! You are running " + oldVersion);
                plugin.logger.warning("Update Vault at: http://dev.bukkit.org/server-mods/quests");
            }
        } catch (Exception e) {
            // ignore exceptions
        }
		*/
		
		// Find Economy
		RegisteredServiceProvider<Economy> economyProvider =
	    getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
	    if (economyProvider != null) {
	    	Econ.economy = economyProvider.getProvider();
	    	this.logger.info( "[" + pdfFile.getName() + "] Version " + pdfFile.getVersion() + " by Quaz3l: Economy Found!");
	    	econEnabled = true;
	    }
	    else
	    {
	    	this.logger.warning( "[" + pdfFile.getName() + "] Version " + pdfFile.getVersion() + " by Quaz3l: Economy Not Found! All Economic Interactions Have Been Disabled!");
	    	econEnabled = false;
	    }
		
		// Get The Configuration Files
	    this.getConfig();
		this.getQuestConfig();
		
		// Saves Configuration Files
		this.saveConfig();
		this.saveQuestConfig();
		
		// Register Events
		PluginManager pm = getServer().getPluginManager();
			// Block Events
			pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
			pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Event.Priority.Normal, this);
		
			// Entity Events
			pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this);
		
		// Setup Command Executors
		cmdExe = new cmd_qQuests(this);
			getCommand("Q").setExecutor(cmdExe);
			getCommand("QUEST").setExecutor(cmdExe);
			getCommand("QUESTS").setExecutor(cmdExe);
			getCommand("qQUESTS").setExecutor(cmdExe);
		
		// Notify The Console
		this.logger.info( "[" + pdfFile.getName() + "] Version " + pdfFile.getVersion() + " by Quaz3l: Enabled");
	}
	 
	// Configuration Functions
	public FileConfiguration getQuestConfig() {
	    if (qConfig == null) {
	        reloadQuestConfig();
	    }
	    return qConfig;
	}
	 
	public void reloadQuestConfig() {
		if (qConfigFile == null) 
		{
		    qConfigFile = new File(getDataFolder(), "quests.yml");
		}
	    qConfig = YamlConfiguration.loadConfiguration(qConfigFile);
	 
	    InputStream defConfigStream = getResource("quests.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        qConfig.setDefaults(defConfig);
	        qConfig.options().copyDefaults(true);
	    }
	}
	 
	public void saveQuestConfig() {
		if (qConfig == null || qConfigFile == null) {
	    return;
	    }
		try {
	        qConfig.save(qConfigFile);
	    } catch (IOException ex) {
	        Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + qConfigFile, ex);
	    }
	}
	
	public FileConfiguration getConfig() {
	    if (cConfig == null) {
	        reloadConfig();
	    }
	    return cConfig;
	}
	 
	public void reloadConfig() {
		if (cConfigFile == null) 
		{
		    cConfigFile = new File(getDataFolder(), "config.yml");
		}
	    cConfig = YamlConfiguration.loadConfiguration(cConfigFile);
	    
	    InputStream defConfigStream = getResource("config.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        cConfig.setDefaults(defConfig);
	        cConfig.options().copyDefaults(true);
	    }
	}
	 
	public void saveConfig() {
		if (cConfig == null || cConfigFile == null) {
	    return;
	    }
		try {
	        cConfig.save(cConfigFile);
	    } catch (IOException ex) {
	        Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + cConfigFile, ex);
	    }
	}
	
	// End Of Quest Function
	public void endQuest(Player player, String type) {
		int healAmount;
		int aHealth;
		int cHealth;
		int feedAmount;
		int aHunger;
		int cHunger;
		
		if(type == "join")
		{
			//***TODO*** Add Items
			//Fee: Items
			
			// Fee: Money
			if(econEnabled) 
			{
				Econ.econChangeBalancePlayer(player, this.getQuestConfig().getDouble(this.currentQuests.get(player) + ".market.fee.toJoin.money"));
			}
			
			// Reward: Health
			cHealth = player.getHealth();
			aHealth = this.getQuestConfig().getInt(this.currentQuests.get(player) + ".market.fee.toJoin.health");
			healAmount = (cHealth + aHealth);
			if(healAmount > 20) {
				player.setHealth(20);
			}
			else if(healAmount < 0)
			{
				player.setHealth(0);
			}
			else
			{
				player.setHealth(healAmount);
			}
			
			// Fee: Hunger
			cHunger = player.getFoodLevel();
			aHunger = this.getQuestConfig().getInt(this.currentQuests.get(player) + ".market.fee.toJoin.hunger");
			feedAmount = (cHunger + aHunger);
			if(feedAmount > 20) {
				player.setFoodLevel(20);
			}
			else if(feedAmount < 0)
			{
				player.setFoodLevel(0);
			}
			else
			{
				player.setFoodLevel(feedAmount);
			}
		}
		else if(type == "drop") {
			//***TODO*** Add Items
			//Fee: Items
			
			// Fee: Money
			if(econEnabled) 
			{
				Econ.econChangeBalancePlayer(player, this.getQuestConfig().getDouble(this.currentQuests.get(player) + ".market.fee.toDrop.money"));
			}

			
			// Fee: Health
			cHealth = player.getHealth();
			aHealth = this.getQuestConfig().getInt(this.currentQuests.get(player) + ".market.fee.toDrop.health");
			healAmount = (cHealth + aHealth);
			if(healAmount > 20) {
				player.setHealth(20);
			}
			else if(healAmount < 0)
			{
				player.setHealth(0);
			}
			else
			{
				player.setHealth(healAmount);
			}
			
			// Fee: Hunger
			cHunger = player.getFoodLevel();
			aHunger = this.getQuestConfig().getInt(this.currentQuests.get(player) + ".market.fee.toDrop.hunger");
			feedAmount = (cHunger + aHunger);
			if(feedAmount > 20) {
				player.setFoodLevel(20);
			}
			else if(feedAmount < 0)
			{
				player.setFoodLevel(0);
			}
			else
			{
				player.setFoodLevel(feedAmount);
			}
						
			
			// Reset Players Quest Data
			this.doneItems.put(player, null);
			this.currentQuests.put(player, null);
			// Notify Player
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Quest Dropped!");
		}
		else if(type == "done")
		{
			//***TODO*** Add Items
			//***FIX*** List<String> Throws a null error
			// Reward: Items
			//List<String> items = plugin.getQuestConfig().getStringList("0.market.reward.items");
			//Iterator<String> iter = items.iterator();
			//while (iter.hasNext()) {
			//	player.sendMessage(iter.next());
			//}
			/*
			List<String> items = plugin.getQuestConfig().getStringList((String) plugin.currentQuests.get("0.market.reward.items"));

			if(items != null && items.size() > 0) // always check against null if it's even the slightest possibility of beeing null
			{
			    for(String itemStr : items) // iterate, don't use indexes and .get() for loops !
			    {
			    	PlayerInventory inventory = player.getInventory();
					String[] cItem = itemStr.split(":");
					Integer howMuch = Integer.parseInt(cItem[1].trim());
					Integer whatToGive = Integer.parseInt(cItem[0].trim());
					ItemStack istack = new ItemStack(whatToGive, howMuch);
					inventory.addItem(istack);
			    }
			}
			*/
			// Reward: Money
			if(econEnabled) 
			{
				Econ.econChangeBalancePlayer(player, this.getQuestConfig().getDouble(this.currentQuests.get(player) + ".market.reward.money"));
			}
			// Reward: Health
			cHealth = player.getHealth();
			aHealth = this.getQuestConfig().getInt(this.currentQuests.get(player) + ".market.reward.health");
			healAmount = (cHealth + aHealth);
			if(healAmount > 20) {
				player.setHealth(20);
			}
			else if(healAmount < 0)
			{
				player.setHealth(0);
			}
			else
			{
				player.setHealth(healAmount);
			}
			
			// Reward: Hunger
			cHunger = player.getFoodLevel();
			aHunger = this.getQuestConfig().getInt(this.currentQuests.get(player) + ".market.reward.hunger");
			feedAmount = (cHunger + aHunger);
			if(feedAmount > 20) {
				player.setFoodLevel(20);
			}
			else if(feedAmount < 0)
			{
				player.setFoodLevel(0);
			}
			else
			{
				player.setFoodLevel(feedAmount);
			}
			
			
			// Reset Players Quest Data
			this.doneItems.put(player, null);
			this.currentQuests.put(player, null);
			
			// Notify Player
			player.sendMessage(ChatColor.GREEN + "Quest Done!");
		}
	}
	
	// This guesses the entity based on trial and error; and returns the right guess
	public String isEntityType(Entity entity)
    {
        if (entity instanceof Player) return "Player";
        if(entity instanceof Sheep) return "Sheep";
        if(entity instanceof Cow) return "Cow";
        if(entity instanceof Pig) return "Pig";
        if(entity instanceof Creeper) return "Creeper";
        if(entity instanceof PigZombie) return "PigZombie";
        if(entity instanceof Skeleton)return "Skeleton";
        if(entity instanceof Spider) return "Spider";
        if(entity instanceof Squid) return "Squid";
        if(entity instanceof Zombie) return "Zombie";
        if(entity instanceof Ghast) return "Ghast";
        if(entity instanceof Slime) return "Slime";
        if(entity instanceof Giant) return "Giant";
        if(entity instanceof Blaze) return "Blaze";
        if(entity instanceof CaveSpider) return "CaveSpider";
        if(entity instanceof Chicken) return "Chicken";
        if(entity instanceof Enderman) return "Enderman";
        if(entity instanceof MagmaCube) return "MagmaCube";
        if(entity instanceof MushroomCow) return "MushroomCow";
        if(entity instanceof Snowman) return "Snowman";
        if(entity instanceof Wolf) return "Wolf";
        return null;
    }

	public void giveQuest(Player player) {
		if(this.currentQuests.get(player) == null) 
		{
			Set<String> questNo = this.getQuestConfig().getKeys(false);
			Random rand = new Random(); 
			Object SelectedQuest = rand.nextInt(questNo.size()); 
			
			this.currentQuests.put(player, SelectedQuest);
			
			this.endQuest(player, "join");
			
			player.sendMessage(ChatColor.AQUA + "Your Quest: " + ChatColor.LIGHT_PURPLE + this.getQuestConfig().getString(SelectedQuest + ".info.messageStart"));
		}
		else
		{
			player.sendMessage(ChatColor.RED + "You already have a active quest!");
			player.sendMessage(ChatColor.AQUA + "Your Quest: " + ChatColor.LIGHT_PURPLE + this.getQuestConfig().getString(this.currentQuests.get(player) + ".info.messageStart"));
		}
		
	}
	
	public void giveQuest(Player player, String s) {
		if(this.currentQuests.get(player) == null) 
		{
			try
		    {
				int SelectedQuest = Integer.parseInt(s.trim()) - 1;
				if(this.getQuestConfig().contains(SelectedQuest + ".info.name")) {
				this.currentQuests.put(player, SelectedQuest);
				
				this.endQuest(player, "join");
				
				player.sendMessage(ChatColor.AQUA + "Your Quest: " + ChatColor.LIGHT_PURPLE + this.getQuestConfig().getString(SelectedQuest + ".info.messageStart"));
				}
				else
				{
					player.sendMessage(ChatColor.RED + "This Is Not A Valid Quest!");
				}
		    }
			catch (NumberFormatException nfe) 
			{
				player.sendMessage(ChatColor.RED + "This Is Not A Valid Quest!");
				return;
			}
		}
		else
		{
			player.sendMessage(ChatColor.RED + "You already have a active quest!");
			player.sendMessage(ChatColor.AQUA + "Your Quest: " + ChatColor.LIGHT_PURPLE + this.getQuestConfig().getString(this.currentQuests.get(player) + ".info.messageStart"));
		}
	}
	
	public String updateCheck(String currentVersion) throws Exception {
        String pluginUrlString = "http://dev.bukkit.org/server-mods/quests/files.rss";
        try {
            URL url = new URL(pluginUrlString);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName("item");
            Node firstNode = nodes.item(0);
            if (firstNode.getNodeType() == 1) {
                Element firstElement = (Element)firstNode;
                NodeList firstElementTagName = firstElement.getElementsByTagName("title");
                Element firstNameElement = (Element) firstElementTagName.item(0);
                NodeList firstNodes = firstNameElement.getChildNodes();
                return firstNodes.item(0).getNodeValue();
            }
        }
        catch (Exception localException) {
        }
        return currentVersion;
    }
}