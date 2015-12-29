package mayday.gaggle.senders;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import mayday.core.DataSet;
import mayday.core.MasterTable;
import mayday.core.Mayday;
import mayday.core.Probe;
import mayday.core.ProbeList;
import mayday.core.meta.MIGroup;
import mayday.core.meta.MIType;
import mayday.core.pluma.AbstractPlugin;
import mayday.core.pluma.Constants;
import mayday.core.pluma.PluginInfo;
import mayday.core.pluma.PluginManagerException;
import mayday.core.pluma.prototypes.ProbelistPlugin;
import mayday.core.settings.SettingDialog;
import mayday.core.settings.generic.BooleanHierarchicalSetting;
import mayday.core.settings.generic.HierarchicalSetting;
import mayday.core.settings.generic.SelectableHierarchicalSetting;
import mayday.core.settings.typed.BooleanSetting;
import mayday.core.settings.typed.MIGroupSetting;
import mayday.core.settings.typed.RestrictedStringSetting;
import mayday.gaggle.GagglePlugin;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.datatypes.Cluster;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.GaggleTuple;
import org.systemsbiology.gaggle.core.datatypes.Interaction;
import org.systemsbiology.gaggle.core.datatypes.Namelist;
import org.systemsbiology.gaggle.core.datatypes.Network;
import org.systemsbiology.gaggle.core.datatypes.Single;
import org.systemsbiology.gaggle.core.datatypes.Tuple;

public class BroadcastProbeList extends AbstractPlugin implements ProbelistPlugin{

	public PluginInfo register() throws PluginManagerException {
		PluginInfo pli = new PluginInfo(
				this.getClass(),
				"PAS.gaggle.send.pl",
				new String[0],
				Constants.MC_PROBELIST,
				new HashMap<String, Object>(),
				"Claudia Broelemann",
				"broelema@informatik.uni-tuebingen.de",
				"Sends probelists to the Gaggle Boss",
				"Send to Gaggle");	
		pli.setIcon("mayday/gaggle/gaggle.gif");

		return pli;
	}


	@Override
	public void init() {
	}

	@Override
	public List<ProbeList> run(List<ProbeList> probeLists, MasterTable masterTable) {
		
		GagglePlugin.showGUI();
		if (!GagglePlugin.ensureConnected()) {
//			JOptionPane.showMessageDialog(Mayday.sharedInstance, "Could not connect to the Gaggle Boss", 
//					"Connection failed", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		
		// find out what to send, and how
		BooleanSetting list_pb_names  = new BooleanSetting("Send probe names",null,true);
		BooleanSetting list_pb_dnames  = new BooleanSetting("Send probe display names",null,false);
		MIGroupSetting list_migroup = new MIGroupSetting("Source",null,null,masterTable.getDataSet().getMIManager(),false);		
		BooleanHierarchicalSetting list_pb_annot  = new BooleanHierarchicalSetting("Send probe annotation",null,false)
		.addSetting(list_migroup);
		HierarchicalSetting LIST = new HierarchicalSetting("List: probe (display) names, display names or meta information")
		.addSetting(list_pb_names)
		.addSetting(list_pb_dnames)
		.addSetting(list_pb_annot);
		
		
		BooleanSetting matrix_pb_dnames  = new BooleanSetting("Use display names for probes","otherwise, use raw names",false);
		BooleanSetting matrix_ex_dnames  = new BooleanSetting("Use display names for experiments","otherwise, use raw names",false);
		HierarchicalSetting MATRIX = new HierarchicalSetting("Matrix: expression values with probe and experiment (display) names")
		.addSetting(matrix_pb_dnames)
		.addSetting(matrix_ex_dnames);
		

		BooleanSetting cluster_pb_dnames = new BooleanSetting("Use display names for probes","otherwise, use raw names",false);
		BooleanSetting cluster_ex_dnames = new BooleanSetting("Use display names for experiments","otherwise, use raw names",false);		
		HierarchicalSetting CLUSTER = new HierarchicalSetting("Cluster: probe and experiment (display) names")
		.addSetting(cluster_pb_dnames)
		.addSetting(cluster_ex_dnames);
				
		BooleanSetting network_pb_dnames = new BooleanSetting("Use display names for probes","otherwise, use raw names",false);
		BooleanSetting network_exdata = new BooleanSetting("Include expression values","otherwise, only sent topology",true);
		BooleanSetting network_interact = new BooleanSetting("ProbeLists as connected components","otherwise, only sent unconnected nodes",true);
		HierarchicalSetting NETWORK = new HierarchicalSetting("Network: Probes as nodes, probelists as connected components")
		.addSetting(network_pb_dnames)
		.addSetting(network_exdata)
		.addSetting(network_interact);
		
		String TUPLE = "Tuple: Structured data containing all available information";
		
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
			return null;

		Boss boss = GagglePlugin.getBoss();
		DataSet ds = masterTable.getDataSet();
		
		try {
			// do the work
			if (gtype.getObjectValue()==LIST) 
				sendAsList(probeLists, ds, boss, gtgt.getStringValue(), list_pb_names.getBooleanValue(), list_pb_dnames.getBooleanValue(), 
						list_pb_annot.getBooleanValue()?list_migroup.getMIGroup():null);
			else 
			if (gtype.getObjectValue()==MATRIX) 
				sendAsMatrix(probeLists, ds, boss, gtgt.getStringValue(), matrix_pb_dnames.getBooleanValue(), matrix_ex_dnames.getBooleanValue());
			else
			if (gtype.getObjectValue()==CLUSTER) 
				sendAsCluster(probeLists, ds, boss, gtgt.getStringValue(), cluster_pb_dnames.getBooleanValue(), cluster_ex_dnames.getBooleanValue());
			else
			if (gtype.getObjectValue()==NETWORK)
				sendAsNetwork(probeLists, ds, boss, gtgt.getStringValue(), network_pb_dnames.getBooleanValue(), network_exdata.getBooleanValue(), network_interact.getBooleanValue());
			else
			if (gtype.getObjectValue()==TUPLE) 
				sendAsTuple(probeLists, ds, boss, gtgt.getStringValue());
		} catch (RemoteException re) {
			JOptionPane.showMessageDialog(Mayday.sharedInstance, "Could not transfer data:\n"+re.getMessage(), "Communication failed", JOptionPane.ERROR_MESSAGE);
		}
		
		return null;
	}

	protected static String createName(List<ProbeList> probelists) {
		return probelists.get(0).getName()+((probelists.size()>1)?" and others":"");
	}
	
	protected static void addMetaTuple(GaggleData data, DataSet ds) {
		MaydayMetaTuple.addMetaTuple(data, ds);
	}
	
	public static void sendAsList(List<ProbeList> probelists, DataSet ds, Boss boss, String targetGoose,
			boolean useN, boolean useD, MIGroup useMI) throws RemoteException {
		Namelist gaggleNameList = new Namelist();
        gaggleNameList.setName(createName(probelists));
        addMetaTuple(gaggleNameList,ds);
        
        ProbeList all = ProbeList.createUniqueProbeList(probelists);
        
        ArrayList<String> names = new ArrayList<String>();
        for (Probe pb : all) {
        	if (useN)
        		names.add(pb.getName());
        	if (useD)
        		names.add(pb.getDisplayName());
        	if (useMI!=null) {
        		MIType mt = useMI.getMIO(pb);
        		if (mt!=null)
        			names.add(mt.toString());
        	}
        }
        
        gaggleNameList.setNames(names.toArray(new String[0]));
        
		
		boss.broadcastNamelist(GagglePlugin.getGoose().getAssignedName(), targetGoose, gaggleNameList);
	}
	
	public static void sendAsMatrix(List<ProbeList> probelists, DataSet ds, Boss boss, String targetGoose,
			boolean usePD, boolean useED) throws RemoteException {
		DataMatrix gaggleMatrix = new DataMatrix();
		gaggleMatrix.setName(createName(probelists));
        addMetaTuple(gaggleMatrix,ds);
        
        ProbeList all = ProbeList.createUniqueProbeList(probelists);
        
        gaggleMatrix.setFullName(probelists.toString());
        
        MasterTable mata = ds.getMasterTable();
        gaggleMatrix.setSize(all.getNumberOfProbes(), mata.getNumberOfExperiments());
        String[] colNames = useED?mata.getExperimentDisplayNames().toArray(new String[0]):mata.getExperimentNames().toArray(new String[0]);
        String[] rowNames = new String[all.getNumberOfProbes()];
        int i=0;
        for (Probe pb : all)
        	rowNames[i++] = usePD?pb.getDisplayName():pb.getName();
        	       
        gaggleMatrix.setColumnTitles(colNames);
        gaggleMatrix.setRowTitles(rowNames);
        
        int row=0;
        for (Probe pb : all) {
        	for (int col=0; col!=pb.getValues().length; ++col)
        		gaggleMatrix.set(row, col, pb.getValues()[col]);
        	++row;
        }
	
		boss.broadcastMatrix(GagglePlugin.getGoose().getAssignedName(), targetGoose, gaggleMatrix);
	}
	
	public static void sendAsCluster(List<ProbeList> probelists, DataSet ds, Boss boss, String targetGoose,
			boolean usePD, boolean useED) throws RemoteException {
		Cluster gaggleCluster = new Cluster();
		gaggleCluster.setName(createName(probelists));
        addMetaTuple(gaggleCluster,ds);
        
        ProbeList all = ProbeList.createUniqueProbeList(probelists);
        
        MasterTable mata = ds.getMasterTable();
        String[] colNames = useED?mata.getExperimentDisplayNames().toArray(new String[0]):mata.getExperimentNames().toArray(new String[0]);
        String[] rowNames = new String[all.getNumberOfProbes()];
        int i=0;
        for (Probe pb : all)
        	rowNames[i++] = usePD?pb.getDisplayName():pb.getName();
        	       
        gaggleCluster.setColumnNames(colNames);
        gaggleCluster.setRowNames(rowNames);
        
		boss.broadcastCluster(GagglePlugin.getGoose().getAssignedName(), targetGoose, gaggleCluster);
	}
	
	public static void sendAsNetwork(List<ProbeList> probelists, DataSet ds, Boss boss, String targetGoose,
			boolean usePD, boolean addExpD, boolean addInter
			) throws RemoteException {
		Network gaggleNetwork = new Network();
		gaggleNetwork.setName(createName(probelists));
		addMetaTuple(gaggleNetwork,ds);
		
		// add all probes as nodes
		MasterTable mt = ds.getMasterTable();
		
		for (Probe pb : ProbeList.createUniqueProbeList(probelists)) {
			String primaryName = usePD?pb.getDisplayName():pb.getName();
			String secondaryName = usePD?pb.getName():pb.getDisplayName();		
			gaggleNetwork.add(primaryName);			
			gaggleNetwork.addNodeAttribute(primaryName, "Alternative Name", secondaryName);
			if (addExpD) {
				double[] vals = pb.getValues();
				for (int i=0; i!=vals.length; ++i)
					gaggleNetwork.addNodeAttribute(primaryName, mt.getExperimentName(i), String.valueOf(vals[i]));
			}
		}

		// add interactions from probelists - this is O(n^2)		
		if (addInter) {
			for (ProbeList pl: probelists) {
				String interName = pl.getName();			
				for (int i=0; i!=pl.getNumberOfProbes(); ++i) {
					Probe pb1 = pl.getProbe(i);
					String primaryName1 = usePD?pb1.getDisplayName():pb1.getName();
					for (int j=i+1; j!=pl.getNumberOfProbes(); ++j) {
						Probe pb2 = pl.getProbe(j);					
						String primaryName2 = usePD?pb2.getDisplayName():pb2.getName();
						gaggleNetwork.add(new Interaction(primaryName1, primaryName2, interName));
					}
				}
			}
		}
		
		boss.broadcastNetwork(GagglePlugin.getGoose().getAssignedName(), targetGoose, gaggleNetwork);
		
	}
	
	protected static void sendAsTuple(List<ProbeList> probelists, DataSet ds, Boss boss, String targetGoose) throws RemoteException {
		GaggleTuple gaggleTuple = new GaggleTuple();
        gaggleTuple.setName(createName(probelists));
        addMetaTuple(gaggleTuple, ds);
        
        // send each probe with exp data and all meta info 
        // send each pl with all probes contained 
        
		Tuple probeListTuple = new Tuple();
    	for (ProbeList pl : probelists){
    		Tuple plContent = new Tuple();
    		for (Probe pb : pl)
    			plContent.addSingle(new Single(pb.getName(), pb.getDisplayName()));
    		Single plEntry = new Single(pl.getName(), plContent);
    		probeListTuple.addSingle(plEntry);
    	}
    	
    	MasterTable mt = ds.getMasterTable();
    	
    	Tuple probesExTuple = new Tuple();
    	for (Probe pb : ProbeList.createUniqueProbeList(probelists)) {
    		Tuple experimentTuple = new Tuple();
    		int i=0;
	 		for (double value : pb.getValues()){
				experimentTuple.addSingle(new Single(mt.getExperimentName(i++), value));
			}
	 		Single probeSingle = new Single(pb.getName(), experimentTuple);
	 		probesExTuple.addSingle(probeSingle);
    	}
    	
    	Tuple probesMiTuple = new Tuple();
    	for (Probe pb : ProbeList.createUniqueProbeList(probelists)) {
    		Tuple metaTuple = new Tuple();
    		for (MIGroup mg : ds.getMIManager().getGroupsForObject(pb))
    			metaTuple.addSingle(new Single(mg.getPath()+"/"+mg.getName(), mg.getMIO(pb).toString()));
    		 Single probeSingle = new Single(pb.getName(), metaTuple);
    		 probesMiTuple.addSingle(probeSingle);
    	}
    	
    	Tuple everyTuple = new Tuple();
    	everyTuple.addSingle(new Single("Expression", probesExTuple));
    	everyTuple.addSingle(new Single("Meta Info", probesMiTuple));
    	everyTuple.addSingle(new Single("ProbeLists", probeListTuple));
    	
    	gaggleTuple.setData(everyTuple);

    	boss.broadcastTuple(GagglePlugin.getGoose().getAssignedName(), targetGoose, gaggleTuple);
	}

}
