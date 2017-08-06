package be.pyrrh4.scrollboard.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import be.pyrrh4.scrollboard.ScrollBoard;
import be.pyrrh4.scrollboard.utils.ScrollType;
import be.pyrrh4.scrollboard.utils.ScrollboardData;

public class PlayerMove implements Listener
{
	@EventHandler
	public void onExecute(PlayerMoveEvent event)
	{
		if (event.getFrom().getY() + 0.419 < event.getTo().getY() || event.getFrom().getY() + 0.2 < event.getTo().getY())
		{
			Player player = event.getPlayer();
			ScrollboardData data = ScrollBoard.instance().getScrollboardManager().playersData.get(player);

			if (data == null) return;
			else if (data.type == null) return;
			else if (!data.type.equals(ScrollType.JUMP)) return;

			// up
			ScrollBoard.instance().getScrollboardManager().goUp(player, data, ScrollType.JUMP);
		}
	}
}
