package be.pyrrh4.scrollboard.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import be.pyrrh4.core.Logger;
import be.pyrrh4.core.Logger.Level;
import be.pyrrh4.core.util.PlaceholderAPIHandler;
import be.pyrrh4.core.util.Utils;
import be.pyrrh4.scrollboard.ScrollBoard;

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

		this.title = Utils.format(title);
		this.separatorSize = 0;
		this.separator = Utils.format(separator);
		this.originalContent = Utils.format(originalContent);
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

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()) {
			PlaceholderAPIHandler.fill(player, replacedContent);
		}

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
		{
			for (String original : originalContent)
				replacedContent.add(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled() ? PlaceholderAPIHandler.fill(player, original) : original);
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
				// not a big placeholder
				if (!text.contains("{") && !text.contains("}"))
				{
					Logger.log(Level.WARNING, ScrollBoard.instance(), "------------------------------------------------------------");
					Logger.log(Level.WARNING, ScrollBoard.instance(), "");
					Logger.log(Level.WARNING, ScrollBoard.instance(), "[ScrollBoard] Trying to put a text that go over 48 characters");
					Logger.log(Level.WARNING, ScrollBoard.instance(), "in length. The text were cutted to 47 characters.");
					Logger.log(Level.WARNING, ScrollBoard.instance(), "(in '" + path + "', line " + index +")");
					Logger.log(Level.WARNING, ScrollBoard.instance(), "New text : " + text.substring(0, 47));
					Logger.log(Level.WARNING, ScrollBoard.instance(), "");
					Logger.log(Level.WARNING, ScrollBoard.instance(), "------------------------------------------------------------");
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
