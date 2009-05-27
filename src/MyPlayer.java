import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.*;
import java.util.*;

public class MyPlayer implements PlayerListener, Runnable {
	private Jmp midlet;
	private PlayList list;
	public static final int STATE_PLAYING = 1;
	public static final int STATE_PAUSED = 2;
	public static final int STATE_DEAD = 3;
	public static final int REPEAT_NONE = 1;
	public static final int REPEAT_ALL = 2;
	public static final int REPEAT_ONE = 3;
	private PlayListItem lastItem;
	private int lastIndex;
	private Player p;
	private VolumeControl vc;
	private int volume;
	public boolean shuffle;
	public int repeat = REPEAT_ALL;

	private Thread thread;
	private PlayListItem threadItem;

	MyPlayer(Jmp m) {
		midlet = m;
		list = midlet.list;
		lastItem = null;
		if(list.size()!=0) {
			lastIndex = 0;
			lastItem = list.elementAt(0);
		} else {
			lastIndex = -1;
		}
		volume = 70;
	}

	public void rebuild() {
		lastIndex = list.indexOf(lastItem);
	}

	private void stop() {
		if(p==null) return;
		try {
			p.stop();
			p.close();
			p = null;
		} catch(MediaException e) {}
		catch(IllegalStateException e) {}
	}

	public void pause() {
		if(!alive()) return;
		
		int s = p.getState();
		if(s==Player.STARTED) {
			try {
				p.stop();
			} catch(MediaException e) {}
		} else if(s==Player.PREFETCHED) {
			try {
				p.start();
			} catch(MediaException e) {}
		}
	}

	public void rewind(long dt) {
		if(!alive()) return;
		
		long dur = p.getDuration();
		long time = p.getMediaTime();
		time += dt;
		if (time < 0) time = 0;
		if (time > dur) time = dur-100;
		try {
			p.setMediaTime(time);
		}
		catch (MediaException e) {}
		catch (NullPointerException e) {}
	}

	private int getShuffleIndex() {
		int n = list.size();
		if(n==1) {
			return 0;
		} else {
			int ind0 = lastIndex;
			Random rnd = new Random();
			while(true) {
				int i = rnd.nextInt(n);
				if(i!=lastIndex) return i;
			}
		}
	}

	public void playPrev() {
		if(list.size()==0) return;
		int i;
		if(shuffle) {
			i = getShuffleIndex();
		} else {
			i = list.indexOf(lastItem);
			i--;
			if(i<0) i = list.size()-1;
		}
		PlayListItem nextItem = list.elementAt(i);
		if(nextItem==null) return;
		play(nextItem);
	}

	public void playNext() {
		if(list.size()==0) return;
		int i;
		if(shuffle) {
			i = getShuffleIndex();
		} else {
			i = list.indexOf(lastItem);
			i++;
			if(i==list.size()) i = 0;
		}
		PlayListItem nextItem = list.elementAt(i);
		if(nextItem==null) return;
		play(nextItem);
	}

	private void playNext2() {
		if(list.size()==0) return;
		int i;
		if(shuffle) {
			i = getShuffleIndex();
		} else {
			i = list.indexOf(lastItem);
			switch(repeat) {
				case REPEAT_NONE:
					if(i==(list.size()-1)) return;
				case REPEAT_ONE:
					break;
				case REPEAT_ALL:
					i++;
					if(i==list.size()) i = 0;
					break;
			}
		}
		PlayListItem nextItem = list.elementAt(i);
		if(nextItem==null) return;
		play(nextItem);
	}

	public void playerUpdate(Player player, String event, Object eventData) {
		if (event == PlayerListener.END_OF_MEDIA) {
			stop();
			playNext2();
		}
	}


	// Количество песен в плейлисте
	public int size() {
		return list.size();
	}

	// Индекс текущей песни, -1 если она не в плейлисте
	public int current() {
		return lastIndex;
	}

	// Длительность текущей песни
	public long duration() {
		if(!alive()) return 0;
		return p.getDuration();
	}

	// Текущая позиция в песне
	public long position() {
		if(!alive()) return 0;
		return p.getMediaTime();
	}

	// true если песня играет или на паузе
	public boolean alive() {
		if(p==null) return false;
		int s = p.getState();
		return ((s==Player.STARTED)||(s==Player.PREFETCHED));
	}

	public int state() {
		if(p==null) return STATE_DEAD;
		int s = p.getState();
		if(s==Player.STARTED) return STATE_PLAYING;
		if(s==Player.PREFETCHED) return STATE_PAUSED;
		return STATE_DEAD;
	}

	public PlayListItem getCurrentItem() {
		return lastItem;
	}
	

	// Управление громкостью

	public int getVolume() {
		return volume;
	}

	public int getMaxVolume() {
		return 100;
	}

	public void setVolume(int v) {
		if(v>100) v = 100;
		if(v<0) v = 0;
		volume = v;
		if(alive()) vc.setLevel(v);
	}

	public void run() {
		PlayListItem item = threadItem;
		if(item==null) return;
		stop();
		try	{
			p = Manager.createPlayer(item.path);
			p.setLoopCount(1);
			p.addPlayerListener(this);
			p.start();
			vc = (VolumeControl)p.getControl("VolumeControl");
			vc.setLevel(volume);

			lastItem = item;
			lastIndex = list.indexOf(lastItem);
			return;
		}
		catch(IOException e) {
			try {
				p.close();
				p = null;
			} catch(Exception e2) {}
			list.delete(item);
		}
		catch(MediaException e) {
			try {
				p.close();
				p = null;
			} catch(Exception e2) {}
			list.delete(item);
		}
		catch(SecurityException e) {
			try {
				p.close();
				p = null;
			} catch(Exception e2) {}
		}
		catch(IllegalStateException e) {
			try {
				p.close();
				p = null;
			} catch(Exception e2) {}
		}
	}




	public void play(PlayListItem item) {
		threadItem = item;
		thread = new Thread(this);
		thread.start();
	}
}
