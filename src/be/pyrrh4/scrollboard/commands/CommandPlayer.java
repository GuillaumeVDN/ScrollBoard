package be.pyrrh4.scrollboard.commands;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import be.pyrrh4.core.Core;
import be.pyrrh4.core.User;
import be.pyrrh4.core.command.Argument;
import be.pyrrh4.core.command.CallInfo;
import be.pyrrh4.core.messenger.Messenger;
import be.pyrrh4.core.messenger.Messenger.Level;
import be.pyrrh4.core.util.Handler;
import be.pyrrh4.scrollboard.PlayerData;
import be.pyrrh4.scrollboard.ScrollBoard;

public class CommandPlayer extends Argument
{
	// ------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------

	public CommandPlayer(Argument parent, ArrayList<String> aliases, ArrayList<String> params, boolean playerOnly, boolean async, String permission, String description, ArrayList<String> paramsDescription) {
		super(parent, aliases, params, playerOnly, async, permission, description, paramsDescription);
	}

	// ------------------------------------------------------------
	// Override
	// ------------------------------------------------------------

	@Override
	public void perform(CallInfo call)
	{
		Player player = call.getSenderAsPlayer();
		Player target = call.getArgAsPlayer(1);
		String path = call.getArgAsString(2);

		if (!ScrollBoard.instance().getConfiguration().contains("scrollboards." + path) && !path.equalsIgnoreCase("{default}") && !path.equalsIgnoreCase("{none}"))
		{
			Messenger.send(player, Level.SEVERE_ERROR, "ScrollBoard", "Could not find scrollboard " + path);
			return;
		}

		final String uuid = player.getUniqueId().toString();
		final String finalPath = path.equalsIgnoreCase("{default}") ? ScrollBoard.instance().getDefaultScrollboard() : path;

		// plugin data
		((PlayerData) User.from(UUID.fromString(uuid)).getPluginData("scrollboard")).setScrollboard(finalPath);

		// mySQL
		if (Core.instance().getMySQL() != null && ScrollBoard.instance().getConfiguration().getBoolean("mysql_enable")) {
			new Handler() {
				@Override
				public void execute() {
					Core.instance().getMySQL().executeQuery("REPLACE INTO scrollboard_players(uuid,path) VALUES('" + uuid + "', '" + finalPath + "');");
				}
			}.runAsync();
		}

		// update
		if (player != null && player.isOnline())
		{
			if (finalPath.equalsIgnoreCase("{none}")) {
				player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}

			ScrollBoard.instance().getScrollboardManager().updateAll();
		}

		// message
		Messenger.send(player, Level.NORMAL_SUCCESS, "ScrollBoard", "§a" + target.getName() + "'s scrollboard was set to " + (path.equalsIgnoreCase("{default}") ? finalPath : path) + " !");
	}
}
