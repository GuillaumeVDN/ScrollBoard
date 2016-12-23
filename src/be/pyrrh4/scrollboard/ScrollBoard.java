package be.pyrrh4.scrollboard;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import be.pyrrh4.core.Core;
import be.pyrrh4.core.PyrPlugin;
import be.pyrrh4.core.lib.command.CommandArgumentsPattern;
import be.pyrrh4.core.lib.command.CommandCallInfo;
import be.pyrrh4.core.lib.command.CommandHandler;
import be.pyrrh4.core.lib.command.CommandSubHandler;
import be.pyrrh4.core.lib.storage.ConfigFile;
import be.pyrrh4.scrollboard.events.PlayerChangedWorld;
import be.pyrrh4.scrollboard.events.PlayerInteract;
import be.pyrrh4.scrollboard.events.PlayerItemHeld;
import be.pyrrh4.scrollboard.events.PlayerJoin;
import be.pyrrh4.scrollboard.events.PlayerMove;
import be.pyrrh4.scrollboard.events.PlayerToggleSneak;
import be.pyrrh4.scrollboard.utils.ScrollType;
import be.pyrrh4.scrollboard.utils.ScrollboardData;

public class ScrollBoard extends PyrPlugin
{
	public static ScrollBoard i;
	public static YamlConfiguration cfg;
	public ScrollboardManager scrollboardManager;
	public String defaultScrollboard;
	private CommandHandler handler;
	public ConfigFile database = null;

	public ScrollBoard()
	{
		super(true, "config.yml", "msg", null, null, "https://www.spigotmc.org/resources/24697/", true);
	}

	@Override
	public void enable()
	{
		i = this;
		cfg = config.getLast();
		this.scrollboardManager = new ScrollboardManager();
		this.defaultScrollboard = cfg.getString("default-scrollboard");

		// Database

		if (getMySQL() == null) {
			database = getStorage().getConfig("scrollboards.data");
		}

		// Converting old data

		File oldFile = new File(getDataFolder().getParentFile() + File.separator + "ScrollBoard", "database.yml");

		if (database != null)
		{
			if (oldFile.exists() && !database.getOrDefault("converted", false))
			{
				Bukkit.getLogger().info("[ScrollBoard] Starting converting old data from /ScrollBoard/database.yml to /pyrrh4_plugins/ScrollBoard/scrollboards.data ...");
				YamlConfiguration old = YamlConfiguration.loadConfiguration(oldFile);
				int loaded = 0;
				int skipped = 0;

				if (old.contains("players"))
				{
					for (String uuid : old.getConfigurationSection("items").getKeys(false))
					{
						try
						{
							String scrollboard = old.getString("items." + uuid);

							if (scrollboard == null)
							{
								Bukkit.getLogger().warning("[ScrollBoard] Could not load " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)) + "'s scrollboard from the old database file.");
								continue;
							}

							database.set(uuid, scrollboard);
							loaded++;
							Bukkit.getLogger().info("[ScrollBoard] Successfully loaded " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)) + "'s from the old database file.");
						}
						catch (Exception exception)
						{
							skipped++;
							Bukkit.getLogger().warning("[ScrollBoard] Could not load " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)) + "'s scrollboard from the old database file.");
						}
					}
				}

				database.set("converted", true);
				Bukkit.getLogger().info("[ScrollBoard] Successfully converted all players scrollboards from the old database file. " + loaded + " player" + (loaded > 1 ? "s" : "") + " were loaded and " + skipped + " player" + (skipped > 1 ? "s" : "") + " were skipped.");
			}
		}

		// MySQL

		else {
			getMySQL().executeQuery("CREATE TABLE IF NOT EXISTS scrollboard_players(uuid VARCHAR(40) NOT NULL, path TINYTEXT, PRIMARY KEY(id))ENGINE=MYISAM DEFAULT CHARSET='utf8';");
		}

		// Commands

		getCommand("scrollboard").setExecutor(this);
		handler = new CommandHandler("/scrollboard", Core.getMessenger());

		handler.addSubCommand(new CommandSubHandler(true, false, "scrollboard.admin", new CommandArgumentsPattern("player [player] [string]"))
		{
			@Override
			public void execute(CommandCallInfo call)
			{
				Player player = call.getSenderAsPlayer();
				Player target = call.getArgAsPlayer(1);
				String path = call.getArgAsString(2);

				if (!cfg.contains("scrollboards." + path) && !path.equalsIgnoreCase("{default}") && !path.equalsIgnoreCase("{none}"))
				{
					Core.getMessenger().error(player, "ScrollBoard >>", "Could not find scrollboard '" + path + "' !");
					return;
				}

				String uuid = player.getUniqueId().toString();
				String finalPath = path.equalsIgnoreCase("{default}") ? ScrollBoard.cfg.getString("default-scrollboard") : path;

				// MySQL

				if (getMySQL() != null)
				{
					long exists = (long) getMySQL().getObject("SELECT COUNT(*) AS exsts FROM scrollboard_players WHERE uuid='" + uuid + "';", "exsts");

					if (exists == 0) {
						getMySQL().executeQuery("INSERT INTO scrollboard_players VALUES('" + uuid + "', '" + finalPath + "');");
					} else {
						getMySQL().executeQuery("UPDATE scrollboard_players SET path='" + finalPath + "' WHERE uuid='" + uuid + "';");
					}
				}

				// Database file

				else {
					database.set(uuid, finalPath);
				}

				// Update

				if (player != null && player.isOnline())
				{
					if (finalPath.equalsIgnoreCase("{none}")) {
						player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
					}

					updateAll();
				}

				// Message

				Core.getMessenger().normal(player, "ScrollBoard >>", "§a" + target.getName() + "'s scrollboard was set to '" + (path.equalsIgnoreCase("{default}") ? finalPath : path) + " !");
			}
		});

		// Events

		Bukkit.getPluginManager().registerEvents(new PlayerItemHeld(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerToggleSneak(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerMove(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerInteract(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerJoin(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerChangedWorld(), this);

		// Delays

		Long updateDelay = Long.parseLong(cfg.getString("update-delay"));

		// Loading default scoreboard

		for (String scrollboardPath : cfg.getConfigurationSection("scrollboards").getKeys(false))
		{
			scrollboardManager.baseScrollboards.put(scrollboardPath,
					new ScrollboardData(
							scrollboardPath,
							ScrollType.fromString(cfg.getString("scrollboards." + scrollboardPath + ".type")),
							cfg.getString("scrollboards." + scrollboardPath + ".title"),
							cfg.getStringList("scrollboards." + scrollboardPath + ".separator"),
							cfg.getStringList("scrollboards." + scrollboardPath + ".content")));
		}

		// Starting task

		checkTaskId = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				updateAll();
			}
		}.runTaskTimerAsynchronously(ScrollBoard.i, 20L, 20L * updateDelay).getTaskId();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (args.length == 0)
		{
			// TODO : /help : /scrollboard

			Core.getMessenger().listMessage(sender, "ScrollBoard >>", "This server is running " + getDescription().getName() + " version " + getDescription().getVersion() + ".");

			if (sender.hasPermission("pyr.core.admin")) {
				Core.getMessenger().listSubMessage(sender, "  >>", "§a/scrollboard player [player] [scrollboard-path] §7: assign a scoreboard to a player");
			}
		}
		else
		{
			handler.execute(sender, args);
		}

		return true;
	}

	public void updateAll()
	{
		try
		{
			scrollboardManager.updateAllPaths();
		}
		catch (SQLException exception) { }

		for (Player pl : Bukkit.getOnlinePlayers())
		{
			scrollboardManager.updatePlaceHolders(pl);
			scrollboardManager.update(pl);
		}
	}

	private int checkTaskId;

	@Override
	public void disable()
	{
		Bukkit.getScheduler().cancelTask(checkTaskId);

		for (Player pl : Bukkit.getOnlinePlayers())
			pl.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	@Override
	public String getAdditionnalPasteContent()
	{
		return "";
	}
}
