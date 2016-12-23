package be.pyrrh4.scrollboard.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import be.pyrrh4.core.lib.messenger.internal.Placeholders;
import be.pyrrh4.core.util.UString;

public class ScrollboardData implements Cloneable
{
	public String path;
	public ScrollType type;

	public String title;
	public int separatorSize;
	public List<String> separator;
	public List<String> originalContent;
	public List<String> replacedContent;

	public int currentIndex;
	public int maxIndex;

	public ScrollboardData(String path, ScrollType type, String title, List<String> separator, List<String> originalContent)
	{
		this.path = path;
		this.type = type;

		this.title = UString.format(title);
		this.separatorSize = 0;
		this.separator = UString.format(separator);
		this.originalContent = UString.format(originalContent);
		this.replacedContent = new ArrayList<String>(originalContent);

		if (separator != null)
			separatorSize = separator.size();

		this.currentIndex = 0;

		if (originalContent.size() < (16 - separatorSize))
			this.maxIndex = 0;
		else
			this.maxIndex = originalContent.size() - (16 - separatorSize) + 1;
	}

	public void replace(Player player)
	{
		replacedContent.clear();
		replacedContent = Placeholders.fillPlaceholders(player, replacedContent);

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
		{
			for (String original : originalContent)
				replacedContent.add(PlaceholderAPI.setPlaceholders(player, original));
		}
	}

	public PyrScoreboard toScoreboard(Player player, int currentIndex)
	{
		PyrScoreboard scoreboard = new PyrScoreboard(player, title);

		// Récupération du contenu

		Map<Integer, String> currentContent = new HashMap<Integer, String>();

		int ind = currentIndex;

		for (int i = 0; i < (15 - separatorSize); i++)
		{
			if (ind >= replacedContent.size())
				break;

			currentContent.put(replacedContent.size() - ind, replacedContent.get(ind));
			ind++;
		}

		// Affichage du texte

		for (Integer index : currentContent.keySet())
		{
			String text = currentContent.get(index);

			if (text.length() <= 48)
				scoreboard.add(text, index);

			else
			{
				if (!text.contains("{") && !text.contains("}"))
				{
					Bukkit.getLogger().warning("------------------------------------------------------------");
					Bukkit.getLogger().warning("");
					Bukkit.getLogger().warning("[ScrollBoard] Trying to put a text that go over 48 characters");
					Bukkit.getLogger().warning("in length. The text were cutted to 47 characters.");
					Bukkit.getLogger().warning("(in '" + path + "', line " + index +")");
					Bukkit.getLogger().warning("New text : " + text.substring(0, 47));
					Bukkit.getLogger().warning("");
					Bukkit.getLogger().warning("------------------------------------------------------------");
				}

				scoreboard.add(text.substring(0, 47), index);
			}
		}

		// Ajout du séparateur

		if (separator != null && !separator.isEmpty())
		{
			Map<Integer, String> currentSeparator = new HashMap<Integer, String>();
			int i = replacedContent.size() + separator.size();

			for (String str : separator)
			{
				currentSeparator.put(i, str);
				i--;
			}

			// Affichage du texte

			for (Integer index : currentSeparator.keySet())
				scoreboard.add(currentSeparator.get(index), index);
		}

		return scoreboard;
	}

	public ScrollboardData clone()
	{
		ScrollboardData cloned;

		try
		{
			cloned = (ScrollboardData) super.clone();
		}
		catch (CloneNotSupportedException exception)
		{
			exception.printStackTrace();
			return null;
		}

		cloned.path = path;
		cloned.type = type;
		cloned.title = title;
		cloned.separatorSize = separatorSize;
		cloned.separator = new ArrayList<String>(separator);
		cloned.originalContent = new ArrayList<String>(originalContent);
		cloned.replacedContent = new ArrayList<String>(replacedContent);
		cloned.currentIndex = currentIndex;
		cloned.maxIndex = maxIndex;

		return cloned;
	}
}
