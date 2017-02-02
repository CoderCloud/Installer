package me.codercloud.installer.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.codercloud.installer.util.Data;
import me.codercloud.installer.util.Data.DataList;
import me.codercloud.installer.util.Data.DataMap;
import me.codercloud.installer.util.WebLib;

public class PluginSearchResults {

	public static PluginSearchResults search(String name) throws IOException {
		name = name.trim().toLowerCase();
		ArrayList<String> q = new ArrayList<String>();
		q.add(name.trim().replace(" ", "_"));
		if(name.contains(" "))
			q.add(name.replace(" ", ""));
		
		DataList d = new DataList();
		
		for(String s : q) {
			Data da = Data.readData(new String(WebLib.getWebsite("https://api.curseforge.com/servermods/projects?search="+s)));
			if(da.isList())
				d.addAll(da.asList());
			else
				d.add(da);
		}
		
		return new PluginSearchResults(d);
	}
	
	private List<PluginInformation> results = new ArrayList<PluginInformation>();
	
	public PluginSearchResults(DataList data) {
		for(Object o : data) {
			if(o instanceof DataMap)
				results.add(new PluginInformation((DataMap) o));
		}
		try {
			findMoreInfo();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<PluginInformation> getResults() {
		return results;
	}
	
	private void findMoreInfo() throws IOException {
		DataList fileData = new DataList();
		
		String ids = "";
		for(PluginInformation i : results) {
			if(ids.length()!=0)
				ids += ",";
			ids += i.getId();
			if(ids.length()>100) {
				Data d = Data.readData(new String(WebLib.getWebsite("https://api.curseforge.com/servermods/files?projectIds="+ids)));
				if(d.isList())
					fileData.addAll(d.asList());
				ids = "";
			}
		}
		if(ids.length()!=0) {
			Data d = Data.readData(new String(WebLib.getWebsite("https://api.curseforge.com/servermods/files?projectIds="+ids)));
			if(d.isList())
				fileData.addAll(d.asList());
		}
		
		HashMap<String, PluginInformation> info = new HashMap<String, PluginInformation>(results.size());
		for(PluginInformation i : results)
			info.put(i.getId(), i);
		
		for(Object o : fileData) {
			if(o instanceof DataMap) {
				PluginInformation i = info.get(((DataMap) o).getAsString("projectId"));
				if(i!=null)
					i.readVersionData((DataMap) o);
			}	
		}
		
	}
	
	
	@Override
	public String toString() {
		return results.toString();
	}
	
}
