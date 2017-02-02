package me.codercloud.installer.util.tasks.menu;


public abstract interface SynchronizedMenuPoint {
	
	public void preRun();
	
	public boolean run();
	
	public boolean postCancel();
	
}
