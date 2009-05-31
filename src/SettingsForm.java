import javax.microedition.lcdui.*;
import java.util.*;

public class SettingsForm extends Form implements CommandListener {
	private Jmp midlet;
	private static final Command cmdSave = new Command("Сохранить", Command.OK, 0);
	private static final Command cmdBack = new Command("Отмена", Command.BACK, 0);
	public static final int OPT_FMADD = 1;
	public static final int OPT_FULLSCREEN = 2;
	public static final int OPT_COMMAND = 3;
	public static final int OPT_MINIMIZE = 4;
	public static final int OPT_READTAGS = 5;
	public static final int OPT_SHOWTAGS = 6;
	public static final int OPT_PLMENU = 7;
	private Vector optNames;
	private Vector optVals;
	private Vector optIndex;
	private ChoiceGroup cg;

	public SettingsForm(Jmp m) {
		super("Настройки");
		midlet = m;

		optNames = new Vector();
		optVals = new Vector();
		optIndex = new Vector();

		cg = new ChoiceGroup("Разное", Choice.MULTIPLE);
		addOpt(OPT_FMADD, "fm_add", "дубликаты");
		addOpt(OPT_FULLSCREEN, "pl_fullscreen", "полный экран");
		addOpt(OPT_COMMAND, "pl_command", "доп. команда");
		addOpt(OPT_MINIMIZE, "minimize", "сворачивать");
		addOpt(OPT_READTAGS, "pl_read_tags", "читать теги");
		addOpt(OPT_SHOWTAGS, "pl_show_tags", "показывать теги");
		addOpt(OPT_PLMENU, "pl_menu", "переходить в меню");
		append(cg);

		addCommand(cmdSave);
		addCommand(cmdBack);
		setCommandListener(this);
	}

	private void addOpt(int opt, String name, String vname) {
		if(opt>=optNames.size()) optNames.setSize(opt+1);
		optNames.setElementAt(name, opt);
		
		if(opt>=optVals.size()) optVals.setSize(opt+1);
		boolean v = midlet.config.getBoolean(name, false);
		optVals.setElementAt(new Boolean(v), opt);

		int i = cg.append(vname, null);
		if(i>=optIndex.size()) optIndex.setSize(i+1);
		optIndex.setElementAt(new Integer(opt), i);

		if(v) cg.setSelectedIndex(i, true);
	}

	public boolean getOpt(int opt) {
		Boolean o = (Boolean)optVals.elementAt(opt);
		if(o==null) return false;
		return o.booleanValue();
	}

	private void forceRefresh(int opt) {
		if(opt==OPT_FULLSCREEN) {
			midlet.pCanvas.updateFullscreen();
		} else if(opt==OPT_COMMAND) {
			midlet.pCanvas.updateCommand();
		} else if(opt==OPT_PLMENU) {
			midlet.pCanvas.updateCommand2();
		} else if(opt==OPT_MINIMIZE) {
			midlet.mainMenu.rebuild();
		}
	}

	private void saveIndex(int ind) {
		boolean v = cg.isSelected(ind);
		
		Integer o = (Integer)optIndex.elementAt(ind);
		if(o==null) return;
		int opt = o.intValue();
		
		String name = (String)optNames.elementAt(opt);
		if(name==null) return;

		optVals.setElementAt(new Boolean(v), opt);

		midlet.config.setBoolean(name, v);

		forceRefresh(opt);
	}

	public void commandAction(Command c, Displayable d) {
		if(c==cmdSave) {
			for(int i=0;i<cg.size();i++) {
				saveIndex(i);
			}
			close();
		} else if(c==cmdBack) {
			close();
		}
	}



	private Stack oldDisp = new Stack();

	private void close() {
		midlet.display.setCurrent((Displayable)oldDisp.pop());
	}

	public void show() {
		oldDisp.push(midlet.display.getCurrent());
		midlet.display.setCurrent(this);
	}
}
