package mayday.gaggle.senders;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import mayday.core.DataSet;
import mayday.core.Mayday;
import mayday.core.Probe;
import mayday.core.meta.MIGroup;
import mayday.core.meta.MIGroupSelection;
import mayday.core.meta.MIManager;
import mayday.core.meta.MIType;
import mayday.core.meta.NumericMIO;
import mayday.core.meta.plugins.AbstractMetaInfoPlugin;
import mayday.core.meta.plugins.MetaInfoPlugin;
import mayday.core.pluma.Constants;
import mayday.core.pluma.PluginInfo;
import mayday.core.pluma.PluginManagerException;
import mayday.core.settings.SettingDialog;
import mayday.core.settings.generic.HierarchicalSetting;
import mayday.core.settings.generic.SelectableHierarchicalSetting;
import mayday.core.settings.typed.BooleanSetting;
import mayday.core.settings.typed.RestrictedStringSetting;
import mayday.gaggle.GagglePlugin;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.GaggleTuple;
import org.systemsbiology.gaggle.core.datatypes.Interaction;
import org.systemsbiology.gaggle.core.datatypes.Namelist;
import org.systemsbiology.gaggle.core.datatypes.Network;
import org.systemsbiology.gaggle.core.datatypes.Single;
import org.systemsbiology.gaggle.core.datatypes.Tuple;

public class BroadcastMetaInfo extends AbstractMetaInfoPlugin {

	@Override
	public void init() {
		pli.getProperties().put(MetaInfoPlugin.MULTISELECT_HANDLING, MetaInfoPlugin.MULTISELECT_HANDLE_INTERNAL);
		registerAcceptableClass(MIType.class);		
	}

	@Override
	public PluginInfo register() throws PluginManagerException {
		pli = new PluginInfo(
				this.getClass(),
				"PAS.gaggle.send.mi",
				new String[0],
				Constants.MC_METAINFO_PROCESS,
				new HashMap<String, Object>(),
				"Claudia Broelemann",
				"broelema@informatik.uni-tuebingen.de",
				"Sends meta information to the Gaggle Boss",
				"Send to Gaggle");	
		pli.setIcon("mayday/gaggle/gaggle.gif");
		return pli;
	}

	@Override
	public void run(final MIGroupSelection<MIType> mgs, final MIManager miManager) {
		
		// connect
		
		GagglePlugin.showGUI();
		if (!GagglePlugin.ensureConnected()) {
			JOptionPane.showMessageDialog(Mayday.sharedInstance, "Could not connect to the Gaggle Boss", 
					"Connection failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// find out what to send, and how
		BooleanSetting list_keys  = new BooleanSetting("Send object names","Send the names of annotated objects",false);
		BooleanSetting list_vals  = new BooleanSetting("Send annotations","Send the annotation values",true);
		HierarchicalSetting LIST = new HierarchicalSetting("List")
		.addSetting(list_keys)
		.addSetting(list_vals);
				
		BooleanSetting matrix_pb_dnames  = new BooleanSetting("Use display names for probes","otherwise, use raw names",false);
		HierarchicalSetting MATRIX = new HierarchicalSetting("Matrix: numerical meta-data attached to probes")
		.addSetting(matrix_pb_dnames);
		
		String CLUSTER = "Cluster: -- not implemented --";
				
		String NETWORK = "Network: Objects as nodes, connected if they have the same annotation";
		
		String TUPLE = "Tuple: Structured data containing the annotation information";
		
		Object[] GTYPES = new Object[]{LIST,MATRIX,CLUSTER,NETWORK,TUPLE};
		
		SelectableHierarchicalSetting gtype = new SelectableHierarchicalSetting("Send as","Choose the Gaggle object type", 0, GTYPES)
		.setLayoutStyle(SelectableHierarchicalSetting.LayoutStyle.COMBOBOX);



		String[] geese = GagglePlugin.getGoose().getGeese();
		String[] geese_and_boss = new String[geese.length+1];
		geese_and_boss[0] = "Boss";
		System.arraycopy(geese, 0, geese_and_boss, 1, geese.length);
		
		RestrictedStringSetting gtgt = new RestrictedStringSetting("Target","Select which Goose should receive the object.\n" +
				"Select \"Boss\" to send to all connected Geese.",0,geese_and_boss);
		
		HierarchicalSetting sett = new HierarchicalSetting("Send ProbeList to Gaggle")
		.addSetting(gtgt).addSetting(gtype);
		
		SettingDialog sd = new SettingDialog(Mayday.sharedInstance, "Send ProbeList to Gaggle", sett);
		sd.showAsInputDialog();
		if (!sd.closedWithOK())
			return;

		Boss boss = GagglePlugin.getBoss();
		DataSet ds = miManager.getDataSet();
		
		try {
			// do the work
			if (gtype.getObjectValue()==LIST) 
				sendAsList(mgs, ds, boss, gtgt.getStringValue(), list_keys.getBooleanValue(), list_vals.getBooleanValue());
			else 
			if (gtype.getObjectValue()==MATRIX) 
				sendAsMatrix(mgs, ds, boss, gtgt.getStringValue(), matrix_pb_dnames.getBooleanValue());
			else
			if (gtype.getObjectValue()==CLUSTER) 
				return;
			else
			if (gtype.getObjectValue()==NETWORK)
				sendAsNetwork(mgs, ds, boss, gtgt.getStringValue());
			else
			if (gtype.getObjectValue()==TUPLE) 
				sendAsTuple(mgs, ds, boss, gtgt.getStringValue());
		} catch (RemoteException re) {
			JOptionPane.showMessageDialog(Mayday.sharedInstance, "Could not transfer data:\n"+re.getMessage(), "Communication failed", JOptionPane.ERROR_MESSAGE);
		}
		
	}

	protected static String createName(MIGroupSelection<MIType> mgs) {
		return mgs.get(0).getName()+((mgs.size()>1)?" and others":"");
	}
	
	protected static void addMetaTuple(GaggleData data, DataSet ds) {
		MaydayMetaTuple.addMetaTuple(data, ds);
	}
	
	public static void sendAsList(MIGroupSelection<MIType> mgs, DataSet ds, Boss boss, String targetGoose,
			boolean useK, boolean useV) throws RemoteException {
		Namelist gaggleNameList = new Namelist();
        gaggleNameList.setName(createName(mgs));
        addMetaTuple(gaggleNameList,ds);
        
        ArrayList<String> names = new ArrayList<String>();

        for (MIGroup mg : mgs) {
			for (Entry<Object, MIType> e : mg.getMIOs()) {
				if (useK)
					names.add(e.getKey().toString());
				if (useV)
					names.add(e.getValue().toString());
			}
		}        
        
        gaggleNameList.setNames(names.toArray(new String[0]));        
		
		boss.broadcastNamelist(GagglePlugin.getGoose().getAssignedName(), targetGoose, gaggleNameList);
	}
	
	@SuppressWarnings("unchecked")
	public static void sendAsMatrix(MIGroupSelection<MIType> mgs, DataSet ds, Boss boss, String targetGoose,
			boolean usePD) throws RemoteException {
		DataMatrix gaggleMatrix = new DataMatrix();
		gaggleMatrix.setName(createName(mgs));
        addMetaTuple(gaggleMatrix,ds);
        
        mgs = ((MIGroupSelection)mgs).filterByInterface(NumericMIO.class);

        // collect all probes
        HashSet<Probe> probes = new HashSet<Probe>();
        for (MIGroup mg : mgs) {
        	for (Object o :mg.getObjects() )
        		if (o instanceof Probe)
        			probes.add((Probe)o);
        }
        
        gaggleMatrix.setSize(probes.size(), mgs.size());
        String[] colNames = new String[mgs.size()];
        for (int i=0; i!=mgs.size(); ++i)
        	colNames[i] = mgs.get(i).getPath()+"/"+mgs.get(i).getName();
        String[] rowNames = new String[probes.size()];
        			
        // collect all values
        int row=0;
        for (Probe pb : probes) {
        	rowNames[row] = usePD?pb.getDisplayName():pb.getName();
        	int col=0;
        	for (MIGroup mg : mgs) {
        		MIType mt = mg.getMIO(pb);
        		if (mt!=null) {
        			gaggleMatrix.set(row, col, ((Number)((NumericMIO)mt).getValue()).doubleValue());
        		}
        		++col;
        	}
        	++row;
        }

        gaggleMatrix.setFullName(mgs.toString());
        gaggleMatrix.setColumnTitles(colNames);
        gaggleMatrix.setRowTitles(rowNames);
        
		boss.broadcastMatrix(GagglePlugin.getGoose().getAssignedName(), targetGoose, gaggleMatrix);
	}
	
	public static void sendAsNetwork(MIGroupSelection<MIType> mgs, DataSet ds, Boss boss, String targetGoose) throws RemoteException {
		Network gaggleNetwork = new Network();
		gaggleNetwork.setName(createName(mgs));
		addMetaTuple(gaggleNetwork,ds);
		
		// add all objects as nodes
        HashSet<Object> obj = new HashSet<Object>();
        for (MIGroup mg : mgs) {
        	for (Object o :mg.getObjects() )
       			obj.add(o);
        }
        for (Object o : obj) {
        	gaggleNetwork.add(o.toString()); //they are hopefully uniquely named
        }
        
		// add interactions from probelists - this is O(n^2)		
        for (MIGroup mg : mgs) {
        	String interName = mg.getPath()+"/"+mg.getName();
        	List<Object> lo = new ArrayList<Object>(mg.getObjects());        	
        	for (int i=0; i!=lo.size(); ++i) {
        		Object o1 = lo.get(i);
    			MIType mt1 = mg.getMIO(o1);
    			if (mt1==null)
    				continue;
    			String on1 = o1.toString();
        		for (int j=i+1; j!=lo.size(); ++j) {
            		Object o2 = lo.get(j);
        			String on2 = o2.toString();			
        			MIType mt2 = mg.getMIO(o1);
        			if (mt2!=null && mt1.equals(mt2))
        				gaggleNetwork.add(new Interaction(on1, on2, interName));
        		}
        	}
        }
		
		boss.broadcastNetwork(GagglePlugin.getGoose().getAssignedName(), targetGoose, gaggleNetwork);
		
	}
	
	protected static void sendAsTuple(MIGroupSelection<MIType> mgs,  DataSet ds, Boss boss, String targetGoose) throws RemoteException {
		GaggleTuple gaggleTuple = new GaggleTuple();
        gaggleTuple.setName(createName(mgs));
        addMetaTuple(gaggleTuple, ds);
        
        // send migroup as tuple of singles mapping object name to value
        
		Tuple mgTuple = new Tuple("MIGroups");
    	for (MIGroup mg : mgs){    		
    		Tuple mgContent = new Tuple();
    		for (Entry<Object, MIType> e : mg.getMIOs()) {
    			mgContent.addSingle(new Single(e.getKey().toString(), e.getValue().toString()));
    		}
    		Single mgEntry = new Single(mg.getPath()+"/"+mg.getName(), mgContent);
    		mgTuple.addSingle(mgEntry);
    	}
    	
    	gaggleTuple.setData(mgTuple);

    	boss.broadcastTuple(GagglePlugin.getGoose().getAssignedName(), targetGoose, gaggleTuple);
	}


}