package me.codercloud.installer;

import java.io.File;
import java.util.List;

import me.codercloud.installer.command.HelpCommand;
import me.codercloud.installer.command.LoadCommand;
import me.codercloud.installer.command.PluginManagerCommand;
import me.codercloud.installer.command.SearchCommand;
import me.codercloud.installer.manager.DeleteModified;
import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.CommandHandler;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class InstallerPlugin extends JavaPlugin {
	
	private static File pluginDir = null;
	
	private CommandHandler h;
	
	public InstallerPlugin() {
		h = new CommandHandler(ChatColor.BLUE + "Use '/inst help/?' to see all commands", 
				new SearchCommand(this),
				new HelpCommand(),
				new LoadCommand(this),
				new PluginManagerCommand(this));
		h.setDefaultHelpFormat(ChatColor.BLUE + "<Installer> Help Page (<page>/<maxpage>)", " -> ", ChatColor.RED + "No commands found!", ChatColor.GREEN, ChatColor.DARK_GREEN);
	}
	
	public static File getPluginsDir() {
		return pluginDir;
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		return h.handleCommand(sender, command, label, args);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return h.handleTabComplete(sender, command, label, args);
	}
	
	@Override
	public void onEnable() {
		pluginDir = getDataFolder().getParentFile();
		BaseUtil.runTask(new DeleteModified());
	}
	
	@Override
	public void onDisable() {
		BaseUtil.handleDisable();
	}
	
}
