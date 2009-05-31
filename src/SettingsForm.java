import javax.microedition.lcdui.*;
import java.util.*;

public class SettingsForm extends Form implements CommandListener {
	private Jmp midlet;
	private static final Command cmdSave = new Command("Сохранить", Command.OK, 0);
	private static final Command cmdBack = new Command("Отмена", Command.BACK, 0);
	private static final String cnfFmAdd = "fm_add";
	private static final String cnfFullscreen = "pl_fullscreen";
	private static final String cnfCommand = "pl_command";
	private static final String cnfMinimize = "minimize";
	private boolean setFmAdd;
	private boolean setFullscreen;
	private boolean setCommand;
	private boolean setMinimize;
	private ChoiceGroup cg;

	public SettingsForm(Jmp m) {
		super("Настройки");
		midlet = m;

		setFmAdd = midlet.config.getBoolean(cnfFmAdd, false);
		setFullscreen = midlet.config.getBoolean(cnfFullscreen, false);
		setCommand = midlet.config.getBoolean(cnfCommand, false);
		setMinimize = midlet.config.getBoolean(cnfMinimize, false);

		cg = new ChoiceGroup("Разное", Choice.MULTIPLE);
		cg.append("добавлять * в ФМ", null);
		cg.append("полный экран", null);
		cg.append("доп. команда", null);
		cg.append("сворачивать", null);
		if(setFmAdd) cg.setSelectedIndex(0, true);
		if(setFullscreen) cg.setSelectedIndex(1, true);
		if(setCommand) cg.setSelectedIndex(2, true);
		if(setMinimize) cg.setSelectedIndex(3, true);

		append(cg);

		addCommand(cmdSave);
		addCommand(cmdBack);
		setCommandListener(this);
	}

	public boolean getFullscreen() {
		return setFullscreen;
	}

	public boolean getFmAdd() {
		return setFmAdd;
	}

	public boolean getCommand() {
		return setCommand;
	}

	public boolean getMinimize() {
		return setMinimize;
	}

	public void commandAction(Command c, Displayable d) {
		if(c==cmdSave) {
			if(cg.isSelected(0)!=setFmAdd) {
				setFmAdd = cg.isSelected(0);
				midlet.config.setBoolean(cnfFmAdd, setFmAdd);
			}
			if(cg.isSelected(1)!=setFullscreen) {
				setFullscreen = cg.isSelected(1);
				midlet.config.setBoolean(cnfFullscreen, setFullscreen);
			}
			if(cg.isSelected(2)!=setCommand) {
				setCommand = cg.isSelected(2);
				midlet.config.setBoolean(cnfCommand, setCommand);
			}
			if(cg.isSelected(3)!=setMinimize) {
				setMinimize = cg.isSelected(3);
				midlet.config.setBoolean(cnfMinimize, setMinimize);
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
