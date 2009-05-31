import javax.microedition.lcdui.*;
import java.util.*;

public class PlayerCanvas extends Canvas implements CommandListener, Runnable {
	private Jmp midlet;
	private Display display;
	private MyPlayer player;
	private static final Command cmdBack = new Command("Меню", Command.BACK, 0);
	private static final Command cmdPause = new Command("Пауза", Command.OK, 0);
	private int rewindState = 0;
	private int rewindSpeed = 5000000;
	private boolean visible = false;
	private Thread thread;
	private int showVolume = 0;

	PlayerCanvas(Jmp m) {
		midlet = m;
		display = midlet.display;
		player = midlet.player;
		addCommand(cmdBack);
		setCommandListener(this);

		if(midlet.stForm.getOpt(SettingsForm.OPT_COMMAND)) addCommand(cmdPause);

		if(midlet.stForm.getOpt(SettingsForm.OPT_FULLSCREEN)) setFullScreenMode(true);
	}

	public void updateCommand() {
		removeCommand(cmdPause);
		if(midlet.stForm.getOpt(SettingsForm.OPT_COMMAND)) addCommand(cmdPause);
	}

	public void updateFullscreen() {
		if(midlet.stForm.getOpt(SettingsForm.OPT_FULLSCREEN)) {
			setFullScreenMode(true);
		}
		else {
			setFullScreenMode(false);
		}
	}

	private int writeln(int oldy, int dh, Graphics g, String s)
	{
		g.drawString(s, 0, oldy, Graphics.TOP | Graphics.LEFT);
		return oldy + dh;
	}

	protected void paint(Graphics g) {
		int width = this.getWidth();
		int height = this.getHeight();
		int dh = g.getFont().getHeight();
		g.setColor(0x000000);
		g.fillRect(0, 0, width, height);
		g.setColor(0xffffff);
		int y = 0;
		PlayListItem item = player.getCurrentItem();
		if(item!=null) {
			y = writeln(y, dh, g, item.name);
			if(midlet.stForm.getOpt(SettingsForm.OPT_SHOWTAGS)) {
				if(item.title!=null) {
					y = writeln(y, dh, g, item.title);
					if(item.artist!=null) {
						y = writeln(y, dh, g, item.artist);
					}
					if(item.album!=null) {
						y = writeln(y, dh, g, item.album);
					}
				}
			}
			int state = player.state();
			if(state!=MyPlayer.STATE_DEAD) {
				y = writeln(y, dh, g, time2str(player.position())+"/"+time2str(player.duration()));
				String s = "";
				if(state==MyPlayer.STATE_PLAYING) {
					s = "is playing";
				} else {
					s = "paused";
				}
				y = writeln(y, dh, g, "["+(player.current()+1)+"/"+player.size()+"] "+s);
			}
		}
		if(player.getShuffle()) {
			y = writeln(y, dh, g, "[shuffle]");
		} else {
			switch(player.getRepeat()) {
				case MyPlayer.REPEAT_ALL:
					y = writeln(y, dh, g, "[repeat all]");
					break;
				case MyPlayer.REPEAT_ONE:
					y = writeln(y, dh, g, "[repeat one]");
					break;
				case MyPlayer.REPEAT_NONE:
					y = writeln(y, dh, g, "[repeat none]");
					break;
			}
		}
		if(showVolume>0) {
			showVolume--;
			int v = player.getVolume();
			y = writeln(y, dh, g, "[volume "+v+"%]");
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == cmdBack) {
			close();
		} else if(c==cmdPause) {
			actionPause();
		}
	}

	protected void showNotify() {
		if(midlet.stForm.getOpt(SettingsForm.OPT_FULLSCREEN)) setFullScreenMode(true);
		visible = true;
	}

	protected void hideNotify() {
		visible = false;
	}

	private void actionPause() {
		if(player.state()==MyPlayer.STATE_DEAD) {
			player.play(player.getCurrentItem());
		} else {
			player.pause();
		}
		repaint2();
	}

	public void keyRepeated(int keyCode) {
		int ga = getGameAction(keyCode);
		switch(ga) {
			case LEFT:
				player.rewind(-rewindSpeed);
				repaint2();
				return;
			case RIGHT:
				player.rewind(rewindSpeed);
				repaint2();
				return;
		}
		switch (keyCode) {
			case KEY_NUM4:
				player.rewind(-rewindSpeed);
				repaint2();
				return;
			case KEY_NUM6:
				player.rewind(rewindSpeed);
				repaint2();
				return;
		}
	}

	public void keyPressed(int keyCode) {
		int ga = getGameAction(keyCode);
		switch(ga) {
			case LEFT:
				player.rewind(-rewindSpeed);
				repaint2();
				return;
			case RIGHT:
				player.rewind(rewindSpeed);
				repaint2();
				return;
			case UP:
				player.playPrev();
				return;
			case DOWN:
				player.playNext();
				return;
			case FIRE:
				actionPause();
				return;
		}
		switch (keyCode) {
			case KEY_NUM8:
				player.playNext();
				return;
			case KEY_NUM2:
				player.playPrev();
				return;
			case KEY_NUM4:
				player.rewind(-rewindSpeed);
				repaint2();
				return;
			case KEY_NUM6:
				player.rewind(rewindSpeed);
				repaint2();
				return;
			case KEY_NUM5:
				actionPause();
				return;
			case KEY_NUM3:
				player.setVolume(player.getVolume()+10);
				showVolume = 5;
				repaint2();
				break;
			case KEY_NUM9:
				player.setVolume(player.getVolume()-10);
				showVolume = 5;
				repaint2();
				break;
			case KEY_STAR:
				midlet.plMenu.show();
				break;
			case KEY_POUND:
				player.setShuffle(!player.getShuffle());
				repaint2();
				break;
			case KEY_NUM0:
				switch(player.getRepeat()) {
					case MyPlayer.REPEAT_ALL:
						player.setRepeat(MyPlayer.REPEAT_ONE);
						break;
					case MyPlayer.REPEAT_ONE:
						player.setRepeat(MyPlayer.REPEAT_NONE);
						break;
					case MyPlayer.REPEAT_NONE:
						player.setRepeat(MyPlayer.REPEAT_ALL);
						break;
				}
				repaint2();
				break;
		}
	}

	private String time2str(long msec) {
		long sec = msec / 1000000;
		long min = sec / 60;
		sec -= min*60;
		String m = String.valueOf(min);
		String s = String.valueOf(sec);
		if ( m.length() == 1) m = "0"+m;
		if ( s.length() == 1) s = "0"+s;
		return m+":"+s;
	}

	public void repaint2() {
		if(visible) repaint();
	}

	public void run() {
		while(true)
		{
			repaint2();
			try {
				Thread.sleep(500);
			} catch (InterruptedException ex) {}
		}
	}



	private Stack oldDisp = new Stack();

	protected void close() {
		display.setCurrent((Displayable)oldDisp.pop());
	}

	public void show() {
		if(thread==null)
		{
			thread = new Thread(this);
			thread.start();
		}
		oldDisp.push(display.getCurrent());
		display.setCurrent(this);
	}
}
