package be.pyrrh4.scrollboard.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener
{
	@EventHandler
	public void onExecute(PlayerQuitEvent event)
	{
		event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}
}
