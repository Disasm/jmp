import javax.microedition.lcdui.*;
import javax.microedition.media.*;

public class AboutForm extends Form implements CommandListener {
	private Jmp midlet;
	private static final Command cmdBack = new Command("Назад", Command.BACK, 0);

	public AboutForm(Jmp m) {
		super("О программе");
		midlet = m;

		String[] types = Manager.getSupportedContentTypes("file");
		String ts = "";
		for(int i=0;i<types.length;i++) {
			if(types[i].indexOf("audio/")!=-1) {
				ts = ts + types[i].substring(6) + "\n";
			}
		}

		Runtime r = Runtime.getRuntime();
		long mFree = r.freeMemory();
		long mTotal = r.totalMemory();
		long mUsed = mTotal - mFree;
		mTotal /= 1024;
		mUsed /= 1024;
		String mem = "Скушано памяти: "+mUsed+"/"+mTotal+"кб";

		StringItem si = new StringItem("", "Java Media Player\n\nby Riateche, Disasm\n\nПоддерживаемые форматы:\n"+ts+"\n"+mem);
		append(si);

		addCommand(cmdBack);
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if(c==cmdBack) {
			close();
		}
	}

	
	private Displayable oldDisp = null;

	private void close() {
		midlet.display.setCurrent(oldDisp);
	}

	public void show() {
		oldDisp = midlet.display.getCurrent();
		midlet.display.setCurrent(this);
	}
}
