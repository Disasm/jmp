import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class Jmp extends MIDlet implements CommandListener {
	private boolean firstTime = true;
	public static Jmp midlet = null;
	public Display display;
	public MainMenu mainMenu;
	public PlayListStore listStore;
	public PlayList list;
	public PlayListMenu plMenu;
	public PlayListMoveMenu plmMenu;
	public PlayListsMenu plsMenu;
	public PlayerCanvas pCanvas;
	public FileManager fileManager;
	public MyPlayer player;
	
    public void startApp() {
		if(firstTime) {
			midlet = this;
			firstTime = false;
			display = Display.getDisplay(this);

			// Главная менюшка
			mainMenu = new MainMenu();
			mainMenu.setCommandListener(this);

			// Грузим штуку для хранения плейлистов в RecordStore
			listStore = new PlayListStore();

			// Грузим плейлист по умолчанию
			list = new PlayList(this, null);

			// Грузим менюшку для этого плейлиста
			plMenu = new PlayListMenu(this);

			// Менюшка перемещения песен в плейлисте
			plmMenu = new PlayListMoveMenu(this);

			// Грузим менюшку для списка плейлистов
			plsMenu = new PlayListsMenu(this);

			// Грузим файловый менеджер
			fileManager = new FileManager(this);

			// Грузим плеер
			player = new MyPlayer(this);

			// Грузим GUI для плеера
			pCanvas = new PlayerCanvas(this);
		}
		display.setCurrent(mainMenu);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
		// save something
		if(list!=null) {
			list.name = null;
			list.save();
		}
		notifyDestroyed();
    }

	public void commandAction(Command cmd, Displayable d) {
		if(cmd == MainMenu.SELECT_COMMAND) {
			int item = mainMenu.getSelectedItem();
			switch (item) {
				case MainMenu.ITEM_LIST:
					plMenu.show();
					break;
				case MainMenu.ITEM_PLAYER:
					pCanvas.show();
					break;
				case MainMenu.ITEM_LISTS:
					plsMenu.show();
					break;
				case MainMenu.ITEM_MINIMIZE:
					display.setCurrent(null);
					break;
				case MainMenu.ITEM_EXIT:
					destroyApp(true);
			}
		}
	}
}
