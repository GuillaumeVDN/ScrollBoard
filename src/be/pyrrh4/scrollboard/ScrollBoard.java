package be.pyrrh4.scrollboard;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import be.pyrrh4.core.AbstractPlugin;
import be.pyrrh4.core.Core;
import be.pyrrh4.core.Setting;
import be.pyrrh4.core.command.CommandArgumentsPattern;
import be.pyrrh4.core.command.CommandCallInfo;
import be.pyrrh4.core.command.CommandHandler;
import be.pyrrh4.core.command.CommandSubHandler;
import be.pyrrh4.core.storage.PMLConvertor;
import be.pyrrh4.core.storage.PMLReader;
import be.pyrrh4.core.storage.PMLWriter;
import be.pyrrh4.scrollboard.events.PlayerChangedWorld;
import be.pyrrh4.scrollboard.events.PlayerInteract;
import be.pyrrh4.scrollboard.events.PlayerItemHeld;
import be.pyrrh4.scrollboard.events.PlayerJoin;
import be.pyrrh4.scrollboard.events.PlayerMove;
import be.pyrrh4.scrollboard.events.PlayerToggleSneak;
import be.pyrrh4.scrollboard.utils.ScrollType;
import be.pyrrh4.scrollboard.utils.ScrollboardData;

public class ScrollBoard extends AbstractPlugin
{
	public static ScrollBoard i;
	public static PMLReader cfg;
	public ScrollboardManager scrollboardManager;
	public String defaultScrollboard;
	private CommandHandler handler;
	public PMLWriter database = null;

	// Initialize

	@Override
	public void initialize()
	{
		setSetting(Setting.AUTO_UPDATE_URL, "https://www.spigotmc.org/resources/24697/");
		setSetting(Setting.ALLOW_PUBLIC_MYSQL, true);
		setSetting(Setting.HAS_STORAGE, true);
		setSetting(Setting.CONFIG_FILE_NAME, "config.pyrml");
	}

	// On enable

	@Override
	public void enable()
	{
		i = this;
		config.loadTextPaths(this, "msg", null, null);

		// Converting data

		File f = new File(getStorage().getParentDirectory(), "config.yml");

		if (f.exists())
		{
			PMLConvertor convertor = new PMLConvertor(this, f);
			convertor.addPath("default-scrollboard");
			convertor.addPath("update-delay");

			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
			ConfigurationSection sec = cfg.getConfigurationSection("worlds");

			if (sec != null)
			{
				for (String key : sec.getKeys(false)) {
					convertor.addPath("worlds." + key);
				}
			}

			convertor.addPath("scrolled-lines.scroll.up");
			convertor.addPath("scrolles-lines.scroll.down");
			convertor.addPath("scrolled-lines.jump.up");
			convertor.addPath("scrolles-lines.jump.down");
			convertor.addPath("scrolled-lines.click.up");
			convertor.addPath("scrolles-lines.click.down");
			convertor.addPath("mysql.enable");
			convertor.addPath("mysql.url");
			convertor.addPath("mysql.user");
			convertor.addPath("mysql.pass");

			sec = cfg.getConfigurationSection("worlds");

			if (sec != null)
			{
				for (String key : sec.getKeys(false))
				{
					convertor.addPath("scrollboards." + key + ".type");
					convertor.addPath("scrollboards." + key + ".title");
					convertor.addPath("scrollboards." + key + ".separator");
					convertor.addPath("scrollboards." + key + ".content");
				}
			}

			convertor.addPath("msg.error-permission");
			convertor.convert();
		}

		// Var

		cfg = config;
		this.scrollboardManager = new ScrollboardManager();
		this.defaultScrollboard = cfg.getString("default-scrollboard");

		// Database

		if (getMySQL() == null) {
			database = getStorage().getPMLWriter("scrollboards.data");
		}

		// Converting old data

		File oldFile = new File(getDataFolder().getParentFile() + File.separator + "ScrollBoard", "database.yml");

		if (database != null)
		{
			if (oldFile.exists() && !database.reader().getOrDefault("converted", false))
			{
				ScrollBoard.i.log(Level.INFO, "Starting converting old data from /ScrollBoard/database.yml to /pyrrh4_plugins/ScrollBoard/scrollboards.data ...");
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
								ScrollBoard.i.log(Level.WARNING, "Could not load " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)) + "'s scrollboard from the old database file.");
								continue;
							}

							database.set(uuid, scrollboard);
							loaded++;
							ScrollBoard.i.log(Level.INFO, "Successfully loaded " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)) + "'s from the old database file.");
						}
						catch (Exception exception)
						{
							skipped++;
							ScrollBoard.i.log(Level.WARNING, "Could not load " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)) + "'s scrollboard from the old database file.");
						}
					}
				}

				database.set("converted", true).save();
				ScrollBoard.i.log(Level.INFO, "Successfully converted all players scrollboards from the old database file. " + loaded + " player" + (loaded > 1 ? "s" : "") + " were loaded and " + skipped + " player" + (skipped > 1 ? "s" : "") + " were skipped.");
			}
		}

		// MySQL

		else {
			getMySQL().executeQuery("CREATE TABLE IF NOT EXISTS scrollboard_players(uuid VARCHAR(40) NOT NULL, path TINYTEXT, PRIMARY KEY(id))ENGINE=MYISAM DEFAULT CHARSET='utf8';");
		}

		// Commands

		getCommand("scrollboard").setExecutor(this);

		handler = new CommandHandler(this, "/scrollboard", Core.getMessenger());
		handler.addHelp("/pyr reload ScrollBoard", "reload the plugin", "pyr.core.admin");
		handler.addHelp("/scrollboard player [player] [scrollboard path]", "assign a scoreboard to a player", "scrollboard.admin");

		handler.addSubCommand(new CommandSubHandler(false, false, "scrollboard.admin", new CommandArgumentsPattern("player [player] [string]"))
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
					database.set(uuid, finalPath).save();
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

		for (String scrollboardPath : cfg.getKeysForSection("scrollboards", false))
		{
			scrollboardManager.baseScrollboards.put(scrollboardPath,
					new ScrollboardData(
							scrollboardPath,
							ScrollType.fromString(cfg.getOrDefault("scrollboards." + scrollboardPath + ".type", (String) null)),
							cfg.getString("scrollboards." + scrollboardPath + ".title"),
							cfg.getListOfString("scrollboards." + scrollboardPath + ".separator"),
							cfg.getListOfString("scrollboards." + scrollboardPath + ".content")));
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
		if (args.length == 0) {
			handler.showHelp(sender);
		}
		else {
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
	public String getAdditionalPasteContent()
	{
		return "";
	}
}
