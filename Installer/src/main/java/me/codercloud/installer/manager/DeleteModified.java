package me.codercloud.installer.manager;

import java.io.File;
import java.util.List;

import me.codercloud.installer.InstallerPlugin;
import me.codercloud.installer.util.PluginUtil;
import me.codercloud.installer.util.Task;

import org.bukkit.configuration.Configuration;

public class DeleteModified extends Task {

	@Override
	public void run() {
		File pluginsFolder = InstallerPlugin.getPluginsDir();
		
		if(pluginsFolder != null && pluginsFolder.exists() && pluginsFolder.isDirectory()) {
			File[] pluginFiles = pluginsFolder.listFiles();
			
			for(File f : pluginFiles) {
				Configuration c = PluginUtil.getDescriptionYml(f);
				
				if(c == null || !c.isString("name") || !c.isString("version") || !c.isString("main") || !c.isList("depend"))
					continue;

				try {
					String name = c.getString("name");
					String version = c.getString("version");
					String main = c.getString("main");
					List<String> depend = c.getStringList("depend");
										
					if(depend.contains(name) && "DELETED".equals(version) && "DELETED".equals(main))
						f.delete();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void interrupt() {
		
	}

}
