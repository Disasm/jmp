import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import java.util.*;
import java.io.*;

class Tag {
	public String artist, title, album;
}

public class TagReader {
	public String artist, title, album;

	public TagReader(String path) {
		//String url = getUrl(path);
		FileConnection fc = null;
		InputStream is = null;
		try {
			//fc = (FileConnection)Connector.open(url, Connector.READ);
			fc = (FileConnection)Connector.open(path, Connector.READ);
			is = fc.openInputStream();
			long size = fc.fileSize();

			//Tag tag1 = null;
			Tag tag2 = null;

			byte[] b = new byte[10];
			int res = is.read(b);
			if(res!=10) throw new Exception();
			if((b[0]=='I')&&(b[1]=='D')&&(b[2]=='3')) {
				tag2 = tryId3v2(is, b);
			}

			/*if(tag2==null) {
				tag1 = tryId3v1(is, size);
			}*/

			if(tag2!=null) {
				title = tag2.title;
				artist = tag2.artist;
				album = tag2.album;
			}/* else if(tag1!=null) {
				title = tag1.title;
				artist = tag1.artist;
				album = tag1.album;
			}*/
		} catch(Exception e) {
			/*Alert a = new Alert("Ошибка", e.getMessage(), null, AlertType.INFO);
			//Alert a = new Alert("Ошибка", e.getMessage()+" (url="+url+")", null, AlertType.INFO);
			a.setTimeout(Alert.FOREVER);
			Jmp.midlet.display.setCurrent(a, Jmp.midlet.display.getCurrent());*/
		}
		
		try {
			is.close();
		} catch(Exception e) {}
		try {
			fc.close();
		} catch(Exception e) {}
	}

	private Tag tryId3v1(InputStream is, long size) throws IOException {
		if(!is.markSupported()) return null;
		is.reset();
		is.skip(size-128);
		return null;
	}

	private Tag tryId3v2(InputStream is, byte[] f10) throws IOException {
		byte vMajor = f10[3];
		byte vMinor = f10[4];
		byte flags = f10[5];
		int size = extractSize(f10, 6);
		if(size==0) return null;
		byte[] b = new byte[size];
		is.read(b);

		if(vMajor==3) return tryId3v2_3(is, flags, b);
		return null;
	}

	private Tag tryId3v2_3(InputStream is, byte flags, byte[] b) {
		boolean extended = ((((int)flags)&0x40) > 0);
		int off = 0;
		if(extended) {
			int size = ((int)b[off + 3])&0xff;
			off += 4 + size;
		}
		Tag t = new Tag();
		while(off<b.length) {
			if((b.length-off)<=10) break;
			if(b[off]==0) break;
			String fid = b2c(b[off]) + "" + b2c(b[off+1]) + "" + b2c(b[off+2]) + "" + b2c(b[off+3]);
			int size = extractSize(b, off+4, 8);

			//System.out.println("FID: '"+fid+"'");
			if(fid.equals("TIT2")) {
				t.title = extractString(b, off+10, size);
			} else if((fid.equals("TPUB"))||(fid.equals("TPE1"))||(fid.equals("TPE2"))||(fid.equals("TPE3"))) {
				if(t.artist==null) t.artist = extractString(b, off+10, size);
			} else if(fid.equals("TALB")) {
				t.album = extractString(b, off+10, size);
			}
			
			off += 10+size;
		}
		return t;
	}

	private char b2c(byte b) {
		int b2 = (((int)b)&0xff);
		return (char)b2;
	}

	private int extractSize(byte[] bf, int off, int pad) {
		int b3 = (((int)bf[off + 3])&0xff);
		int b2 = (((int)bf[off + 2])&0xff);
		int b1 = (((int)bf[off + 1])&0xff);
		int b0 = (((int)bf[off])&0xff);
		return b3 + (b2<<pad) + (b1<<(pad*2)) + (b0<<(pad*3));
	}

	private int extractSize(byte[] bf, int off) {
		return extractSize(bf, off, 7);
	}

	private String extractString(byte[] bf, int off, int len) {
		if(len<2) return null;
		byte enc = bf[off];
		off++;
		len--;
		if(enc==0x00) {
			return extractLocalString(bf, off, len);
		} else if(enc==0x01) {
			if(len<4) return null;
			int b0 = (((int)bf[off])&0xff);
			int b1 = (((int)bf[off + 1])&0xff);
			if((b0==0xff)&&(b1==0xfe)) {
				return extractUnicodeString(bf, off+2, (len-2)/2, true);
			} else if((b0==0xfe)&&(b1==0xff)) {
				return extractUnicodeString(bf, off+2, (len-2)/2, false);
			}
		}
		return null;
	}

	private String extractUnicodeString(byte[] bf, int off, int len, boolean intel) {
		String s = "";
		for(int i=0;i<len;i++) {
			char c;
			int b0 = (((int)bf[i*2+off])&0xff);
			int b1 = (((int)bf[i*2+off+1])&0xff);
			if(intel) {
				c = (char)(b0 + (b1<<8));
			} else {
				c = (char)(b1 + (b0<<8));
			}
			if(c==0) break;
			s = s + c;
		}
		//System.out.println("extractUnicodeString: '"+s+"'");
		return s;
	}

	private String extractLocalString(byte[] bf, int off, int len) {
		String s = "";
		for(int i=0;i<len;i++) {
			byte b = bf[off+i];
			if(b==0) break;
			s = s + getCp1251(b);
		}
		//System.out.println("extractLocalString: '"+s+"'");
		return s;
	}

	private char getCp1251(byte b0) {
		int b = ((int)b0)&0xff;
		if(b<0x80) return (char)b;
		if(b==0xb8) return (char)0x451;
		if(b==0xa8) return (char)0x401;
		if((b>=0xc0)&&(b<=0xff)) return (char)(0x410+b-0xc0);
		return '?';
	}

	private String a2hex(int a) {
		a = a & 0xff;
		String s1 = Integer.toHexString(a);
		while(s1.length()<2) s1 = "0" + s1;
		return "%"+s1;
	}

	private String urlEncode(int c) {
		//int a = ((int)c)&0xffff;
		if(((c>='A')&&(c<='Z'))||
		   ((c>='a')&&(c<='z'))||
		   ((c>='0')&&(c<='9'))||
		   (c=='/')||(c==':')||
		   (c=='.')||(c=='-')||
		   (c=='(')||(c==')')||
		   (c==' ')) return (char)c+"";
		if((c&0xff00)==0) {
			return a2hex(c&0xff);
		} else {
			return a2hex((c&0xff00)>>8)+a2hex(c&0xff);
		}
	}

	// getUrl: 'file:///root1/Łzy - Gdybyś był (wersja karaoke).mp3' =>
	// 'file:///root%25%33%31/%25%38a%25%38azy%25%320-%25%320Gdyby%250%36%25%32e%25%320by%25%31%34%25%31%34%25%320(wersja%25%320karaoke).mp%25%33%33'
	// getUrl: 'file:///root1/Łzy - Gdybyś był (wersja karaoke).mp3' =>
	// 'file:///root%25%33%31/%25%38b%25%38bzy%25%320-%25%320Gdyby%250%36%25%32e%25%320by%25%31%37%25%31%37%25%320(wersja%25%320karaoke).mp%25%33%33'
	// buf: 66 69 6c 65 3a 2f 2f 2f 72 6f 6f 74 31 2f ffffffc5 ffffff81 7a 79 20 2d 20 47 64 79 62 79 ffffffc5 ffffff9b 20 62 79 ffffffc5 ffffff82 20 28 77 65 72 73 6a 61 20 6b 61 72 61 6f 6b 65 29 2e 6d 70 33

	/*
		file:///e:/MP3/card/Fleur/01%20-%20%d0%00%06%86%03%43%34%34%68%68%d1%d1%e8%00.mp3
	 * file:///root1/             01%20-%20 %d0%00 %06%86 %03%43 %34%34 %68%68 %d1%d1 %e8%00.mp3
	 *  01 - Шелкопряд.mp3
	 */

	private String getUrl(String s0) {
		String s1 = "";
		try {
			byte[] bf = s0.getBytes("UTF-8");
			//
			/*System.out.print("  buf: ");
			for(int i=0;i<bf.length;i++) {
				System.out.print(Integer.toHexString(((int)bf[i])&0xff)+" ");
			}
			System.out.println();*/
			//
			int i = 0;
			while(i<bf.length) {
				int b = ((int)bf[i])&0xff;
				//System.out.print(" => "+(Integer.toHexString(b)));
				if(b<0x80) {
					String s = urlEncode(b);
					//System.out.println(" '"+s+"'");
					s1 = s1 + s;
					i++;
				} else {
					if((i+1)>=bf.length) break;
					int b2 = ((int)bf[i+1])&0xff;
					//System.out.print(" (b2="+Integer.toHexString(b2)+")");
					//System.out.print(" (b="+Integer.toHexString(b)+")");
					int b3 = (b<<8) + b2;
					String s = urlEncode(b3);
					//System.out.println(" (b3="+Integer.toHexString(b3)+") '"+s+"'");
					s1 = s1 + s;
					i += 2;
				}
			}
			//System.out.println("getUrl: '"+s0+"' => '"+s1+"'");
			return s1;
		} catch(Exception e) {
			s1 = s0;
		}

		String s = "";
		for(int i=0;i<s1.length();i++) {
			int c = ((int)s1.charAt(i))&0xffff;
			s = s + urlEncode(c);
		}
		//System.out.println("getUrl: '"+s0+"' => '"+s+"'");
		return s;
	}
}


/*
 * Invalid file name in FileConnection Url: ///e:/MP3/card/Fleur/01 - Как все уходит.mp3
 * File does not exists
 */
