package be.pyrrh4.scrollboard.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import be.pyrrh4.scrollboard.ScrollBoard;

public class PlayerJoin implements Listener
{
	@EventHandler
	public void onExecute(PlayerJoinEvent event)
	{
		ScrollBoard.i.updateAll();
	}
}
