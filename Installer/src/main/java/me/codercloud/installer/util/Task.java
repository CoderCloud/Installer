package me.codercloud.installer.util;

import me.codercloud.installer.util.BaseUtil.RunTask;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class Task implements Listener {
	
	private Task next = null;
	private RunTask runner = null;
	
	public abstract void run();
	
	protected abstract void interrupt();
	
	void start(RunTask task) {
		synchronized (this) {
			if(runner != null)
				throw new IllegalStateException("Task can only accessed by one runner");
			runner = task;
		}
		try {
			run();
		} finally {
			synchronized (this) {
				runner = null;
			}
		}
	}
	
	protected final void addListener(Listener l) {
		runner.addListener(l);
	}
	
	protected final Plugin getPlugin() {
		return runner.getPlugin();
	}
	
	protected final void setNextTask(Task t) {
		next = t;
	}
	
	public final Task getNext() {
		return next;
	}
	
}
