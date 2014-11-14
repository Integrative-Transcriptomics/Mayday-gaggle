package mayday.gaggle.senders;

import java.lang.reflect.Method;
import java.util.Calendar;

import mayday.core.DataSet;
import mayday.core.MaydayDefaults;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.Single;
import org.systemsbiology.gaggle.core.datatypes.Tuple;

public class MaydayMetaTuple {

	public static void addMetaTuple(GaggleData gd, DataSet ds) {
		Tuple metaTuple = new Tuple();
		
		// add information for compatibility with MeV
		Single mev_id_type = new Single("identifier-type", "DataMatrix");
		metaTuple.addSingle(mev_id_type);		
		Tuple mevTuple = new Tuple();
		mevTuple.addSingle(new Single("data-type", "intensities"));
		mevTuple.addSingle(new Single("array-name", "NA"));
		metaTuple.addSingle(new Single("MeV-metadata", mevTuple));
		
		Tuple maydayTuple = new Tuple("Mayday");
		maydayTuple.addSingle(new Single("Version: ", MaydayDefaults.RELEASE_MAJOR + "." + MaydayDefaults.RELEASE_MINOR + " " + MaydayDefaults.RELEASE_SUPPLEMENT));
		maydayTuple.addSingle(new Single("Website: ", "http://www.microarray-analysis.org"));
		maydayTuple.addSingle(new Single("Dataset: ", ds.getName()));
		maydayTuple.addSingle(new Single("Data sent: ", Calendar.getInstance().getTime().toString()));
		
		// add information about Mayday
		metaTuple.addSingle(new Single("Sender", maydayTuple));
		
		// why does the interface not contain the common method? Thanks a lot!
		Method m;
		try {
			m = gd.getClass().getDeclaredMethod("setMetadata", Tuple.class);
			m.setAccessible(true);
			m.invoke(gd, metaTuple);		
		} catch (Exception e) {
			// bloody hell
			System.err.println("Oh great. This Gaggle object does not support its own interface, not adding meta information tuple:\n"+gd.getClass());
			e.printStackTrace();
		} 
		
	}
	
}
