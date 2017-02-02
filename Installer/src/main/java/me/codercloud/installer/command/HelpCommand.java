package me.codercloud.installer.command;

import me.codercloud.installer.InstallerConst;
import me.codercloud.installer.util.CommandHandler;
import me.codercloud.installer.util.CommandHandler.CommandListener;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

public class HelpCommand extends CommandListener {
	
	public HelpCommand() {
		super("/inst <?/help> <var=page>");
	}

	@Override
	public void handleCommand(CommandHandler h, CommandSender sender, Command command, String label, String[] args) {
		if (isVarSet("page", args)) {
			Integer page = getVarAsInt("page", args);
			if (page != null)
				sender.sendMessage(h.getHelp(sender, Integer.valueOf(page), 5));
			else
				sender.sendMessage(ChatColor.RED + "'" + getVar("page", args) + "' is not a number");
		} else
			sender.sendMessage(h.getHelp(sender, 1, 5));
	}
	
	@Override
	public String getDescription() {
		return "Display helpscreen";
	}
	
	@Override
	public boolean hasPermission(Permissible p) {
		return p.hasPermission(InstallerConst.PERM_HELP);
	}
	
}
