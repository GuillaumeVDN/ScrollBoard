package be.pyrrh4.scrollboard.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import be.pyrrh4.scrollboard.ScrollBoard;
import be.pyrrh4.scrollboard.utils.ScrollType;
import be.pyrrh4.scrollboard.utils.ScrollboardData;

public class PlayerToggleSneak implements Listener
{
	@EventHandler
	public void onExecute(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();
		ScrollboardData data = ScrollBoard.instance().getScrollboardManager().playersData.get(player);

		if (data == null) return;
		else if (data.type == null) return;
		else if (!data.type.equals(ScrollType.JUMP)) return;

		// down
		ScrollBoard.instance().getScrollboardManager().goDown(player, data, ScrollType.JUMP);
	}
}
