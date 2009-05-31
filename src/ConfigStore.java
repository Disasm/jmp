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



	
	public int getInt(String name, int def) {
		DataInputStream is = getIs(name);
		if(is==null) return def;
		try {
			return is.readInt();
		} catch (IOException ex) {
			return def;
		}
	}

	public int getInt(String name) {
		return getInt(name, 0);
	}

	public boolean getBoolean(String name, boolean def) {
		DataInputStream is = getIs(name);
		if(is==null) return def;
		try {
			return is.readBoolean();
		} catch (IOException ex) {
			return def;
		}
	}

	public boolean getBoolean(String name) {
		return getBoolean(name, false);
	}

	public String getString(String name, String def) {
		DataInputStream is = getIs(name);
		if(is==null) return def;
		try {
			return is.readUTF();
		} catch (IOException ex) {
			return def;
		}
	}

	public String getString(String name) {
		return getString(name, null);
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
