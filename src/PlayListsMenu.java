import javax.microedition.lcdui.*;
import java.util.*;

public class PlayListsMenu extends Menu implements CommandListener {
	private Jmp midlet;
	private static final Command cmdSelect = new Command("Выбрать", Command.ITEM, 0);
	private static final Command cmdBack = new Command("Назад", Command.BACK, 0);
	private static final Command cmdDel = new Command("Удалить", Command.ITEM, 1);
	private Vector v;

	PlayListsMenu(Jmp m) {
		super(m.display, "Выберите список:", Choice.IMPLICIT);
		midlet = m;
		rebuild();
		
		setSelectCommand(cmdSelect);
		addCommand(cmdDel);
		addCommand(cmdBack);
		setCommandListener(this);
	}

	public void rebuild() {
		Vector names = midlet.listStore.getNames();
		deleteAll();
		v = new Vector();
		for(int i=0;i<names.size();i++) {
			int ind = append((String)names.elementAt(i), null);
			if(ind>=v.size()) v.setSize(ind+1);
			v.setElementAt(names.elementAt(i), ind);
		}
	}

	public String getSelectedItem() {
		int ind = getSelectedIndex();
		if(ind==-1) return null;
		String s = (String)v.elementAt(ind);
		if(s==null) return null;
		if(s.equals(PlayListStore.defaultListName)) return null;
		return s;
	}

	public void commandAction(Command cmd, Displayable d) {
		if(cmd==cmdDel) {
			String name = getSelectedItem();
			System.out.println("cmdDel: "+name);
			midlet.listStore.delete(name);
			rebuild();
		} else if(cmd==cmdBack) {
			close();
		} else if(cmd==cmdSelect) {
			String name = getSelectedItem();
			midlet.list.load(name);
			midlet.plMenu.rebuild();
			midlet.plMenu.show();
		}
	}
}
