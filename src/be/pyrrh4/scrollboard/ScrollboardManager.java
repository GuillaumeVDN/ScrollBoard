package be.pyrrh4.scrollboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import be.pyrrh4.core.Core;
import be.pyrrh4.core.User;
import be.pyrrh4.scrollboard.utils.PyrScoreboard;
import be.pyrrh4.scrollboard.utils.ScrollType;
import be.pyrrh4.scrollboard.utils.ScrollboardData;

public class ScrollboardManager
{
	public Map<Player, ScrollboardData> playersData;
	public Map<String, ScrollboardData> baseScrollboards;

	public ScrollboardManager()
	{
		this.playersData = new HashMap<Player, ScrollboardData>();
		this.baseScrollboards = new HashMap<String, ScrollboardData>();
	}

	public void updateAllPaths() throws	SQLException
	{
		//ScrollBoard.instance().log(Level.INFO, "Starting update...");

		// ----------------------------------------------------------------------------------------------------
		// SQL
		// ----------------------------------------------------------------------------------------------------

		if (Core.instance().getMySQL() != null)
		{
			Map<Player, String> playersPath = new HashMap<Player, String>();
			ResultSet set = Core.instance().getMySQL().get("SELECT * FROM scrollboard_players;");

			while (set.next())
			{
				String uuid = set.getString("uuid");
				Player pl = Bukkit.getPlayer(UUID.fromString(uuid));

				if (pl == null) continue;
				if (!pl.isOnline()) continue;

				playersPath.put(pl, set.getString("path"));
			}

			set.close();

			// Players

			for (Player pl : Bukkit.getOnlinePlayers())
			{
				String path;

				// Contains in playersPath

				if (playersPath.containsKey(pl))
				{
					path = playersPath.get(pl);

					if (path == null || path.isEmpty())
						path = getDefaultPath(pl.getWorld());
				}

				// Not contained in playerPath

				else
					path = getDefaultPath(pl.getWorld());

				// Apply path

				try
				{
					if (playersData.get(pl).path.equals(path))
						continue;
				}
				catch (Exception exception)
				{
					playersData.remove(pl);

					if (path.equals("{default}"))
						path = getDefaultPath(pl.getWorld());
					else if (path.equals("{none}"))
						continue;

					playersData.put(pl, baseScrollboards.get(path).clone());
				}
			}
		}

		// ----------------------------------------------------------------------------------------------------
		// Non-SQL
		// ----------------------------------------------------------------------------------------------------

		else
		{
			//ScrollBoard.instance().log(Level.INFO, "... non-SQL");

			// Players

			for (Player pl : Bukkit.getOnlinePlayers())
			{
				//ScrollBoard.instance().log(Level.INFO, "... player " + pl.getName());
				String path = null;

				if (User.from(pl) != null && User.from(pl).getPluginData("scrollboard") != null) {
					PlayerData data = User.from(pl).getPluginData("scrollboard");
					path = data.getScrollboard();
				}

				if (path == null || path.isEmpty() || !ScrollBoard.instance().getScrollboardManager().baseScrollboards.containsKey(path)) {
					path = getDefaultPath(pl.getWorld());
				}

				//ScrollBoard.instance().log(Level.INFO, "... Path 1 : " + path);

				if (path == null || path.isEmpty())
					path = getDefaultPath(pl.getWorld());

				//ScrollBoard.instance().log(Level.INFO, "... Path 2 : " + path);

				// Apply path

				try
				{
					//ScrollBoard.instance().log(Level.INFO, "... trying to check if it is already the same path");
					if (playersData.get(pl).path.equals(path))
						continue;
					//ScrollBoard.instance().log(Level.INFO, "... no, and no exception were trow");
				}
				catch (Exception exception)
				{
					//ScrollBoard.instance().log(Level.INFO, "... an exception were trow");
					playersData.remove(pl);

					if (path.equals("{default}"))
						path = getDefaultPath(pl.getWorld());
					else if (path.equals("{none}"))
						continue;

					playersData.put(pl, baseScrollboards.get(path).clone());
					//ScrollBoard.instance().log(Level.INFO, "... removed and putted it to " + path);
				}
			}
		}
	}

	private String getDefaultPath(World world)
	{
		// worlds
		String worldName = world.getName();
		if (ScrollBoard.instance().getConfiguration().contains("worlds." + worldName)) {
			return ScrollBoard.instance().getConfiguration().getString("worlds." + worldName);
		}
		// default
		else {
			return ScrollBoard.instance().getDefaultScrollboard();
		}
	}

	public void updatePlaceHolders(Player player)
	{
		ScrollboardData data = playersData.get(player);

		if (data == null) return;
		if (data.path == null) return;
		if (data.path.equalsIgnoreCase("{none}")) return;

		data.replace(player);
	}

	public void update(final Player player)
	{
		ScrollboardData data = playersData.get(player);

		if (data == null) return;
		if (data.path == null) return;
		if (data.path.equalsIgnoreCase("{none}")) return;

		PyrScoreboard scoreboard = data.toScoreboard(player, data.currentIndex);

		scoreboard.build();
		scoreboard.send(player);
	}

	public void goUp(Player player, ScrollboardData data, ScrollType type)
	{
		if (data.currentIndex <= 0)
			return;

		data.currentIndex = getNewIndex(data, type, true);
		update(player);
	}

	public void goDown(Player player, ScrollboardData data, ScrollType type)
	{
		if (data.currentIndex >= data.maxIndex)
			return;

		data.currentIndex = getNewIndex(data, type, false);
		update(player);
	}

	private int getNewIndex(ScrollboardData data, ScrollType type, boolean up)
	{
		// Monter

		if (up)
		{
			int lines = ScrollBoard.instance().getConfiguration().getInt("scrolled-lines." + type.toString().toLowerCase().replace("_", "-") + ".up");

			if (lines == -1)
				return 0;

			if (data.currentIndex - lines < 0)
				return 0;

			return data.currentIndex - lines;
		}

		// Descendre

		else
		{
			int lines = ScrollBoard.instance().getConfiguration().getInt("scrolled-lines." + type.toString().toLowerCase().replace("_", "-") + ".down");

			if (lines == -1)
				return data.maxIndex;

			if (data.currentIndex + lines > data.maxIndex)
				return data.maxIndex;

			return data.currentIndex + lines;
		}
	}

	public void updateAll()
	{
		try {
			updateAllPaths();
		} catch (SQLException exception) { }

		for (Player pl : Bukkit.getOnlinePlayers())
		{
			updatePlaceHolders(pl);
			update(pl);
		}
	}
}
