package me.codercloud.installer.util.tasks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.Task;
import me.codercloud.installer.util.tasks.menu.SynchronizedMenuPoint;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class MenuTask extends Task {
	
	private static final Object lock = new Object();
	private static final HashMap<String, Thread> locks = new HashMap<String, Thread>();
	
	private MenuPoint p = null;
	private final Player player;
	private Inventory inventory = null;
	private final ArrayDeque<Inventory> newInventory = new ArrayDeque<Inventory>(3);
	private boolean interrupted = false;
	private boolean updating = false;
	private boolean updateRequest = false;
	
	public MenuTask(MenuPoint p, Player player) {
		p.assign(this);
		this.p = p;
		this.player = player;
	}
	
	@Override
	public void run() {
		try {
			while (p != null && !interrupted) {
				updateInventory();
				update();
				
				boolean waitForNext = false;
				if(p instanceof SynchronizedMenuPoint) {
					SynchronizedMenuPoint s = (SynchronizedMenuPoint) p;
					s.preRun();
					String lockKey = p.syncLock;
					
					boolean access = false;
					try {
						while(p.isOpen() && !p.syncCancel && !access) {
							synchronized (lock) {
								if(locks.get(lockKey) == Thread.currentThread())
									access = true;
								if(locks.get(lockKey) == null) {
									locks.put(lockKey, Thread.currentThread());
									access = true;
								}
							}
						}
						
						if(!p.isOpen() || p.syncCancel)
							waitForNext = s.postCancel();
						else
							waitForNext = s.run();
					} finally {
						if(access)
							synchronized (lock) {
								if(locks.get(lockKey) == Thread.currentThread())
									locks.remove(lockKey);
								access = false;
							}
					}
					
				} else {
					waitForNext = p.run();
				}
				
				if (waitForNext) {
					while (!p.set && !interrupted) {
						try {
							Thread.sleep(3);
						} catch (InterruptedException e) {
						}
					}
				}
				
				p = p.next;
			}
		} finally {
			interrupted = true;
			if(player.getOpenInventory().getTopInventory().equals(inventory))
				player.closeInventory();
			p = null;
		}
	}
	
	private synchronized void changeInventory(final Inventory to) {
		if(interrupted)
			return;
		synchronized (this) {
			newInventory.addLast(to);
			BaseUtil.openInventorySilent(getPlugin(), player, to, new Runnable() {
				public void run() {
					if(newInventory.contains(to))
						inventoryClosed();
				}
			});
		}
		update(to);
	}
	
	private void inventoryClosed() {
		interrupt();
	}
	
	public void update() {
		update(0);
	}
	
	private void update(int run) {
		boolean perform = false;
		synchronized (this) {
			if(updating) {
				if(run==0)
					updateRequest = true;
			} else {
				updating = true;
				perform = true;
				updateRequest = false;
			}
		}
		try {
			if(perform && inventory != null)
				update(inventory);
		} finally {
			synchronized (this) {
				if(perform && updateRequest) {
					updating = false;
					if(run>25) {
						new RuntimeException("Updateloop reached length > 25").printStackTrace();
						return;
					}
				} else if(perform) {
					updating = false;
					perform = false;

				}
			}
			if(perform)
				update(run+1);
		}
	}
	
	private void update(Inventory inventory) {
		int s = inventory.getSize();
		for(int i = 0; i<s; i++) {
			ItemStack item = p != null ? p.update(i) : null;
			ItemStack old = inventory.getItem(i);
			if((old != null && old.equals(item)) || old == item)
				continue;
			inventory.setItem(i, item);
		}
	}
	
	private void updateInventory() {
		changeInventory(BaseUtil.createInventory(p.inventory, p.title));
		update();
	}
	
	@EventHandler
	public void inventoryCloseEvent(InventoryCloseEvent e) {
		if(newInventory.size() != 0)
			return;
		if(e.getInventory().equals(inventory)) {
			inventoryClosed();
		}
	}
	
	@EventHandler
	public void inventoryOpenEvent(InventoryOpenEvent e) {
		Inventory i = e.getInventory();
		if(e.getPlayer().equals(player)) {
			boolean newI = false;
			synchronized (this) {
				if(i.equals(newInventory.peekFirst())) {
					newInventory.pollFirst();
					if(newInventory.size() == 0) {
						newInventory.clear();
					}
					newI = true;
				}
			}
			
			if(newI) {
				if(interrupted) {
					e.setCancelled(true);
					return;
				}
				inventory = i;
				update();
			}
		}
	}
	
	@EventHandler
	public void inventoryClick(InventoryClickEvent e) {
		Inventory top = e.getView().getTopInventory();
		if(top.equals(inventory)) {
			e.setCancelled(true);
			if(e.getWhoClicked() == player && !interrupted) {
				if(e.getRawSlot()<top.getSize()) {
					if(p.click(e.getRawSlot(), e.getClick(), e.getHotbarButton()))
						update();
				} else {
					/*Click in bottom inventory*/
				}
			}
		}
	}
	
	@Override
	public void interrupt() {
		if(interrupted)
			return;
		interrupted = true;
		if(p != null)
			p.onClose();
		player.closeInventory();
	}
	
	public Inventory getInventory() {
		return inventory;
	}
	
	public static class ItemRequest {
		
		private int slot;
		private boolean cancel = false;
		
		protected ItemRequest() {}
		
		public void changeSlot(int slot) {
			this.slot = slot;
		}
		
		public void cancel() {
			cancel = true;
		}
		
		public int getSlot() {
			return slot;
		}
		
		public boolean isCanceled() {
			return cancel;
		}
		
		protected ItemStack invoke(MenuPoint p) {
			Iterator<MenuPointLayer> layers = p.layers.iterator();
			while(layers.hasNext() && !cancel) {
				ItemStack s = layers.next().getItem(this, slot);
				if(s != null)
					return s;
			}
			if(cancel)
				return null;
			return p.updateItem(slot);
		}
		
	}
	
	public static class Click {
		
		public static final int LEFT_CLICK = 1;
		public static final int RIGHT_CLICK = 2;
		public static final int DOUBLE_CLICK = 3;
		public static final int LEFT_CLICK_OUT = 4;
		public static final int RIGHT_CLICK_OUT = 5;
		public static final int NUMBER_CLICK = 6;
		public static final int Q_CLICK = 7;
		
		private int click;
		private int slot;
		private int extra;
		private boolean cancel = false;
		
		protected Click() {}

		public void cancel() {
			cancel = true;
		}
		
		public void changeToLeftClick(int slot, boolean shift) {
			this.click = LEFT_CLICK;
			this.slot = slot;
			this.extra = shift?1:0;
		}
		
		public void changeToRightClick(int slot, boolean shift) {
			this.click = RIGHT_CLICK;
			this.slot = slot;
			this.extra = shift?1:0;
		}
		
		public void changeToDoubleClick(int slot) {
			this.click = DOUBLE_CLICK;
			this.slot = slot;
		}
		
		public void changeToLeftClickOut() {
			this.click = LEFT_CLICK_OUT;
		}
		
		public void changeToRightClickOut() {
			this.click = RIGHT_CLICK_OUT;
		}
		
		public void changeToNumberClick(int slot, int number) {
			this.click = NUMBER_CLICK;
			this.slot = slot;
			this.extra = number;
		}
		
		public void changeToQClick(int slot, boolean ctrl) {
			this.click = Q_CLICK;
			this.slot = slot;
			this.extra = ctrl?1:0;
		}
		
		public int getClick() {
			return click;
		}
		
		public int getSlot() {
			return slot;
		}
		
		public int getExtra() {
			return extra;
		}
		
		public boolean isCanceled() {
			return cancel;
		}
		
		protected boolean invoke(MenuPoint p) {
			Iterator<MenuPointLayer> layers = p.getLayers().iterator();
			boolean b = false;
			while(layers.hasNext() && !cancel) {
				MenuPointLayer l = layers.next();
				switch (click) {
				case LEFT_CLICK:
					b |= l.onLeftClick(this, slot, extra==1);
					break;
				case RIGHT_CLICK:
					b |= l.onRightClick(this, slot, extra==1);
					break;
				case DOUBLE_CLICK:
					b |= l.onDoubleClick(this, slot);
					break;
				case LEFT_CLICK_OUT:
					b |= l.onLeftClickOut(this);
					break;
				case RIGHT_CLICK_OUT:
					b |= l.onRightClickOut(this);
					break;
				case NUMBER_CLICK:
					b |= l.onNumberClick(this, slot, extra);
					break;
				case Q_CLICK:
					b |= l.onQClick(this, slot, extra==1);
					break;
				default:
					break;
				}
			}
			
			if(!cancel) {
				switch (click) {
				case 1:
					b |= p.onLeftClick(slot, extra==1);
					break;
				case 2:
					b |= p.onRightClick(slot, extra==1);
					break;
				case 3:
					b |= p.onDoubleClick(slot);
					break;
				case 4:
					b |= p.onLeftClickOut();
					break;
				case 5:
					b |= p.onLeftClickOut();
					break;
				case 6:
					b |= p.onNumberClick(slot, extra);
					break;
				case 7:
					b |= p.onQClick(slot, extra==1);
					break;
				default:
					break;
				}
			}
			
			return b;
		}
		
	}
	
	public static abstract class MenuPointLayer {
		
		public ItemStack getItem(ItemRequest r, int slot) {
			return null;
		}
		
		public boolean onDoubleClick(Click c, int slot) {
			return false;
		}
		
		public boolean onNumberClick(Click c, int slot, int number) {
			return false;
		}
		
		public boolean onLeftClickOut(Click c) {
			return false;
		}
		
		public boolean onRightClickOut(Click c) {
			return false;
		}
		
		public boolean onRightClick(Click c, int slot, boolean shift) {
			return false;
		}
		
		public boolean onLeftClick(Click c, int slot, boolean shift) {
			return false;
		}
		
		public boolean onQClick(Click c, int slot, boolean ctrl) {
			return false;
		}
		
	}
	
	public static abstract class MenuPoint {
		
		private MenuTask task = null;
		private final ArrayList<MenuPointLayer> layers = new ArrayList<MenuPointLayer>();
		private final List<MenuPointLayer> layerlist = Collections.unmodifiableList(layers);
		private final int inventory;
		private String title;
		private MenuPoint next = null;
		private boolean set = false;
		private String syncLock = null;
		private boolean syncCancel = false;
		
		public MenuPoint(int inventorySize, String title) {
			this.inventory = inventorySize;
			this.title = title;
		}
		
		public abstract ItemStack updateItem(int slot);
		
		public boolean onDoubleClick(int slot) {
			return false;
		}
		
		public boolean onNumberClick(int slot, int number) {
			return false;
		}
		
		public boolean onLeftClickOut() {
			return false;
		}
		
		public boolean onRightClickOut() {
			return false;
		}
		
		public boolean onRightClick(int slot, boolean shift) {
			return false;
		}
		
		public boolean onLeftClick(int slot, boolean shift) {
			return false;
		}
		
		public boolean onQClick(int slot, boolean ctrl) {
			return false;
		}
		
		public boolean run() {
			return true;
		}
		
		public abstract void onClose();
		
		protected final void addLayer(MenuPointLayer l) {
			layers.add(l);
		}
		
		public final List<MenuPointLayer> getLayers() {
			return layerlist;
		}
		
		protected final void update() {
			if(isActive())
				getMenuTask().update();
		}
		
		protected final void close() {
			if(isActive())
				getMenuTask().interrupt();
		}
		
		protected final void setTitle(String title) {
			this.title = title;
			if(isActive())
				task.updateInventory();
		}
		
		public final MenuTask getMenuTask() {
			return task;
		}
		
		public final Player getPlayer() {
			return getMenuTask() == null ? null : getMenuTask().player;
		}
		
		public final boolean hasPermission(String permission) {
			Player p = getPlayer();
			return p == null ? null : getPlayer().hasPermission(permission);
		}
		
		public final boolean isActive() {
			return getMenuTask() == null ? false : getMenuTask().p == this;
		}
		
		public final boolean isOpen() {
			return isActive() && !getMenuTask().interrupted;
		}
		
		private final ItemStack update(int slot) {
			ItemRequest r = new ItemRequest();
			r.changeSlot(slot);
			return r.invoke(this);
		}
		
		private final boolean click(int slot, ClickType click, int hotbar) {
			Click c = new Click();
			if(slot<0) {
				switch (click) {
				case LEFT:
					c.changeToLeftClickOut();
					break;
				case RIGHT:
					c.changeToRightClickOut();
					break;
				default:
					c = null;
					break;
				}
			} else {
				switch (click) {
				case RIGHT:
					c.changeToRightClick(slot, false);
					break;
				case SHIFT_RIGHT:
					c.changeToRightClick(slot, true);
					break;
				case LEFT:
					c.changeToLeftClick(slot, false);
					break;
				case SHIFT_LEFT:
					c.changeToLeftClick(slot, true);
					break;
				case DOUBLE_CLICK:
					c.changeToDoubleClick(slot);
					break;
				case DROP:
					c.changeToQClick(slot, false);
					break;
				case CONTROL_DROP:
					c.changeToQClick(slot, true);
					break;
				case NUMBER_KEY:
					c.changeToNumberClick(slot, hotbar);
					break;
				default:
					c = null;
					break;
				}
			}
			
			if(c == null)
				return false;
			else
				return c.invoke(this);
		}
		
		private final void assign(MenuTask t) {
			synchronized (this) {
				if(getMenuTask() != null && getMenuTask() != t)
					throw new IllegalArgumentException("You can only assign one MenuTask per MenuPoint");
				this.task = t;
			}
		}
		
		protected final void setSyncLock(String lock) {
			syncLock = lock;
		}
		
		protected final void cancelSync() {
			syncCancel = true;
		}
		
		protected final void setNext(MenuPoint p) {
			if(getMenuTask() == null)
				throw new IllegalStateException("Your MenuPoint has to be activated");
			p.assign(getMenuTask());
			this.next = p;
			this.set = true;
		}
		
	}
	
}
