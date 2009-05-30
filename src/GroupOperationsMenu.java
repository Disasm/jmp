import javax.microedition.lcdui.*;
import java.util.*;

public class GroupOperationsMenu extends Menu implements CommandListener {
	private Jmp midlet;
	private static final Command cmdBack = new Command("Назад", Command.BACK, 0);
	private static final Command cmdMove = new Command("Переместить", Command.ITEM, 1);
	private static final Command cmdRemove = new Command("Удалить", Command.ITEM, 1);
	private static final Command cmdAll = new Command("Выделить все", Command.SCREEN, 3);
	private static final Command cmdNone = new Command("Снять выдел.", Command.SCREEN, 3);
	private Vector v;

	GroupOperationsMenu(Jmp m) {
		super(m.display, "Групповые операции", Choice.MULTIPLE);
		midlet = m;

		addCommand(cmdBack);
		addCommand(cmdMove);
		addCommand(cmdRemove);
		addCommand(cmdAll);
		addCommand(cmdNone);
		setCommandListener(this);

		v = new Vector();
	}

	public void rebuild() {
		deleteAll();
		v = new Vector();
		for(int i=0;i<midlet.list.size();i++) {
			PlayListItem item = midlet.list.elementAt(i);
			int ind = append(item.name, null);
			if(ind>=v.size()) v.setSize(ind+1);
			v.setElementAt(item, ind);
		}
	}

	public void commandAction(Command cmd, Displayable d) {
		if(cmd==cmdMove) {
			Vector v2 = new Vector();
			for(int i=0;i<size();i++) {
				if(isSelected(i)) {
					v2.addElement(v.elementAt(i));
				}
			}
			midlet.plmMenu.show(v2);
		} else if(cmd==cmdRemove) {
			Vector v2 = new Vector();
			for(int i=0;i<size();i++) {
				if(isSelected(i)) {
					v2.addElement(v.elementAt(i));
				}
			}
			midlet.list.delete(v2);
			rebuild();
		} else if(cmd==cmdAll) {
			for(int i=0;i<size();i++) {
				setSelectedIndex(i, true);
			}
		} else if(cmd==cmdNone) {
			for(int i=0;i<size();i++) {
				setSelectedIndex(i, false);
			}
		} else if(cmd==cmdBack) {
			close();
		}
	}

	public void show() {
		rebuild();
		super.show();
	}
}
