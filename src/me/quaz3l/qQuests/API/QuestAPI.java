package me.quaz3l.qQuests.API;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.entity.Player;

import me.quaz3l.qQuests.API.Util.Quest;
import me.quaz3l.qQuests.Util.Storage;

public class QuestAPI {
	private QuestWorker QuestWorker;
	
	public QuestAPI() {
		this.QuestWorker = new QuestWorker();
	}
	
	public QuestWorker getQuestWorker() 
	{
		return QuestWorker;
	}
	
	/*
	public Map<String, Quest> getQuests()
	{
		return QuestWorker.getQuests();
	}
	*/
	public Map<Player, Quest> getActiveQuests()
	{
		return QuestWorker.getActiveQuests();
	}
    
    public Quest getActiveQuest(Player player)
    {
    	return QuestWorker.getActiveQuests().get(player);
    }
    
    public boolean hasActiveQuest(Player p)
    {
    	if(QuestWorker.getActiveQuests().get(p) == null) 
    		return false;
    	else 
    		return true;
    }
    
	// Quest Functions
	public Quest giveQuest(Player player)
    {
		Random generator = new Random();
		Object[] values = QuestWorker.getQuests().values().toArray();
		Quest q = (Quest) values[generator.nextInt(values.length)];
		
		// Rewards/Fees
		if(q.onJoin().feeReward(player))
		{
			// Give The Quest
			QuestWorker.getActiveQuests().put(player, q);
			
			// Setup Tasks
			HashMap<Integer, Integer> ctp = new HashMap<Integer, Integer>();
			int i=0;
			while((q.tasks().size() - 1) >= i)
			{
				ctp.put(i, 0);
				i++;
			}
			Storage.currentTaskProgress.put(player, ctp);
			
			return q;
		}
		else return null;
    }
	
	public Quest giveQuest(Player player, String quest)
    {
		Quest q = QuestWorker.getQuests().get(quest);
		if(q != null)
			if(q.onJoin().feeReward(player))
			{
				// Give The Quest
				QuestWorker.getActiveQuests().put(player, q);
				
				// Setup Tasks
				HashMap<Integer, Integer> ctp = new HashMap<Integer, Integer>();
				int i=0;
				while((q.tasks().size() - 1) >= i)
				{
					ctp.put(i, 0);
					i++;
				}
				Storage.currentTaskProgress.put(player, ctp);
				
				return q;
			}
			else return null;
		else 
			throw new IllegalArgumentException("The Quest '" +quest + "' Does Not Exist!");
    }
	
	public boolean dropQuest(Player player){
		Quest q = QuestWorker.getActiveQuests().get(player);
		if(hasActiveQuest(player))
		{
			if(q.onDrop().feeReward(player))
			{
				this.resetPlayer(player);
				return true;
			}
			else return false;
		}
		else return true;
	}
	
	public void cancelQuest(Player player)
	{
		this.resetPlayer(player);
	}
	private void resetPlayer(Player player)
	{
		QuestWorker.getActiveQuests().remove(player);
		Storage.currentTaskProgress.get(player).clear();
	}
	
	// Streamline The Permissions
	public boolean checkPerms(Player p, String perm)
	{
		if(p.hasPermission("qquests." + perm) || p.isOp())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}