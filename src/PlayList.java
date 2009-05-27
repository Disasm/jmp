import java.util.*;

public class PlayList {
	private Jmp midlet;
	public String name = null;
	private Vector items;
	
	PlayList(Jmp m) {
		midlet = m;
		items = new Vector();
	}

	PlayList(Jmp m, String name) {
		this(m);
		this.name = name;
		items = new Vector();
		load(name);
	}

	public void load(String nm) {
		name = nm;
		items = midlet.listStore.get(name);
		forceUpdate();
	}

	public void save() {
		midlet.listStore.update(name, items);
	}

	public int size() {
		return items.size();
	}

	public PlayListItem elementAt(int index) {
		try {
			return (PlayListItem)items.elementAt(index);
		} catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public int indexOf(PlayListItem item) {
		if(item==null) return -1;
		return items.indexOf(item);
	}

	public boolean hasFile(String path) {
		for(int i=0;i<items.size();i++) {
			PlayListItem item = (PlayListItem)items.elementAt(i);
			if(item.path.equals(path)) return true;
		}
		return false;
	}

	public void delete(PlayListItem item) {
		if(item==null) return;
		Vector v = new Vector();
		for(int i=0;i<items.size();i++) {
			if(items.elementAt(i)!=item) v.addElement(items.elementAt(i));
		}
		items = v;
		forceUpdate();
	}

	public void clear() {
		items = new Vector();
		forceUpdate();
	}

	public void moveAfter(PlayListItem item, PlayListItem after) {
		Vector v = new Vector();
		if(after==null) {
			v.addElement(item);
			for(int i=0;i<items.size();i++) {
				if(items.elementAt(i)!=item) v.addElement(items.elementAt(i));
			}
		} else {
			if(item==after) return;
			for(int i=0;i<items.size();i++) {
				PlayListItem ii = (PlayListItem)items.elementAt(i);
				if(ii==item) continue;
				v.addElement(ii);
				if(ii==after) v.addElement(item);
			}
		}
		items = v;
		forceUpdate();
	}

	public void add(PlayListItem item) {
		items.addElement(item);
		forceUpdate();
	}

	private void forceUpdate() {
		if(midlet.plMenu!=null) midlet.plMenu.rebuild();
		if(midlet.player!=null) midlet.player.rebuild();
	}
}
