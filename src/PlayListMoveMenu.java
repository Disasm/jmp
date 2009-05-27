import javax.microedition.lcdui.*;
import java.util.*;

public class PlayListMoveMenu extends Menu implements CommandListener {
	private Jmp midlet;
	private static final Command cmdSelect = new Command("ОК", Command.OK, 0);
	private static final Command cmdBack = new Command("Отмена", Command.BACK, 0);
	private Vector v;
	private PlayListItem oldItem;

	PlayListMoveMenu(Jmp m) {
		super(m.display, "После файла:", Choice.IMPLICIT);
		midlet = m;

		setSelectCommand(cmdSelect);
		addCommand(cmdBack);
		setCommandListener(this);

		v = new Vector();
	}

	private void rebuild() {
		v = new Vector();
		deleteAll();
		append("<в начало>", null);
		for(int i=0;i<midlet.list.size();i++) {
			PlayListItem item = midlet.list.elementAt(i);
			int ind = append(item.name, null);
			if(ind>=v.size()) v.setSize(ind+1);
			v.setElementAt(item, ind);
		}
	}
	
	public void commandAction(Command cmd, Displayable d) {
		if(cmd==cmdSelect) {
			int ind = getSelectedIndex();
			if(ind==-1) return;
			PlayListItem item = (PlayListItem)v.elementAt(ind);
			midlet.list.moveAfter(oldItem, item);
			midlet.plMenu.rebuild();
			close();
		} else if(cmd==cmdBack) {
			close();
		}
	}

	public void show(PlayListItem old) {
		System.out.println("=show1");
		oldItem = old;
		rebuild();
		System.out.println("=show2");
		super.show();
		System.out.println("=show3");
	}
}
