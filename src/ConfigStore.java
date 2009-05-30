import java.util.*;
import java.io.*;

public class ConfigStore extends HashedStore {
	public ConfigStore() {
		super("confnames", "confdata");
	}

	private DataInputStream getIs(String name) {
		byte[] b = get(name);
		if(b==null) return null;
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		DataInputStream is = new DataInputStream(bais);
		return is;
	}



	
	public int getInt(String name) {
		DataInputStream is = getIs(name);
		if(is==null) return 0;
		try {
			return is.readInt();
		} catch (IOException ex) {
			return 0;
		}
	}

	public boolean getBoolean(String name) {
		DataInputStream is = getIs(name);
		if(is==null) return false;
		try {
			return is.readBoolean();
		} catch (IOException ex) {
			return false;
		}
	}

	public String getString(String name) {
		DataInputStream is = getIs(name);
		if(is==null) return null;
		try {
			return is.readUTF();
		} catch (IOException ex) {
			return null;
		}
	}




	public void setInt(String name, int v) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(baos);
		try {
			os.writeInt(v);
		} catch (IOException ex) {
			return;
		}
		byte[] b = baos.toByteArray();
		update(name, b);
	}

	public void setBoolean(String name, boolean v) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(baos);
		try {
			os.writeBoolean(v);
		} catch (IOException ex) {
			return;
		}
		byte[] b = baos.toByteArray();
		update(name, b);
	}

	public void setString(String name, String v) {
		if(v==null) {
			delete(name);
			return;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(baos);
		try {
			os.writeUTF(v);
		} catch (IOException ex) {
			return;
		}
		byte[] b = baos.toByteArray();
		update(name, b);
	}
}
