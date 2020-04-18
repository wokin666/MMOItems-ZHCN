package net.Indyuce.mmoitems.stat.data;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;

public class CommandData {
	private final String command;
	private final double delay;
	private final boolean console, op;

	public CommandData(String command, double delay, boolean console, boolean op) {
		Validate.notNull(command, "Command cannot be null");

		this.command = command;
		this.delay = delay;
		this.console = console;
		this.op = op;
	}

	public String getCommand() {
		return command;
	}

	public double getDelay() {
		return delay;
	}

	public boolean hasDelay() {
		return delay > 0;
	}

	public boolean isConsoleCommand() {
		return console;
	}

	public boolean hasOpPerms() {
		return op;
	}

	public String getParsed(Player player) {
		return MMOItems.plugin.getPlaceholderParser().parse(player, command);
	}
}