package mayday.gaggle.incoming;

import mayday.core.DataSet;
import mayday.core.MasterTable;
import mayday.core.Probe;
import mayday.core.ProbeList;
import mayday.core.meta.MIGroup;
import mayday.core.meta.MIManager;
import mayday.core.meta.types.IntegerMIO;

import org.systemsbiology.gaggle.core.datatypes.DataMatrix;

public class MatrixToDataset {
	
	public static DataSet convert(DataMatrix matrix, String sender) {
		DataSet ds = new DataSet(matrix.getName());
		ds.getAnnotation().setQuickInfo("Received from "+sender+" via Gaggle");
		MasterTable mt = new MasterTable(ds);
		mt.setNumberOfExperiments(matrix.getColumnCount());
		for (int i = 0; i != mt.getNumberOfExperiments(); ++i) {
			mt.setExperimentName(i, matrix.getColumnTitles()[i]);
		}
		for (String s : matrix.getRowTitles()) {
			Probe pb = new Probe(mt);
			for (Double ex : matrix.get(s)) {
				pb.addExperiment(ex);
			}
			pb.setName(s);
			mt.addProbe(pb);
		}
		ds.setMasterTable(mt);
		ProbeList gpl = mt.createGlobalProbeList(true);
		ds.getProbeListManager().addObject(gpl);

		ds.getAnnotation().setInfo(
			"Full name: "+matrix.getFullName()+"\n" +
			"Short name :"+matrix.getShortName()+"\n" +
			"Type: "+matrix.getDataTypeBriefName() +"\n"+
			"Species: "+matrix.getSpecies()
		);
		
		// add original row order
		MIManager mi = ds.getMIManager();
		MIGroup mioClusterGroup = mi.newGroup("PAS.MIO.Integer", "Original row order");
		for (int i = 0; i < matrix.getRowTitles().length; i++) {
			String s = matrix.getRowTitles()[i];
			Probe pb = mt.getProbe(s);
			if (pb != null) {
				((IntegerMIO)mioClusterGroup.add(pb)).setValue(i);
			}
		}
		
		return ds;
	}

}
