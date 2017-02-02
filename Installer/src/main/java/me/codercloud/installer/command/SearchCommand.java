package me.codercloud.installer.command;

import me.codercloud.installer.InstallerConst;
import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.search.SearchProject;
import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.CommandHandler;
import me.codercloud.installer.util.CommandHandler.CommandListener;
import me.codercloud.installer.util.tasks.MenuTask;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class SearchCommand extends CommandListener {
		
	public SearchCommand(InstallerPlugin p) {
		super("/inst search <...>");
	}

	@Override
	public void handleCommand(CommandHandler h, CommandSender sender,
			Command command, String label, String[] args) {
		if(sender instanceof Player) {
			String q = getVar("<...>", args);
			BaseUtil.runTask(new MenuTask(new SearchProject(q == null ? "" : q), (Player) sender));
		} else
			sender.sendMessage(ChatColor.RED + "You have to be a player");
	}
	
	@Override
	public String getDescription() {
		return "Search for plugins";
	}
	
	@Override
	public boolean hasPermission(Permissible p) {
		return p.hasPermission(InstallerConst.PERM_INSTALL);
	}
}
