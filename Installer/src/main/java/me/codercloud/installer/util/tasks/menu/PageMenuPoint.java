package me.codercloud.installer.util.tasks.menu;

import java.util.Iterator;

import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.tasks.MenuTask.Click;
import me.codercloud.installer.util.tasks.MenuTask.ItemRequest;
import me.codercloud.installer.util.tasks.MenuTask.MenuPoint;
import me.codercloud.installer.util.tasks.MenuTask.MenuPointLayer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public abstract class PageMenuPoint extends MenuPoint {
	
	private int page = 1;
	private final int rows;
	
	public PageMenuPoint(int size, String title) {
		super(size+1, title);
		rows = size;
		addLayer(new PageMenuInventoryHandler());
	}
	
	public int getMaxPage() {
		return -1;
	}
	
	public ItemStack getButton(int slot) {
		return null;
	}
	
	public boolean onLeftClickButton(int slot, boolean shift) {
		return false;
	}

	public boolean onQClickButton(int slot, boolean ctrl) {
		return false;
	}

	public boolean onNumberClickButton(int slot, int number) {
		return false;
	}

	public boolean onDoubleClickButton(int slot) {
		return false;
	}

	public boolean onRightClickButton(int slot, boolean shift) {
		return false;
	}
	
	public final int getPageSize() {
		return rows*9;
	}
	
	public final int getPage() {
		int maxPage = getMaxPage();
		if(maxPage >= 0 && page>maxPage)
			page=maxPage;
		if(page<1)
			page = 1;
		return page;
	}
	
	private final ItemStack getPageIndicator() {
		int maxPage = getMaxPage();
		if(maxPage == 0)
			maxPage = 1;
		if(maxPage>0)
			return BaseUtil.setName(new ItemStack(Material.NAME_TAG), ChatColor.BLUE + "Page (" + getPage() + "/" + getMaxPage() + ")");
		else
			return BaseUtil.setName(new ItemStack(Material.NAME_TAG), ChatColor.BLUE + "Page (" + getPage() + ")");
	}
	
	private final ItemStack getNextPageIndicator() {
		return BaseUtil.setName(new ItemStack(Material.PAPER), ChatColor.BLUE + "Next page (" + (getPage()+1) + ")");
	}
	
	private final ItemStack getPrevPageIndicator() {
		return BaseUtil.setName(new ItemStack(Material.PAPER), ChatColor.BLUE + "Previous page (" + (getPage()+1) + ")");
	}
	
	public static interface PageMenuPointLayer {
		
		public ItemStack getButton(ItemRequest r, int slot);
		
		public boolean onDoubleClickButton(Click c, int slot);
		
		public boolean onNumberClickButton(Click c, int slot, int number);
		
		public boolean onRightClickButton(Click c, int slot, boolean shift);
		
		public boolean onLeftClickButton(Click c, int slot, boolean shift);
		
		public boolean onQClickButton(Click c, int slot, boolean ctrl);
		
	}
	
	private class PageItemRequest extends ItemRequest {
		
		@Override
		protected ItemStack invoke(MenuPoint p) {
			Iterator<MenuPointLayer> layers = p.getLayers().iterator();
			while(layers.hasNext() && !isCanceled()) {
				MenuPointLayer l1 = layers.next();
				ItemStack s = null;
				if(l1 instanceof PageMenuPointLayer)
					s = ((PageMenuPointLayer) l1).getButton(this, getSlot());
				if(s != null)
					return s;
			}
			if(isCanceled() || !(p instanceof PageMenuPoint))
				return null;
			return ((PageMenuPoint) p).getButton(getSlot());
		}
		
	}
	
	private class PageMenuClick extends Click {
		
		@Override
		protected boolean invoke(MenuPoint p1) {
			Iterator<MenuPointLayer> layers = p1.getLayers().iterator();
			boolean b = false;
			while(layers.hasNext() && !isCanceled()) {
				MenuPointLayer l1 = layers.next();
				if(!(l1 instanceof PageMenuPointLayer))
					continue;
				PageMenuPointLayer l = (PageMenuPointLayer) l1;
				switch (getClick()) {
				case LEFT_CLICK:
					b |= l.onLeftClickButton(this, getSlot(), getExtra()==1);
					break;
				case RIGHT_CLICK:
					b |= l.onRightClickButton(this, getSlot(), getExtra()==1);
					break;
				case DOUBLE_CLICK:
					b |= l.onDoubleClickButton(this, getSlot());
					break;
				case NUMBER_CLICK:
					b |= l.onNumberClickButton(this, getSlot(), getExtra());
					break;
				case Q_CLICK:
					b |= l.onQClickButton(this, getSlot(), getExtra()==1);
					break;
				default:
					break;
				}
			}
			
			if(!isCanceled() && p1 instanceof PageMenuPoint) {
				PageMenuPoint p = (PageMenuPoint) p1;
				switch (getClick()) {
				case LEFT_CLICK:
					b |= p.onLeftClickButton(getSlot(), getExtra()==1);
					break;
				case RIGHT_CLICK:
					b |= p.onRightClickButton(getSlot(), getExtra()==1);
					break;
				case DOUBLE_CLICK:
					b |= p.onDoubleClickButton(getSlot());
					break;
				case NUMBER_CLICK:
					b |= p.onNumberClickButton(getSlot(), getExtra());
					break;
				case Q_CLICK:
					b |= p.onQClickButton(getSlot(), getExtra()==1);
					break;
				default:
					break;
				}
			}
			
			return b;
		}
		
	}
	
	private class PageMenuInventoryHandler extends MenuPointLayer {

		@Override
		public ItemStack getItem(ItemRequest r, int slot) {
			int barStart = rows*9;
			if(slot>=0 && slot<barStart) {
				int location = (getPage()-1)*barStart + slot;
				r.changeSlot(location);
				return null;
			} else if(slot>0) {
				int location = slot-barStart;
				r.cancel();
				switch (location) {
				case 3:
					return getPage() == 1 ? null : getPrevPageIndicator();
				case 4:
					return getPageIndicator();
				case 5:
					return getPage() == getMaxPage() ? null : getNextPageIndicator();
				}
				if(location>=0 && location<9 && (location<3 || location>5)) {
					if(location>5)
						location-=3;
					PageItemRequest r1 = new PageItemRequest();
					r1.changeSlot(location);
					return r1.invoke(PageMenuPoint.this);
				}
				return null;
			} else {
				r.cancel();
				return null;
			}
		}
		
		@Override
		public boolean onLeftClick(Click c, int slot, boolean shift) {
			int barStart = rows*9;
			if(slot>=0 && slot<barStart) {
				int location = (getPage()-1)*barStart + slot;
				c.changeToLeftClick(location, shift);
			} else if(slot>0) {
				int location = slot-barStart;
				c.cancel();
				switch (location) {
				case 3:
					page--;
					return true;
				case 4:
					return false;
				case 5:
					page++;
					return true;
				default:
					break;
				}
				
				if(location>=0 && location<9 && (location<3 || location>5)) {
					if(location>5)
						location-=3;
					PageMenuClick c1 = new PageMenuClick();
					c1.changeToLeftClick(location, shift);
					c1.invoke(PageMenuPoint.this);
				}
			} else {
				c.cancel();
			}
			return false;
		}
		
		@Override
		public boolean onDoubleClick(Click c, int slot) {
			int barStart = rows*9;
			if(slot>=0 && slot<barStart) {
				int location = (getPage()-1)*barStart + slot;
				c.changeToDoubleClick(location);
			} else if(slot>0) {
				int location = slot-barStart;
				c.cancel();
				if(location>=0 && location<9 && (location<3 || location>5)) {
					if(location>5)
						location-=3;
					PageMenuClick c1 = new PageMenuClick();
					c1.changeToDoubleClick(location);
					c1.invoke(PageMenuPoint.this);
				}
			} else {
				c.cancel();
			}
			return false;
		}
		
		@Override
		public boolean onRightClick(Click c, int slot, boolean shift) {
			int barStart = rows*9;
			if(slot>=0 && slot<barStart) {
				int location = (getPage()-1)*barStart + slot;
				c.changeToRightClick(location, shift);
			} else if(slot>0) {
				int location = slot-barStart;
				c.cancel();
				if(location>=0 && location<9 && (location<3 || location>5)) {
					if(location>5)
						location-=3;
					PageMenuClick c1 = new PageMenuClick();
					c1.changeToRightClick(location, shift);
					c1.invoke(PageMenuPoint.this);
				}
			} else {
				c.cancel();
			}
			return false;
		}
		
		@Override
		public boolean onNumberClick(Click c, int slot, int number) {
			int barStart = rows*9;
			if(slot>=0 && slot<barStart) {
				int location = (getPage()-1)*barStart + slot;
				c.changeToNumberClick(location, number);
			} else if(slot>0) {
				int location = slot-barStart;
				c.cancel();
				if(location>=0 && location<9 && (location<3 || location>5)) {
					if(location>5)
						location-=3;
					PageMenuClick c1 = new PageMenuClick();
					c1.changeToNumberClick(location, number);
					c1.invoke(PageMenuPoint.this);
				}
			} else {
				c.cancel();
			}
			return false;
		}
		
		@Override
		public boolean onQClick(Click c, int slot, boolean ctrl) {
			int barStart = rows*9;
			if(slot>=0 && slot<barStart) {
				int location = (getPage()-1)*barStart + slot;
				c.changeToQClick(location, ctrl);
			} else if(slot>0) {
				int location = slot-barStart;
				c.cancel();
				if(location>=0 && location<9 && (location<3 || location>5)) {
					if(location>5)
						location-=3;
					PageMenuClick c1 = new PageMenuClick();
					c1.changeToQClick(location, ctrl);
					c1.invoke(PageMenuPoint.this);
				}
			} else {
				c.cancel();
			}
			return false;
		}
		
	}

}
