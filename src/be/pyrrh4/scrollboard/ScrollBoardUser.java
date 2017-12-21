package be.pyrrh4.scrollboard;

import be.pyrrh4.core.PluginData;

public class ScrollBoardUser extends PluginData
{
	// ------------------------------------------------------------
	// Fields and methods
	// ------------------------------------------------------------

	private String scrollboard;

	public String getScrollboard() {
		return scrollboard;
	}

	public ScrollBoardUser setScrollboard(String scrollboard) {
		this.scrollboard = scrollboard;
		return this;
	}
}
