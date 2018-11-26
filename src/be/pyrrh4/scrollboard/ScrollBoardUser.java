package be.pyrrh4.scrollboard;

import be.pyrrh4.core.storage.PluginData;

public class ScrollBoardUser extends PluginData {

	// ------------------------------------------------------------
	// Fields and methods
	// ------------------------------------------------------------

	private String scrollboard;

	public String getScrollboard() {
		return scrollboard;
	}

	public void setScrollboard(String scrollboard) {
		this.scrollboard = scrollboard;
		mustSave(true);
	}

}
