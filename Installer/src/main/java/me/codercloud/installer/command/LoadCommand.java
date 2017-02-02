package me.codercloud.installer.command;

import me.codercloud.installer.InstallerConst;
import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.search.LoadProject;
import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.CommandHandler;
import me.codercloud.installer.util.CommandHandler.CommandListener;
import me.codercloud.installer.util.tasks.MenuTask;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class LoadCommand extends CommandListener{
	
	InstallerPlugin p;

	public LoadCommand(InstallerPlugin p) {
		super("/inst load <...>");
		this.p = p;
	}

	@Override
	public void handleCommand(CommandHandler h, CommandSender sender,
			Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You have to be a player");
			return;
		}
		try {
			String q = getVar("<...>", args);
			BaseUtil.runTask(new MenuTask(new LoadProject(q == null ? "" : q), (Player) sender));
		} catch (NullPointerException e) {}
	}
	
	@Override
	public String getDescription() {
		return "Install plugins from File URLs or Project links";
	}
	
	@Override
	public boolean hasPermission(Permissible p) {
		return p.hasPermission(InstallerConst.PERM_INSTALL);
	}
	
}
