package be.pyrrh4.scrollboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import be.pyrrh4.core.Core;
import be.pyrrh4.core.PyrPlugin;
import be.pyrrh4.core.command.Arguments;
import be.pyrrh4.core.command.Command;
import be.pyrrh4.scrollboard.commands.ArgPlayer;
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
	// ------------------------------------------------------------
	// Instance
	// ------------------------------------------------------------

	private static ScrollBoard instance;

	public ScrollBoard() {
		instance = this;
	}

	public static ScrollBoard instance() {
		return instance;
	}

	// ------------------------------------------------------------
	// Fields
	// ------------------------------------------------------------

	private int checkTaskId;
	private ScrollboardManager scrollboardManager;
	private String defaultScrollboard;

	public ScrollboardManager getScrollboardManager() {
		return scrollboardManager;
	}

	public String getDefaultScrollboard() {
		return defaultScrollboard;
	}

	// ------------------------------------------------------------
	// Override
	// ------------------------------------------------------------

	@Override
	protected void init() {
		//getSettings().autoUpdateUrl("https://www.spigotmc.org/resources/24697/");
	}

	@Override
	protected void initStorage() {}

	@Override
	protected void savePluginData() {}

	// ------------------------------------------------------------
	// On enable
	// ------------------------------------------------------------

	@Override
	protected void enable()
	{
		// settings
		this.scrollboardManager = new ScrollboardManager();
		this.defaultScrollboard = getConfiguration().getString("default-scrollboard");

		// mysql
		if (Core.instance().getMySQL() != null && getConfiguration().getBoolean("mysql_enable")) {
			Core.instance().getMySQL().executeQuery("CREATE TABLE IF NOT EXISTS scrollboard_players(id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40) NOT NULL, path TINYTEXT, PRIMARY KEY(id))ENGINE=MYISAM DEFAULT CHARSET='utf8';");
		}

		// commands
		new Command(this, "scrollboard", "scrollboard", null).addArguments(new Arguments("player [player] [string]", "player [player] [scrollboard]", "assign a scrollboard", "scrollboard.admin", false, new ArgPlayer()));

		// events
		Bukkit.getPluginManager().registerEvents(new PlayerItemHeld(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerToggleSneak(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerMove(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerInteract(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerJoin(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerChangedWorld(), this);

		// load default scoreboard
		for (String scrollboardPath : getConfiguration().getKeysForSection("scrollboards", false))
		{
			scrollboardManager.baseScrollboards.put(scrollboardPath,
					new ScrollboardData(
							scrollboardPath,
							ScrollType.fromString(getConfiguration().getString("scrollboards." + scrollboardPath + ".type")),
							getConfiguration().getString("scrollboards." + scrollboardPath + ".title"),
							getConfiguration().getList("scrollboards." + scrollboardPath + ".separator"),
							getConfiguration().getList("scrollboards." + scrollboardPath + ".content")));
		}

		// task
		checkTaskId = new BukkitRunnable() {
			@Override
			public void run() {
				scrollboardManager.updateAll();
			}
		}.runTaskTimerAsynchronously(ScrollBoard.instance(), 20L * 6L, (long) (20 * getConfiguration().getInt("update-delay"))).getTaskId();
	}

	// ------------------------------------------------------------
	// On disable
	// ------------------------------------------------------------

	@Override
	protected void disable()
	{
		Bukkit.getScheduler().cancelTask(checkTaskId);

		for (Player pl : Bukkit.getOnlinePlayers()) {
			pl.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}
}
