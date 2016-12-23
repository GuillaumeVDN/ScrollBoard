package be.pyrrh4.scrollboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import be.pyrrh4.scrollboard.utils.ScrollType;
import be.pyrrh4.scrollboard.utils.ScrollboardData;
import be.pyrrh4.scrollboard.utils.PyrScoreboard;

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
		//Bukkit.getLogger().info("Starting update...");

		// ----------------------------------------------------------------------------------------------------
		// SQL
		// ----------------------------------------------------------------------------------------------------

		if (ScrollBoard.i.getMySQL() != null)
		{
			Map<Player, String> playersPath = new HashMap<Player, String>();
			ResultSet set = ScrollBoard.i.getMySQL().get("SELECT * FROM scrollboard_players;");

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
			//Bukkit.getLogger().info("... non-SQL");

			// Players

			for (Player pl : Bukkit.getOnlinePlayers())
			{
				//Bukkit.getLogger().info("... player " + pl.getName());
				String path = ScrollBoard.i.database.getOrDefault(pl.getUniqueId().toString(), getDefaultPath(pl.getWorld()));
				//Bukkit.getLogger().info("... Path 1 : " + path);

				if (path == null || path.isEmpty())
					path = getDefaultPath(pl.getWorld());

				//Bukkit.getLogger().info("... Path 2 : " + path);

				// Apply path

				try
				{
					//Bukkit.getLogger().info("... trying to check if it is already the same path");
					if (playersData.get(pl).path.equals(path))
						continue;
					//Bukkit.getLogger().info("... no, and no exception were trow");
				}
				catch (Exception exception)
				{
					//Bukkit.getLogger().info("... an exception were trow");
					playersData.remove(pl);

					if (path.equals("{default}"))
						path = getDefaultPath(pl.getWorld());
					else if (path.equals("{none}"))
						continue;

					playersData.put(pl, baseScrollboards.get(path).clone());
					//Bukkit.getLogger().info("... removed and putted it to " + path);
				}
			}
		}
	}

	private String getDefaultPath(World world)
	{
		// Mondes

		String worldName = world.getName();

		if (ScrollBoard.cfg.contains("worlds." + worldName))
			return ScrollBoard.cfg.getString("worlds." + worldName);

		// Par défaut

		else
			return ScrollBoard.i.defaultScrollboard;
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
			int lines = ScrollBoard.cfg.getInt("scrolled-lines." + type.toString().toLowerCase().replace("_", "-") + ".up");

			if (lines == -1)
				return 0;

			if (data.currentIndex - lines < 0)
				return 0;

			return data.currentIndex - lines;
		}

		// Descendre

		else
		{
			int lines = ScrollBoard.cfg.getInt("scrolled-lines." + type.toString().toLowerCase().replace("_", "-") + ".down");

			if (lines == -1)
				return data.maxIndex;

			if (data.currentIndex + lines > data.maxIndex)
				return data.maxIndex;

			return data.currentIndex + lines;
		}
	}

	/*


	OLD SCROLLBOARD MANAGER


	public Map<Player, ScrollboardData> playersData;
	public Map<String, ScrollboardData> defaultScrollboards;

	public ScrollboardManager_old()
	{
		this.playersData = new HashMap<Player, ScrollboardData>();
		this.defaultScrollboards = new HashMap<String, ScrollboardData>();
	}

	public void updateAllPaths() throws SQLException
	{
		Map<Player, String> playersPath = new HashMap<Player, String>();
		YamlConfiguration currentConfig = Main.cfg;

		Bukkit.getLogger().warning("1");
		// SQL

		if (Main.mySQL != null)
		{
			ResultSet set = Main.mySQL.get("SELECT * FROM scrollboard_players;");

			if (set != null)
			{
				while (set.next())
				{
					String uuid = set.getString("uuid");
					Player pl = Bukkit.getPlayer(UUID.fromString(uuid));

					if (pl == null)
						continue;

					else if (!pl.isOnline())
						continue;

					playersPath.put(pl, set.getString("path"));
				}
			}
		}

		// Tous les joueurs

		for (Player player : Bukkit.getOnlinePlayers())
		{
			Bukkit.getLogger().warning("Player " + player.getName());
			UUID uuid = player.getUniqueId();
			String scrollboardPath = "";

			// MySQL / Database

			if (playersPath.containsKey(player))
				scrollboardPath = playersPath.get(player);

			else if (Main.database.get().contains(uuid))
				scrollboardPath = Main.database.get().getString(uuid);

			Bukkit.getLogger().warning("currentPath 1 : " + scrollboardPath);
			// Autre

			if (scrollboardPath.isEmpty())
			{
				// Mondes

				String worldName = player.getWorld().getName();

				if (currentConfig.contains("worlds." + worldName))
					scrollboardPath = currentConfig.getString("worlds." + worldName);

				// Par défaut

				else
					scrollboardPath = Main.i.defaultScrollboard;

				Bukkit.getLogger().warning("currentPath 2 : " + scrollboardPath);
			}

			// On vérifie si ce n'est pas le scrollboard actuel

			if (playersData.containsKey(player))
			{
				try
				{
					if (playersData.get(player).path.equals(scrollboardPath))
						continue;
				}
				catch (Exception exception) { }

				playersData.remove(player);
			}

			Bukkit.getLogger().warning("currentPath 3 : " + scrollboardPath);
			// On vérifie si ce n'est pas un scoreboard {default} ou {none}

			if (scrollboardPath == null)
			{
				Bukkit.getLogger().warning("if 1");
				Bukkit.getLogger().warning("if 1 : '" + Main.i.defaultScrollboard + "'");
				scrollboardPath = Main.i.defaultScrollboard;
			}

			else if (scrollboardPath.equalsIgnoreCase("{none}"))
			{
				Bukkit.getLogger().warning("if 2");
				continue;
			}

			else if (scrollboardPath.equalsIgnoreCase("{default}"))
			{
				Bukkit.getLogger().warning("if 3");
				scrollboardPath = Main.i.defaultScrollboard;
			}

			Bukkit.getLogger().warning("currentPath 4 : " + scrollboardPath);
			// On crée le scrollboard

			ScrollboardData scrollboardData = defaultScrollboards.get(scrollboardPath);
			ScrollboardData data = new ScrollboardData(scrollboardData);

			playersData.put(player, data);
			Bukkit.getLogger().warning("currentPath 5 : " + scrollboardPath);
		}
	}

	public void updatePath(Player player)
	{
		UUID uuid = player.getUniqueId();
		String scrollboardPath = "";

		// MySQL / Database

		if (Main.mySQL != null)
			scrollboardPath = (String) Main.mySQL.getObject("SELECT path FROM scrollboard_players WHERE uuid='" + uuid + "';", "path");

		else if (Main.database.get().contains(uuid))
			scrollboardPath = Main.database.get().getString(uuid);

		// Autre

		if (scrollboardPath.isEmpty())
		{
			// Mondes

			String worldName = player.getWorld().getName();

			if (currentConfig.contains("worlds." + worldName))
				scrollboardPath = currentConfig.getString("worlds." + worldName);

			// Par défaut

			else
				scrollboardPath = currentConfig.getString("default-scrollboard");
		}

		// On vérifie si ce n'est pas le scrollboard actuel

		if (playersData.containsKey(player))
		{
			if (playersData.get(player).scrollboardData.path.equals(scrollboardPath))
				return;

			else
				playersData.remove(player);
		}

		// On vérifie si ce n'est pas un scoreboard {default} ou {none}

		if (scrollboardPath == null)
			return;

		else if (scrollboardPath.equalsIgnoreCase("{none}"))
			return;

		else if (scrollboardPath.equalsIgnoreCase("{default}"))
			scrollboardPath = currentConfig.getString("default-scrollboard");

		// On crée le scrollboard

		ScrollboardData scrollboardData = defaultScrollboards.get(scrollboardPath);
		ScrollboardData data = new ScrollboardData(scrollboardData);

		playersData.put(player, data);
	}

	public void updatePlaceHolders(Player player)
	{
		ScrollboardData data = playersData.get(player);

		if (data == null) return;
		if (data.scrollboardData == null) return;
		if (data.scrollboardData.path == null) return;
		if (data.scrollboardData.path.equalsIgnoreCase("{none}")) return;

		data.scrollboardData.replace(player);
	}

	public void update(final Player player)
	{
		ScrollboardData data = playersData.get(player);

		if (data == null) return;
		if (data.scrollboardData == null) return;
		if (data.scrollboardData.path == null) return;
		if (data.scrollboardData.path.equalsIgnoreCase("{none}")) return;

		pScoreboard scoreboard = data.scrollboardData.toScoreboard(player, data.currentIndex);

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
		if (data.currentIndex >= data.scrollboardData.maxIndex)
			return;

		data.currentIndex = getNewIndex(data, type, false);
		update(player);
	}

	private int getNewIndex(ScrollboardData data, ScrollType type, boolean up)
	{
		// Monter

		if (up)
		{
			int lines = Main.cfg.getInt("scrolled-lines." + type.toString().toLowerCase().replace("_", "-") + ".up");

			if (lines == -1)
				return 0;

			if (data.currentIndex - lines < 0)
				return 0;

			return data.currentIndex - lines;
		}

		// Descendre

		else
		{
			int lines = Main.cfg.getInt("scrolled-lines." + type.toString().toLowerCase().replace("_", "-") + ".down");

			if (lines == -1)
				return data.scrollboardData.maxIndex;

			if (data.currentIndex + lines > data.scrollboardData.maxIndex)
				return data.scrollboardData.maxIndex;

			return data.currentIndex + lines;
		}
	}*/
}
