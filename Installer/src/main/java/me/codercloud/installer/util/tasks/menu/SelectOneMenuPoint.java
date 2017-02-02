package me.codercloud.installer.util.tasks.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.tasks.MenuTask.Click;
import me.codercloud.installer.util.tasks.MenuTask.ItemRequest;
import me.codercloud.installer.util.tasks.MenuTask.MenuPointLayer;

import org.bukkit.inventory.ItemStack;


public abstract class SelectOneMenuPoint<T> extends PageMenuPoint {

	private final ArrayList<T> objects = new ArrayList<T>();
	private final List<T> objectList = Collections.unmodifiableList(objects);
	private int selected = -1;
	
	public SelectOneMenuPoint(int size, String title, Collection<T> objects) {
		super(size, title);
		addLayer(new SelectOneMenuPointLayer());
		setObjects(objects);
	}
	
	public SelectOneMenuPoint(int size, String title, T ... objects) {
		this(size, title, BaseUtil.toList(objects));
	}
	
	public abstract ItemStack asItem(T object, int slot, boolean selected);
	
	public ItemStack getSubmitButton() {
		return null;
	}
	
	public boolean onLeftClickSubmit(boolean shift) {
		return false;
	}

	public boolean onQClickSubmit(boolean ctrl) {
		return false;
	}

	public boolean onNumberClickSubmit(int number) {
		return false;
	}

	public boolean onDoubleClickSubmit() {
		return false;
	}

	public boolean onRightClickSubmit(boolean shift) {
		return false;
	}
	
	public final ItemStack updateItem(int slot) {
		return slot >= 0 && slot < objects.size() ? asItem(objects.get(slot), slot, selected == slot) : null;
	}
	
	public final int getMaxPage() {
		int i = (objects.size()-1)/getPageSize()+1;
		if(i<=0)
			i = 1;
		return i;
	}
	
	public final void setObjects(Collection<T> objects) {
		this.objects.clear();
		this.objects.addAll(objects);
		update();
	}
	
	public final void select(int location) {
		this.selected = location;
		update();
	}
	
	public final List<T> getObjects() {
		return objectList;
	}
	
	public final T getObject(int index) {
		return index < 0 || index >= objects.size() ? null : objects.get(index);
	}
	
	public final int getSelected() {
		return selected < 0 || selected >= objects.size() ? -1 : selected;
	}
	
	public final T getSelectedObject() {
		return selected < 0 || selected >= objects.size() ? null : objects.get(selected);
	}
	
	public final void sortObjects(Comparator<? super T> comparator) {
		try {
			Collections.sort(objects, comparator);
		} finally {
			update();
		}
	}
	
	private final class SelectOneMenuPointLayer extends MenuPointLayer implements PageMenuPointLayer {
		
		public boolean onLeftClick(Click c, int slot, boolean shift) {
			if(slot >= 0 && slot < objects.size())
				selected = slot;
			return true;
		}

		public ItemStack getButton(ItemRequest r, int slot) {
			if(slot == 5) {
				r.cancel();
				return getSubmitButton();
			}
			return null;
		}

		public boolean onDoubleClickButton(Click c, int slot) {
			if(slot == 5) {
				c.cancel();
				return onDoubleClickSubmit();
			}
			return false;
		}

		public boolean onNumberClickButton(Click c, int slot, int number) {
			if(slot == 5) {
				c.cancel();
				return onNumberClickSubmit(number);
			}
			return false;
		}

		public boolean onRightClickButton(Click c, int slot, boolean shift) {
			if(slot == 5) {
				c.cancel();
				return onRightClickSubmit(shift);
			}
			return false;
		}

		public boolean onLeftClickButton(Click c, int slot, boolean shift) {
			if(slot == 5) {
				c.cancel();
				return onLeftClickSubmit(shift);
			}
			return false;
		}

		public boolean onQClickButton(Click c, int slot, boolean ctrl) {
			if(slot == 5) {
				c.cancel();
				return onQClickSubmit(ctrl);
			}
			return false;
		}
		
	}
	
}
