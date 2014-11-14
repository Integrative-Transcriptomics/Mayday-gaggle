package mayday.gaggle;

import java.util.ArrayList;
import java.util.List;

import mayday.core.DataSet;
import mayday.gaggle.incoming.MatrixToDataset;
import mayday.gaggle.incoming.NetworkToDataset;

import org.systemsbiology.gaggle.core.datatypes.Cluster;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.GaggleTuple;
import org.systemsbiology.gaggle.core.datatypes.Namelist;
import org.systemsbiology.gaggle.core.datatypes.Network;
import org.systemsbiology.gaggle.core.datatypes.Single;
import org.systemsbiology.gaggle.core.datatypes.Tuple;

public enum GaggleType {

	Tuple, Network, Cluster, List, Matrix, UnknownType;
	
	public static GaggleType typeOf(GaggleData data) {
		if (data instanceof GaggleTuple)
			return GaggleType.Tuple;
		if (data instanceof Network) 
			return GaggleType.Network;
		if (data instanceof Cluster)
			return GaggleType.Cluster;
		if (data instanceof Namelist)
			return GaggleType.List;
		if (data instanceof DataMatrix)
			return GaggleType.Matrix;
		return 
			GaggleType.UnknownType;
	}

	public static List<String> extractStrings(GaggleData data) {
		ArrayList<String>  strings = new ArrayList<String>();

		switch(GaggleType.typeOf(data)) {
		case List: 
			for (String q : ((Namelist)data).getNames())
				strings.add(q);
			break;
		case Cluster:
			for (String q : ((Cluster)data).getColumnNames())
				strings.add(q);
			for (String q : ((Cluster)data).getRowNames())
				strings.add(q);
			break;
		case Matrix:
			for (String q : ((DataMatrix)data).getColumnTitles())
				strings.add(q);
			for (String q : ((DataMatrix)data).getRowTitles())
				strings.add(q);
			break;		
		case Network:
			for (String q : ((Network)data).getNodes())
				strings.add(q);
			for (String a : ((Network)data).getNodeAttributeNames())
				for (Object q : ((Network)data).getNodeAttributes(a).values())
					strings.add(q.toString());
			break;
		case Tuple:			
			extractFromTuple(((GaggleTuple)data).getData(), strings);
			break;
		}	
		return strings;
	}
	
	public static void extractFromTuple(Tuple data, List<String> target) {
		for (Single s : data.getSingleList()){
			target.add(s.getName());
			if (s.getValue() instanceof Tuple)
				extractFromTuple((Tuple)s.getValue(), target);
			else
				target.add(s.getValue().toString());
		}		
	}
	
	public static DataSet asDataSet(GaggleData data, String sender) {
		
		DataSet ds = null;
		
		switch (GaggleType.typeOf(data)) {
		case Matrix:
			DataMatrix matrix = (DataMatrix)data;
			ds = MatrixToDataset.convert(matrix, sender);			
			break;
		case Network:
			Network network = (Network)data;
			ds = NetworkToDataset.convert(network, sender);
		}
		return ds;
	}
	
	

	
}
