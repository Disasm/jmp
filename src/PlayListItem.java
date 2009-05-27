import java.io.*;

public class PlayListItem {
	public String name;
	public String path;
	public String title;
	public String artist;
	public String album;
	
	PlayListItem(String p) {
		path = p;
		TagReader tr = new TagReader(path);
		title = tr.title;
		artist = tr.artist;
		album = tr.album;
		if(title!=null) {
			name = title;
		} else {
			int i = path.lastIndexOf('/');
			name = path.substring(i+1);
		}
	}
	
	PlayListItem(DataInputStream s) throws IOException {
		path = s.readUTF();
		name = s.readUTF();
		title = s.readUTF();
		if(title.length()==0) title = null;
		artist = s.readUTF();
		if(artist.length()==0) artist = null;
		album = s.readUTF();
		if(album.length()==0) album = null;
	}

	public void save(DataOutputStream s) throws IOException {
		s.writeUTF(path);
		s.writeUTF(name);
		s.writeUTF((title!=null)?title:"");
		s.writeUTF((artist!=null)?artist:"");
		s.writeUTF((album!=null)?album:"");
	}
}
