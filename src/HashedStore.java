import javax.microedition.rms.*;
import java.util.*;
import java.io.*;

public class HashedStore {
	protected String rmsNames;
	protected String rmsData;
	private Hashtable hash;

	HashedStore(String rNames, String rData) {
		rmsNames = rNames;
		rmsData = rData;
		loadNames();
	}

	private void loadNames() {
		hash = new Hashtable();
		try {
			RecordStore rs = RecordStore.openRecordStore(rmsNames, true);
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

			RecordStore rs = RecordStore.openRecordStore(rmsNames, true);
			rs.addRecord(b, 0, b.length);
			rs.closeRecordStore();

			hash.put(name, new Integer(i2));
		} catch(RecordStoreException e)	{ }
		catch(IOException e) { }
	}

	protected void delete(String name) {
		if(name==null) return;
		Integer i = (Integer)hash.get(name);
		int ind1 = i.intValue();
		int ind2 = -1;
		if(i==null) return;
		try {
			RecordStore rs = RecordStore.openRecordStore(rmsNames, true);
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

			rs = RecordStore.openRecordStore(rmsData, true);
			rs.deleteRecord(ind1);
			rs.closeRecordStore();
		} catch(RecordStoreException e) { }
		loadNames();
	}

	protected byte[] get(String name) {
		if(name==null) return null;
		Integer i = (Integer)hash.get(name);
		if(i==null) return null;
		int id = i.intValue();
		try {
			RecordStore rs = RecordStore.openRecordStore(rmsData, true);
			byte[] b = rs.getRecord(id);
			rs.closeRecordStore();
			return b;
		} catch(RecordStoreException e) {
			return null;
		}
	}

	protected void update(String name, byte[] b) {
		if(name==null) return;
		if(b==null) return;
		Integer ind = (Integer)hash.get(name);
		try {
			RecordStore rs = RecordStore.openRecordStore(rmsData, true);
			if(ind==null) {
				int i2 = rs.addRecord(b, 0, b.length);
				addName(name, i2);
			} else {
				int ii = ind.intValue();
				rs.setRecord(ii, b, 0, b.length);
			}
			rs.closeRecordStore();
		}
		catch(RecordStoreException e) { }
		loadNames();
	}

	protected Vector getNames() {
		Enumeration e = hash.keys();
		Vector r = new Vector();
		while(e.hasMoreElements()) {
			String s = (String)e.nextElement();
			r.addElement(s);
		}
		return r;
	}
}
