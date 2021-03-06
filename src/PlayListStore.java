import javax.microedition.rms.*;
import java.util.*;
import java.io.*;

public class PlayListStore {
	public static final String defaultListName = "По умолчанию";
	private static final String rmsNames = "plnames";
	private static final String rmsData = "pldata";
	private Hashtable hash;
	
	PlayListStore() {
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

	public void delete(String name) {
		if(name==null) name = defaultListName;
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

	public Vector get(String name) {
		if(name==null) name = defaultListName;
		Integer i = (Integer)hash.get(name);
		if(i==null) return new Vector();
		int id = i.intValue();
		try {
			RecordStore rs = RecordStore.openRecordStore(rmsData, true);
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
		if(name==null) name = defaultListName;
		Integer ind = (Integer)hash.get(name);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream os = new DataOutputStream(baos);
			os.writeInt(vals.size());
			for(int i=0;i<vals.size();i++) {
				((PlayListItem)vals.elementAt(i)).save(os);
			}
			byte[] b = baos.toByteArray();
			RecordStore rs = RecordStore.openRecordStore(rmsData, true);
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

	public boolean stringGreater(String s, String then) {
		int l = Math.min(s.length(), then.length());
		for(int i=0;i<l;i++) {
			char c1 = s.charAt(i);
			char c2 = then.charAt(i);
			if(c1>c2) return true;
			if(c1<c2) return false;
		}
		if(s.length()>then.length()) return true;
		else return false;
	}

	public Vector stringSort(Vector v0) {
		if(v0==null) return null;
		if(v0.size()==0) return new Vector();
		Vector r = new Vector();
		String[] ss = new String[v0.size()];
		try {
			for(int i=0;i<v0.size();i++) {
				ss[i] = (String)v0.elementAt(i);
			}
		} catch(Exception e) {
			return null;
		}
		boolean sorted;
		while(true) {
			sorted = true;
			for(int i=0;i<(ss.length-1);i++) {
				if(stringGreater(ss[i],ss[i+1])) {
					String s = ss[i];
					ss[i] = ss[i+1];
					ss[i+1] = s;
					sorted = false;
				}
			}
			if(sorted) break;
		}
		for(int i=0;i<ss.length;i++) {
			r.addElement(ss[i]);
		}
		return r;
	}

	public Vector getNames() {
		boolean hasDefault = false;
		if(hash.get(defaultListName)!=null) {
			hasDefault = true;
		}
		Enumeration e = hash.keys();
		Vector r = new Vector();
		while(e.hasMoreElements()) {
			String s = (String)e.nextElement();
			if(!s.equals(defaultListName)) {
				r.addElement(s);
			}
		}
		r = stringSort(r);
		Vector res = new Vector();
		if(hasDefault) res.addElement(defaultListName);
		for(int i=0;i<r.size();i++) {
			res.addElement(r.elementAt(i));
		}
		return res;
	}
}
