import javax.microedition.rms.*;
import java.util.*;
import java.io.*;

public class PlayListStore {
	private Hashtable hash;
	
	PlayListStore() {
		loadNames();
	}

	private void loadNames() {
		hash = new Hashtable();
		try {
			RecordStore rs = RecordStore.openRecordStore("plnames", true);
			RecordEnumeration re = rs.enumerateRecords(null, null, false);
			while(re.hasNextElement()) {
				int id = re.nextRecordId();
				byte[] b = rs.getRecord(id);
				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(b);
					DataInputStream is = new DataInputStream(bais);
					String s = is.readUTF();
					int ind = is.readInt();
					hash.put(s, new Integer(ind));
				} catch(IOException e) { }
			}
			re.destroy();
			rs.closeRecordStore();
		}
		catch(RecordStoreException e) { }
	}

	private void addName(String name, int i2) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream os = new DataOutputStream(baos);
			os.writeUTF(name);
			os.writeInt(i2);
			byte[] b = baos.toByteArray();

			RecordStore rs = RecordStore.openRecordStore("plnames", true);
			rs.addRecord(b, 0, b.length);
			rs.closeRecordStore();

			hash.put(name, new Integer(i2));
		} catch(RecordStoreException e)	{ }
		catch(IOException e) { }
	}

	public void delete(String name) {
		if(name==null) name = "<default>";
		Integer i = (Integer)hash.get(name);
		int ind1 = i.intValue();
		int ind2 = -1;
		if(i==null) return;
		try {
			System.out.println("=delete");
			RecordStore rs = RecordStore.openRecordStore("plnames", true);
			RecordEnumeration re = rs.enumerateRecords(null, null, false);
			while(re.hasNextElement()) {
				int id = re.nextRecordId();
				byte[] b = rs.getRecord(id);
				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(b);
					DataInputStream is = new DataInputStream(bais);
					String s = is.readUTF();
					if(s.equals(name)) {
						ind2 = id;
						break;
					}
				} catch(IOException e) { }
			}
			re.destroy();

			if(ind2!=-1) rs.deleteRecord(ind2);

			rs.closeRecordStore();

			rs = RecordStore.openRecordStore("pldata", true);
			rs.deleteRecord(ind1);
			rs.closeRecordStore();
		} catch(RecordStoreException e) { }
		loadNames();
	}

	public Vector get(String name) {
		if(name==null) name = "<default>";
		Integer i = (Integer)hash.get(name);
		if(i==null) return new Vector();
		int id = i.intValue();
		try {
			RecordStore rs = RecordStore.openRecordStore("pldata", true);
			byte[] b = rs.getRecord(id);
			rs.closeRecordStore();
			Vector v = new Vector();
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(b);
				DataInputStream is = new DataInputStream(bais);
				int size = is.readInt();
				while(is.available()>0) {
					PlayListItem item = new PlayListItem(is);
					v.addElement(item);
				}
			} catch(IOException e) { }
			return v;
		} catch(RecordStoreException e) {
			return new Vector();
		}
	}

	public void update(String name, Vector vals) {
		if(name==null) name = "<default>";
		Integer ind = (Integer)hash.get(name);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream os = new DataOutputStream(baos);
			os.writeInt(vals.size());
			for(int i=0;i<vals.size();i++) {
				((PlayListItem)vals.elementAt(i)).save(os);
			}
			byte[] b = baos.toByteArray();
			RecordStore rs = RecordStore.openRecordStore("pldata", true);
			if(ind==null) {
				int i2 = rs.addRecord(b, 0, b.length);
				addName(name, i2);
			} else {
				int ii = ind.intValue();
				rs.setRecord(ii, b, 0, b.length);
			}
			rs.closeRecordStore();
		} catch(IOException e) { }
		catch(RecordStoreException e) { }
		loadNames();
	}

	public Vector getNames() {
		Vector r = new Vector();
		Enumeration e = hash.keys();
		if(hash.get("<default>")!=null) {
			r.addElement("<default>");
		}
		while(e.hasMoreElements()) {
			String s = (String)e.nextElement();
			if(!s.equals("<default>")) {
				r.addElement(s);
			}
		}
		return r;
	}
}
