import javax.microedition.lcdui.*;
import java.util.*;

public class PlayerCanvas extends Canvas implements CommandListener, Runnable {
	private Jmp midlet;
	private Display display;
	private MyPlayer player;
	private static final Command cmdBack = new Command("Назад", Command.BACK, 0);
	private int rewindState = 0;
	private int rewindSpeed = 5000000;
	private boolean visible = false;
	private Thread thread;
	private boolean canFullscreen = false;

	PlayerCanvas(Jmp m) {
		midlet = m;
		display = midlet.display;
		player = midlet.player;
		addCommand(cmdBack);
		setCommandListener(this);


		String microeditionPlatform = System.getProperty("microedition.platform");
		if (microeditionPlatform != null) {
			if(microeditionPlatform.toLowerCase().indexOf("ericsson") != -1) {
				canFullscreen = true;
			}
		}

		if(canFullscreen) setFullScreenMode(true);
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
			if(item.title!=null) {
				y = writeln(y, dh, g, item.title);
				if(item.artist!=null) {
					y = writeln(y, dh, g, item.artist);
				}
				if(item.album!=null) {
					y = writeln(y, dh, g, item.album);
				}
			} else {
				y = writeln(y, dh, g, item.name);
			}
			int state = player.state();
			if(state!=MyPlayer.STATE_DEAD) {
				y = writeln(y, dh, g, time2str(player.position())+"/"+time2str(player.duration()));
				y = writeln(y, dh, g, "["+(player.current()+1)+"/"+player.size()+"]");
			}
		}
		if(player.shuffle) {
			y = writeln(y, dh, g, "[shuffle]");
		} else {
			switch(player.repeat) {
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
	}

	public void commandAction(Command c, Displayable d) {
		if (c == cmdBack) {
			close();
		}
	}

	protected void showNotify() {
		if(canFullscreen) setFullScreenMode(true);
		visible = true;
	}

	protected void hideNotify() {
		visible = false;
	}

	public void keyPressed(int keyCode) {
		long dt;
		switch (keyCode) {
			case KEY_NUM8:
				player.playNext();
				break;
			case KEY_NUM2:
				player.playPrev();
				break;
			case KEY_NUM3:
				player.setVolume(player.getVolume()+10);
				break;
			case KEY_NUM9:
				player.setVolume(player.getVolume()-10);
				break;
			case KEY_NUM5:
				if(player.state()==MyPlayer.STATE_DEAD) {
					player.play(player.getCurrentItem());
				} else {
					player.pause();
				}
				break;
			case KEY_NUM4:
				rewindState = -1;
				dt = rewindState * rewindSpeed;
				player.rewind(dt);
				break;
			case KEY_NUM6:
				rewindState = 1;
				dt = rewindState * rewindSpeed;
				player.rewind(dt);
				break;
			case KEY_STAR:
				midlet.plMenu.show();
				break;
			case KEY_POUND:
				player.shuffle = !player.shuffle;
				break;
			case KEY_NUM0:
				switch(player.repeat) {
					case MyPlayer.REPEAT_ALL:
						player.repeat = MyPlayer.REPEAT_ONE;
						break;
					case MyPlayer.REPEAT_ONE:
						player.repeat = MyPlayer.REPEAT_NONE;
						break;
					case MyPlayer.REPEAT_NONE:
						player.repeat = MyPlayer.REPEAT_ALL;
						break;
				}
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

	public void run() {
		while(true)
		{
			if(visible) repaint();
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
