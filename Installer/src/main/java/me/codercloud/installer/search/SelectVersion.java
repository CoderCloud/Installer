package me.codercloud.installer.search;

import java.util.Comparator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.codercloud.installer.data.PluginInformation.VersionInformation;
import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.tasks.menu.SelectOneMenuPoint;

public class SelectVersion extends SelectOneMenuPoint<VersionInformation> implements Comparator<VersionInformation>  {
	
	public SelectVersion(List<VersionInformation> projects) {
		super(2, ChatColor.BLUE + "Select Version");
		setObjects(projects);
		sortObjects(this);
		select(0);
	}

	public int compare(VersionInformation o1, VersionInformation o2) {
		return o2.getNumber()-o1.getNumber();
	}

	@Override
	public ItemStack asItem(VersionInformation object, int slot, boolean selected) {
		if(object != null)
			return object.asItemStack(selected);
		return null;
	}

	public ItemStack getSubmitButton() {
		VersionInformation selected = getSelectedObject();
		if(selected != null)
			return BaseUtil.setNameAndLore(new ItemStack(Material.EMERALD_BLOCK), ChatColor.GREEN + "Select file:",
				ChatColor.BOLD + "" + ChatColor.BLUE + selected.getName(),
				"Type: " + selected.getReleaseType()
				);
		else
			return BaseUtil.setNameAndLore(new ItemStack(Material.GLASS), ChatColor.BLUE + "Please select a file", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Click to cancel");
	}
	
	@Override
	public boolean onLeftClickSubmit(boolean shift) {
		VersionInformation selected = getSelectedObject();
		if(selected != null)
			setNext(new SearchPluginFile(selected));
		else
			close();
		return false;
	}
	
	@Override
	public void onClose() {
		getPlayer().sendMessage(ChatColor.RED + "Your installation got canceled");
	}

}
