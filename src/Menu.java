import javax.microedition.lcdui.*;
import java.util.*;

public class Menu extends List {
	public Display display;

	Menu(Display disp, String name, int choice) {
		super(name, choice);
		display = disp;
	}

	private Stack oldDisp = new Stack();

	protected void close() {
		display.setCurrent((Displayable)oldDisp.pop());
	}

	public void show() {
		oldDisp.push(display.getCurrent());
		display.setCurrent(this);
	}

	public Displayable parent() {
		if(oldDisp.empty()) return null;
		return (Displayable)oldDisp.peek();
	}
}
