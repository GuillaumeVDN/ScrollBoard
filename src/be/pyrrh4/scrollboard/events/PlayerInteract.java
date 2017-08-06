package be.pyrrh4.scrollboard.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import be.pyrrh4.scrollboard.ScrollBoard;
import be.pyrrh4.scrollboard.utils.ScrollType;
import be.pyrrh4.scrollboard.utils.ScrollboardData;

public class PlayerInteract implements Listener
{
	@EventHandler
	public void onExecute(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		ScrollboardData data = ScrollBoard.instance().getScrollboardManager().playersData.get(player);

		if (data == null) return;
		else if (data.type == null) return;

		// normal click
		if (data.type.equals(ScrollType.CLICK))
		{
			// up
			if (event.getAction().toString().contains("LEFT_CLICK")) {
				ScrollBoard.instance().getScrollboardManager().goUp(player, data, ScrollType.CLICK);
			}
			// down
			else if (event.getAction().toString().contains("RIGHT_CLICK")) {
				ScrollBoard.instance().getScrollboardManager().goDown(player, data, ScrollType.CLICK);
			}
		}

		/*// specific click
		else if (data.scrollboardData.type.equals(ScrollType.CLICK_BLOCK))
		{
			String materialName = Main.cfg.getString("scrollboards." + data.scrollboardData.path + ".type").replace("CLICK{", "").replace("}", "");
			Material material = Material.valueOf(materialName);

			if (material == null)
				Messages.ERROR_TYPE_MATERIAL").send(player, new Var("$MATERIAL", materialName));

			else if (event.getClickedBlock().getType().equals(material))
			{
				// Monter

				if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
					Main.i.getScrollboardManager().goUp(player, data, ScrollType.CLICK_BLOCK);

				// Descendre

				else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
					Main.i.getScrollboardManager().goDown(player, data, ScrollType.CLICK_BLOCK);
			}
		}*/
	}
}
