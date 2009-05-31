import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.util.*;
import java.io.*;

public class FileManager extends Menu implements CommandListener, Runnable {
	private Jmp midlet;
	private static final String cnfDir = "fm_dir";
	private static final Command cmdDone = new Command("Готово", Command.OK, 0);
	private static final Command cmdBack = new Command("Отмена", Command.BACK, 0);
	private static final Command cmdSelect = new Command("Открыть", Command.ITEM, 0);
	private static final Command cmdAdd = new Command("Добавить", Command.ITEM, 0);
	private String dir;
	private Vector files;
	private Vector files2;
	private Vector v;

	private static final int CMD_NULL = 0;
	private static final int CMD_CD = 1;
	private static final int CMD_ADD = 2;
	private static final int CMD_REBUILD = 3;
	private String currentFname;
	private int command;
	private Thread thread;

	FileManager(Jmp m) {
		super(m.display, "Выберите файл:", IMPLICIT);
		midlet = m;

		dir = midlet.config.getString(cnfDir, "/");
		
		files = new Vector();
		files2 = new Vector();
		
		setSelectCommand(cmdSelect);
		addCommand(cmdDone);
		addCommand(cmdBack);
		addCommand(cmdAdd);
		setCommandListener(this);
	}

	private boolean isFileIncluded(String path) {
		for(int i=0;i<files.size();i++) {
			String s = (String)files.elementAt(i);
			if(s.equals(path)) return true;
		}
		return false;
	}

	private void includeFile(String path) {
		if(!midlet.stForm.getOpt(SettingsForm.OPT_FMADD)) {
			if(midlet.list.hasFile("file://"+path)) return;
		}
		Vector v = new Vector();
		Vector v2 = new Vector();
		for(int i=0;i<files.size();i++) {
			v.addElement(files.elementAt(i));
			v2.addElement(files2.elementAt(i));
		}
		v.addElement(path);
		v2.addElement(new PlayListItem(midlet, "file://"+path));
		files = v;
		files2 = v2;
	}

	private void excludeFile(String path) {
		Vector v = new Vector();
		Vector v2 = new Vector();
		for(int i=0;i<files.size();i++) {
			String s = (String)files.elementAt(i);
			if(s.equals(path)) continue;
			v.addElement(s);
			v2.addElement(files2.elementAt(i));
		}
		files = v;
		files2 = v2;
	}

	private void includeDir(String dir) {
		Vector files = readDir(dir);
		for(int i=0;i<files.size();i++) {
			String name = (String)files.elementAt(i);
			if(isDir(dir+name))	{
				includeDir(dir+name);
			} else {
				includeFile(dir+name);
			}
		}
	}

	private String getShowName(String path, String name) {
		String pr = "";
		if(name.endsWith("/")) {
			name = name.substring(0, name.length()-1);
			pr = "/";
		}
		if(midlet.list.hasFile("file://"+path)) {
			pr = "*";
		}
		if(isFileIncluded(path)) {
			pr = "#";
		}
		return pr+name;
	}

	private void addMenuElem(String name, String name2) {
		int ind = append(name2, null);
		if(ind>=v.size()) v.setSize(ind+1);
		v.setElementAt(name, ind);
	}

	private Vector readDir(String dir) throws SecurityException {
		Vector r = new Vector();
		try {
			if(dir.equals("/"))	{
				Enumeration e = FileSystemRegistry.listRoots();
				while(e.hasMoreElements()) {
					String name = (String)e.nextElement();
					r.addElement(name);
				}
			} else {
				FileConnection fc = (FileConnection)Connector.open("file://"+dir, Connector.READ);
				Enumeration e = fc.list("*", true);
				while(e.hasMoreElements()) {
					String name = (String)e.nextElement();
					r.addElement(name);
				}
				fc.close();
			}
		} catch(Exception e) { }
		return r;
	}

	private boolean isDir(String path) throws SecurityException {
		/*
		try
		{
			if(path.equals("/"))
			{
				return true;
			}
			else
			{
				FileConnection fc = (FileConnection)Connector.open("file://"+path, Connector.READ);
				boolean r = fc.isDirectory();
				fc.close();
				return r;
			}
		}
		catch(IOException e)
		{
		}
		return false;
		 * */
		
		// Это не бага, это фича
		return path.endsWith("/");
	}

	private void rebuild(boolean reread) {
		deleteAll();
		if(reread==false) {
			Vector v0 = v;
			v = new Vector();
			for(int i=0;i<v0.size();i++) {
				String name = (String)v0.elementAt(i);
				String name2 = getShowName(dir+name, name);
				addMenuElem(name, name2);
			}
			return;
		}
		v = new Vector();
		try {
			if(!dir.equals("/")) {
				addMenuElem("..", "..");
			}
			Vector elems = readDir(dir);
			for(int i=0;i<elems.size();i++) {
				String name = (String)elems.elementAt(i);
				String name2 = getShowName(dir+name, name);
				addMenuElem(name, name2);
			}
		} catch(SecurityException e) {
			showError();
		}
	}

	public void commandAction(Command cmd, Displayable d) {
		if(cmd==cmdDone) {
			for(int i=0;i<files2.size();i++) {
				PlayListItem item = (PlayListItem)files2.elementAt(i);
				midlet.list.add(item);
			}
			midlet.list.save(null);
			midlet.plMenu.rebuild();
			close();
		} else if(cmd==cmdBack) {
			close();
		} else if(cmd==cmdSelect) {
			int ind = getSelectedIndex();
			if(ind==-1) return;
			String fname = (String)v.elementAt(ind);
			if(fname==null) return;

			currentFname = fname;
			command = CMD_CD;
		} else if(cmd==cmdAdd) {
			int ind = getSelectedIndex();
			if(ind==-1) return;
			String fname = (String)v.elementAt(ind);
			if(fname==null) return;

			currentFname = fname;
			command = CMD_ADD;
		}
	}

	public void show() {
		files = new Vector();
		if(thread==null) {
			thread = new Thread(this);
			thread.start();
		}
		command = CMD_REBUILD;
		super.show();
	}

	public void close() {
		midlet.config.setString(cnfDir, dir);
		super.close();
	}

	private void showError() {
		Alert a = new Alert("Ошибка", "Нет доступа к файловой системе", null, AlertType.INFO);
		a.setTimeout(Alert.FOREVER);
		display.setCurrent(a, this);
	}

	public void run() {
		try	{
			while(true)	{
				int cmd = command;
				if(cmd==CMD_NULL) {
					try {
						Thread.sleep(100);
					} catch(InterruptedException e) { }
					continue;
				}
				command = CMD_NULL;
				if(cmd==CMD_REBUILD) {
					rebuild(true);
					continue;
				}
				String fname = currentFname;
				if(fname==null) return;
				if(fname.equals("..")) {
					if(cmd==CMD_CD) {
						String d2 = dir;
						String d = dir.substring(0, dir.length()-1);
						int ind = d.lastIndexOf('/');
						dir = d.substring(0, ind+1);
					}
					rebuild(true);
					continue;
				}
				String path = dir + fname;
				if(isDir(path)) {
					if(cmd==CMD_CD) {
						dir = path;
						rebuild(true);
					} else if(cmd==CMD_ADD) {
						includeDir(path);
						rebuild(false);
					}
				} else {
					if(isFileIncluded(path)) {
						excludeFile(path);
					} else {
						includeFile(path);
					}
					rebuild(false);
				}
			}
		} catch(SecurityException e) {
			showError();
		}
	}
}
