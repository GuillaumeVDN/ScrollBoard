package be.pyrrh4.scrollboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import be.pyrrh4.core.Core;
import be.pyrrh4.core.Perm;
import be.pyrrh4.core.PyrPlugin;
import be.pyrrh4.core.command.CommandRoot;
import be.pyrrh4.core.util.Utils;
import be.pyrrh4.scrollboard.commands.ArgAssign;
import be.pyrrh4.scrollboard.events.PlayerChangedWorld;
import be.pyrrh4.scrollboard.events.PlayerInteract;
import be.pyrrh4.scrollboard.events.PlayerItemHeld;
import be.pyrrh4.scrollboard.events.PlayerJoin;
import be.pyrrh4.scrollboard.events.PlayerMove;
import be.pyrrh4.scrollboard.events.PlayerToggleSneak;
import be.pyrrh4.scrollboard.utils.ScrollType;
import be.pyrrh4.scrollboard.utils.ScrollboardData;

public class ScrollBoard extends PyrPlugin {

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
	// Pre enable
	// ------------------------------------------------------------

	@Override
	protected boolean preEnable() {
		this.spigotResourceId = 24697;
		return true;
	}

	@Override
	protected void loadStorage() {
	}

	@Override
	protected void saveStorage() {
	}

	// ------------------------------------------------------------
	// Override : reload
	// ------------------------------------------------------------

	@Override
	protected void reloadInner() {
		// settings
		this.defaultScrollboard = getConfiguration().getString("default-scrollboard");

		// load default scoreboard
		for (String scrollboardPath : getConfiguration().getKeysForSection("scrollboards", false)) {
			scrollboardManager.baseScrollboards.put(scrollboardPath,
					new ScrollboardData(
							scrollboardPath,
							ScrollType.fromString(getConfiguration().getString("scrollboards." + scrollboardPath + ".type")),
							getConfiguration().getString("scrollboards." + scrollboardPath + ".title"),
							getConfiguration().getList("scrollboards." + scrollboardPath + ".separator"),
							getConfiguration().getList("scrollboards." + scrollboardPath + ".content")));
		}
	}

	// ------------------------------------------------------------
	// On enable
	// ------------------------------------------------------------

	@Override
	protected boolean enable() {
		// settings
		this.scrollboardManager = new ScrollboardManager();

		// mysql
		if (Core.instance().getMySQL() != null && getConfiguration().getBoolean("mysql_enable")) {
			Core.instance().getMySQL().executeQuery("CREATE TABLE IF NOT EXISTS scrollboard_players(id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40) NOT NULL, path TINYTEXT, PRIMARY KEY(id))ENGINE=MYISAM DEFAULT CHARSET='utf8';");
		}

		// call reload

		// task
		checkTaskId = new BukkitRunnable() {
			@Override
			public void run() {
				scrollboardManager.updateAll();
			}
		}.runTaskTimerAsynchronously(ScrollBoard.instance(), 20L * 6L, (long) (20 * getConfiguration().getInt("update-delay"))).getTaskId();
		reloadInner();

		// events
		Bukkit.getPluginManager().registerEvents(new PlayerItemHeld(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerToggleSneak(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerMove(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerInteract(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerJoin(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerChangedWorld(), this);

		// commands
		CommandRoot root = new CommandRoot(this, Utils.asList("scrollboard"), null, Perm.SCROLLBOARD_ADMIN, false);
		registerCommand(root, Perm.SCROLLBOARD_ADMIN);
		root.addChild(new ArgAssign());

		// return
		return true;
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
