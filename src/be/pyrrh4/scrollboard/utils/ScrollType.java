package be.pyrrh4.scrollboard.utils;

public enum ScrollType
{
	DEFAULT,
	SCROLL,
	JUMP,
	CLICK;

	public static ScrollType fromString(String name)
	{
		if (name == null)
			return DEFAULT;

		for (ScrollType type : values())
		{
			if (type.toString().equalsIgnoreCase(name))
				return type;
		}

		throw new IllegalArgumentException("unknow scroll type '" + name + "'");
	}
}
