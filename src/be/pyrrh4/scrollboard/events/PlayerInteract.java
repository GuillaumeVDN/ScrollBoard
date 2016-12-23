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
		ScrollboardData data = ScrollBoard.i.scrollboardManager.playersData.get(player);

		if (data == null) return;
		else if (data.type == null) return;

		// Click normal

		if (data.type.equals(ScrollType.CLICK))
		{
			// Monter

			if (event.getAction().toString().contains("LEFT_CLICK"))
				ScrollBoard.i.scrollboardManager.goUp(player, data, ScrollType.CLICK);

			// Descendre

			else if (event.getAction().toString().contains("RIGHT_CLICK"))
				ScrollBoard.i.scrollboardManager.goDown(player, data, ScrollType.CLICK);
		}

		/*// Click spécifique

		else if (data.scrollboardData.type.equals(ScrollType.CLICK_BLOCK))
		{
			String materialName = Main.cfg.getString("scrollboards." + data.scrollboardData.path + ".type").replace("CLICK{", "").replace("}", "");
			Material material = Material.valueOf(materialName);

			if (material == null)
				Messages.ERROR_TYPE_MATERIAL").send(player, new Var("{material}", materialName));

			else if (event.getClickedBlock().getType().equals(material))
			{
				// Monter

				if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
					Main.i.scrollboardManager.goUp(player, data, ScrollType.CLICK_BLOCK);

				// Descendre

				else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
					Main.i.scrollboardManager.goDown(player, data, ScrollType.CLICK_BLOCK);
			}
		}*/
	}
}
