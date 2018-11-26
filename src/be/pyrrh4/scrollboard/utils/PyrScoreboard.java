package be.pyrrh4.scrollboard.utils;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author RainoBoy97
 * @thread https://bukkit.org/threads/simplescoreboard-make-pretty-scoreboards-with-ease.263041/
 */

public class PyrScoreboard
{
	private Scoreboard scoreboard;
	private String title;
	private Map<String, Integer> scores;
	private List<Team> teams;

	public PyrScoreboard(Player player, String title)
	{
		this.scoreboard = player.getScoreboard();
		this.title = title;
		this.scores = Maps.newLinkedHashMap();
		this.teams = Lists.newArrayList();
	}

	public void add(String text)
	{
		add(text, null);
	}

	public void add(String text, Integer score)
	{
		Preconditions.checkArgument(text.length() < 48, "text cannot be over 48 characters in length");
		text = fixDuplicates(text);
		scores.put(text, score);
	}

	private String fixDuplicates(String text)
	{
		while (scores.containsKey(text))
			text += "§r";

		if (text.length() > 48)
			text = text.substring(0, 47);

		return text;
	}

	private Map.Entry<Team, String> createTeam(String text)
	{
		String result = "";

		if (text.length() <= 16)
			return new AbstractMap.SimpleEntry<>(null, text);

		Team team = scoreboard.registerNewTeam(UUID.randomUUID().toString().substring(0, 15));
		Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();

		team.setPrefix(iterator.next());
		result = iterator.next();

		if (text.length() > 32)
			team.setSuffix(iterator.next());

		teams.add(team);

		return new AbstractMap.SimpleEntry<>(team, result);
	}

	@SuppressWarnings("deprecation")
	public void build()
	{
		if (scoreboard.getObjective(DisplaySlot.SIDEBAR) != null)
			scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();

		String objName = (title.length() > 16 ? title.substring(0, 15) : title);

		if (scoreboard.getObjective(objName) != null)
			scoreboard.getObjective(objName).unregister();

		Objective obj = scoreboard.registerNewObjective(objName, "dummy");
		obj.setDisplayName(title);
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

		int index = scores.size();

		for (String text : scores.keySet())
		{
			Map.Entry<Team, String> team = createTeam(text);
			int score = scores.get(text) != null ? scores.get(text) : index;

			if (team.getKey() != null)
				team.getKey().addEntry(team.getValue());

			obj.getScore(team.getValue()).setScore(score);
			index -= 1;
		}
	}

	public void reset()
	{
		title = null;
		scores.clear();

		for (Team t : teams)
			t.unregister();

		teams.clear();
	}

	public Scoreboard getScoreboard()
	{
		return scoreboard;
	}

	public void send(Player... players)
	{
		for (Player p : players)
			p.setScoreboard(scoreboard);
	}
}