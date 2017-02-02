package me.codercloud.installer.command;

import me.codercloud.installer.InstallerConst;
import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.manager.PluginManager;
import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.CommandHandler;
import me.codercloud.installer.util.CommandHandler.CommandListener;
import me.codercloud.installer.util.tasks.MenuTask;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PluginManagerCommand extends CommandListener {
		
	public PluginManagerCommand(InstallerPlugin p) {
		super("/inst <pm/pluginmanager>");
	}

	@Override
	public void handleCommand(CommandHandler h, CommandSender sender,
			Command command, String label, String[] args) {
		if(sender instanceof Player) {
			BaseUtil.runTask(new MenuTask(new PluginManager(), (Player) sender));
		} else
			sender.sendMessage(ChatColor.RED + "You have to be a player");
	}
	
	@Override
	public String getDescription() {
		return "Show PluginManager";
	}
	
	@Override
	public boolean hasPermission(Permissible p) {
		return p.hasPermission(InstallerConst.PERM_PLUGINMANAGER);
	}
}
