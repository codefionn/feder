import java.io.*;

public class Read_readme {
	public static final void main (String[] args) {
		try (FileInputStream fis = new FileInputStream (new File ("jfederc"))) {
			int n;
			while ((n = fis.read ()) != -1) {
				System.out.print ((char) n);
			}
		} catch (IOException ex) {
			ex.printStackTrace ();
			System.exit (1);
		}
	}
}
