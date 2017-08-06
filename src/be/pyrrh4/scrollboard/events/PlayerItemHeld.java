package be.pyrrh4.scrollboard.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import be.pyrrh4.scrollboard.ScrollBoard;
import be.pyrrh4.scrollboard.utils.ScrollType;
import be.pyrrh4.scrollboard.utils.ScrollboardData;

public class PlayerItemHeld implements Listener
{
	@EventHandler
	public void onExecute(final PlayerItemHeldEvent event)
	{
		final Player player = event.getPlayer();
		final ScrollboardData data = ScrollBoard.instance().getScrollboardManager().playersData.get(player);

		if (data == null) return;
		else if (data.type == null) return;
		else if (!data.type.equals(ScrollType.SCROLL)) return;

		int oldSlot = event.getPreviousSlot();
		int newSlot = event.getNewSlot();

		// up
		if (newSlot == oldSlot - 1 || oldSlot == 0 && newSlot == 8) {
			ScrollBoard.instance().getScrollboardManager().goUp(player, data, ScrollType.SCROLL);
		}
		// down
		else if (newSlot == oldSlot + 1 || oldSlot == 8 && newSlot == 0) {
			ScrollBoard.instance().getScrollboardManager().goDown(player, data, ScrollType.SCROLL);
		}
	}
}
