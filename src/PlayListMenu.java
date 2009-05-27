import javax.microedition.lcdui.*;
import java.util.*;

public class PlayListMenu extends Menu implements CommandListener {
	private Jmp midlet;
	private static final Command cmdSelect = new Command("Играть", Command.OK, 0);
	private static final Command cmdBack = new Command("Назад", Command.BACK, 3);
	private static final Command cmdAdd = new Command("Добавить", Command.ITEM, 1);
	private static final Command cmdMove = new Command("Переместить", Command.ITEM, 2);
	private static final Command cmdDel = new Command("Удалить", Command.ITEM, 2);
	private static final Command cmdClear = new Command("Очистить", Command.SCREEN, 2);
	private static final Command cmdGroup = new Command("Групповые операции", Command.SCREEN, 2);
	private static final Command cmdSave = new Command("Сохранить", Command.SCREEN, 2);
	private static final Command cmdSaveOK = new Command("OK", Command.OK, 1);
	private static final Command cmdSaveCancel = new Command("Отмена", Command.CANCEL, 2);
	private TextBox savename;
	private PlayList list;
	private Vector v;

	PlayListMenu(Jmp m) {
		//super((l.name==null)?"":l.name, Choice.IMPLICIT);
		super(m.display, m.list.name, Choice.IMPLICIT);
		midlet = m;
		list = m.list;
		rebuild();
		setSelectCommand(cmdSelect);
		addCommand(cmdAdd);
		addCommand(cmdMove);
		addCommand(cmdDel);
		addCommand(cmdClear);
		addCommand(cmdGroup);
		addCommand(cmdSave);
		addCommand(cmdBack);
		setCommandListener(this);

		savename = new TextBox("Название списка:", "", 50, TextField.INITIAL_CAPS_SENTENCE);
		savename.addCommand(cmdSaveOK);
		savename.addCommand(cmdSaveCancel);
		savename.setCommandListener(this);
	}

	private void addMenuElem(PlayListItem item) {
		int ind = append(item.name, null);
		if(ind>=v.size()) v.setSize(ind+1);
		v.setElementAt(item, ind);
	}

	public void itemChanged(PlayListItem item) {
		int ind = v.indexOf(item);
		if(ind==-1) return;
		set(ind, item.name, null);
	}

	public void rebuild() {
		deleteAll();
		v = new Vector();
		for(int i=0;i<list.size();i++) {
			PlayListItem item = list.elementAt(i);
			addMenuElem(item);
		}
		setTitle(list.name);
	}

	public PlayListItem getSelectedItem() {
		int ind = getSelectedIndex();
		if(ind==-1) return null;
		PlayListItem item = (PlayListItem)v.elementAt(ind);
		return item;
	}

	public void commandAction(Command cmd, Displayable d) {
		if(cmd==cmdAdd) {
			midlet.fileManager.show();
		} else if(cmd==cmdMove) {
			PlayListItem item = getSelectedItem();
			if(item==null) return;
			midlet.plmMenu.show(item);
		} else if(cmd==cmdDel) {
			PlayListItem item = getSelectedItem();
			if(item==null) return;
			list.delete(item);
		} else if(cmd==cmdClear) {
			list.clear();
		} else if(cmd==cmdGroup) {
			//
		} else if(cmd==cmdSave) {
			if(list.name!=null) {
				savename.setString(list.name);
			}
			display.setCurrent(savename);
		} else if(cmd==cmdSaveOK) {
			String s = savename.getString();
			if(s.length()>0) {
				setTitle(s);
				list.name = s;
			} else {
				setTitle("");
				list.name = null;
			}
			list.save();
			midlet.plsMenu.rebuild();
			display.setCurrent(this);
		} else if(cmd==cmdSaveCancel) {
			display.setCurrent(this);
		} else if(cmd==cmdBack) {
			close();
		} else if(cmd==cmdSelect) {
			PlayListItem item = getSelectedItem();
			midlet.player.play(item);
			if(parent()==midlet.pCanvas) {
				close();
			} else {
				midlet.pCanvas.show();
			}
		}
	}
}
