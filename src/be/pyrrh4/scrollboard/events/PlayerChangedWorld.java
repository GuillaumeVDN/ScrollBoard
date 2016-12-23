package be.pyrrh4.scrollboard.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import be.pyrrh4.scrollboard.ScrollBoard;

public class PlayerChangedWorld implements Listener
{
	@EventHandler
	public void onExecute(PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();

		ScrollBoard.i.scrollboardManager.update(player);
	}
}
