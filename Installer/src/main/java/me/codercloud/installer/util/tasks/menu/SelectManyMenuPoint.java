package me.codercloud.installer.util.tasks.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import me.codercloud.installer.util.BaseUtil;
import me.codercloud.installer.util.tasks.MenuTask.Click;
import me.codercloud.installer.util.tasks.MenuTask.ItemRequest;
import me.codercloud.installer.util.tasks.MenuTask.MenuPointLayer;

import org.bukkit.inventory.ItemStack;


public abstract class SelectManyMenuPoint<T> extends PageMenuPoint {

	public static enum SelectMode {
		CLICK, SHIFT_CLICK;
	}
	
	private final ArrayList<T> objects = new ArrayList<T>();
	private final List<T> objectList = Collections.unmodifiableList(objects);
	private final HashMap<Integer, T> selected = new HashMap<Integer, T>();
	private final Set<Integer> selectedSet = Collections.unmodifiableSet(selected.keySet());
	private final Collection<T> selectionObjectSet = Collections.unmodifiableCollection(selected.values());
	private final SelectMode mode;
	
	public SelectManyMenuPoint(int size, String title, SelectMode m, Collection<T> objects) {
		super(size, title);
		this.mode = m == null ? SelectMode.CLICK : m;
		addLayer(new SelectManyMenuPointLayer());
		setObjects(objects);
	}
	
	public SelectManyMenuPoint(int size, String title, Collection<T> objects) {
		this(size, title, null, objects);
	}
	
	public SelectManyMenuPoint(int size, String title, SelectMode m, T ... objects) {
		this(size, title, m, BaseUtil.toList(objects));
	}
	
	public SelectManyMenuPoint(int size, String title, T ... objects) {
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
		return slot >= 0 && slot < objects.size() ? asItem(objects.get(slot), slot, selected.containsKey(slot)) : null;
	}
	
	public final int getMaxPage() {
		int i = (objects.size()-1)/getPageSize()+1;
		if(i<=0)
			i = 1;
		return i;
	}
	
	public final void setObjects(Collection<T> objects) {
		this.objects.clear();
		this.selected.clear();
		this.objects.addAll(objects);
		update();
	}
	
	public final void setSelected(int location, boolean selected) {
		if(location>=objects.size() || location < 0)
			return;
		if(selected)
			this.selected.put(location, objects.get(location));
		else
			this.selected.remove(location);
		update();
	}
	
	public final void setSelectAll(boolean selected) {
		if(selected)
			for(int i = 0; i<objects.size(); i++)
				this.selected.put(i, objects.get(i));
		else
			this.selected.clear();
			
	}
	
	public final List<T> getObjects() {
		return objectList;
	}
	
	public final T getObject(int index) {
		return index < 0 || index >= objects.size() ? null : objects.get(index);
	}
	
	public final Set<Integer> getSelected() {
		return selectedSet;
	}
	
	public final Collection<T> getSelectedObjects() {
		return selectionObjectSet;
	}
	
	public final void sortObjects(Comparator<? super T> comparator) {
		try {
			Collections.sort(objects, comparator);
		} finally {
			update();
		}
	}
	
	public final SelectMode getSelectMode() {
		return mode;
	}
	
	private final class SelectManyMenuPointLayer extends MenuPointLayer implements PageMenuPointLayer {
		
		public boolean onLeftClick(Click c, int slot, boolean shift) {
			if(slot >= 0 && slot < objects.size()) {
				if(mode == SelectMode.SHIFT_CLICK && !shift)
					selected.clear();
				if(selected.containsKey(slot)) {
					selected.remove(slot);
				} else {
					selected.put(slot, objects.get(slot));
				}
				return true;
			}
			return false;
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
