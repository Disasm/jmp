import javax.microedition.lcdui.*;
import java.util.*;

public class MainMenu extends List {
	private Jmp midlet;
	public static final Command SELECT_COMMAND = new Command("", Command.SCREEN, 0);
	public static final int ITEM_LIST = 1;
	public static final int ITEM_PLAYER = 2;
	public static final int ITEM_LISTS = 3;
	public static final int ITEM_SETTINGS = 4;
	public static final int ITEM_MINIMIZE = 5;
	public static final int ITEM_EXIT = 6;
	Vector v;

	MainMenu(Jmp m) {
		super("Главное меню", Choice.IMPLICIT);
		midlet = m;

		v = new Vector();
		append("Список", ITEM_LIST);
		append("Плеер", ITEM_PLAYER);
		append("Списки", ITEM_LISTS);
		append("Настройки", ITEM_SETTINGS);
		if(midlet.stForm.getMinimize()) append("Свернуть", ITEM_MINIMIZE);
		append("Выход", ITEM_EXIT);
		setSelectCommand(SELECT_COMMAND);
	}

	private void append(String s, int a) {
		int ind = append(s, null);
		if(ind>=v.size()) v.setSize(ind+1);
		v.setElementAt(new Integer(a), ind);
	}

	public int getSelectedItem() {
		int i = getSelectedIndex();
		if(i==-1) return 0;
		Integer a = (Integer)v.elementAt(i);
		if(a==null) return 0;
		return a.intValue();
	}
}
