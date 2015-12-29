package mayday.gaggle.incoming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

import mayday.core.DataSet;
import mayday.core.MasterTable;
import mayday.core.Probe;
import mayday.core.io.dataset.tabular.TabularImport;
import mayday.core.meta.MIGroup;
import mayday.core.meta.MIGroupSelection;
import mayday.core.meta.MIManager;
import mayday.core.meta.MIType;
import mayday.core.meta.types.StringMIO;
import mayday.core.structures.maps.MultiHashMap;
import mayday.core.structures.trie.Trie;
import mayday.core.structures.trie.TrieNode;

import org.systemsbiology.gaggle.core.datatypes.Interaction;
import org.systemsbiology.gaggle.core.datatypes.Network;

public class NetworkToDataset {

	public static DataSet convert(Network network, String sender) {
		// create a tabular view of the network, then use the default tabular importer
		DefaultTableModel model = convert(network);
		
		DataSet ds = TabularImport.parseDataset(model,"Network \"" + network.getName());
		if (ds==null)
			return null;
		
		ds.getAnnotation().setQuickInfo("Received from "+sender+" via Gaggle");							
		
		// reorder groups
		ArrayList<String> groupNames = new ArrayList<String>();
		for (MIGroup mg : ds.getMIManager().getGroups())
			groupNames.add(mg.getName());
		addInteractionMIOs(ds, network);
		buildTreeHierarchy(ds, groupNames);
				
		return ds;
	}
	
	public static DefaultTableModel convert(Network network) {
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("Node");
		
		for (String attribute : network.getNodeAttributeNames()) 
			model.addColumn(attribute);		
				
		model.setNumRows(network.getNodes().length);
		int row=0;
		
		for (String node : network.getNodes()) {
			model.setValueAt(node, row, 0);
			int col=1;
			for (String attribute : network.getNodeAttributeNames()) 
				model.setValueAt(network.getNodeAttributes(attribute).get(node), row, col++);		
			++row;
		}
		
		return model;		
	}
	
	private static String getPrefix(String name, Trie pathTree){
		TrieNode node = pathTree.getNode(name);
		TrieNode parent = node.getParent();
		String prefix = "";
		while (parent != null) {
			if (parent.getChildren().size() > 1) {
				pathTree.add(parent.getPrefix());
				if (parent.getPrefix().length() >= 4) {
					prefix = parent.getPrefix();
				}
			}
			parent = parent.getParent();
		}
		return prefix;
	}
	
	private static MIGroup getOrCreate(String prefix, MIManager mi, String path){
		MIGroupSelection<MIType> mgs = mi.getGroupsForName(prefix);
		if (mgs.size()>0)
			return mgs.get(0);
		return mi.newGroup("PAS.MIO.String", prefix, path);
	}
	
	private static void buildTreeHierarchy(DataSet ds, ArrayList<String> groupNames){
		MIManager mi = ds.getMIManager();
		Trie pathTree = buildTree(new Trie(), groupNames.toArray(new String[groupNames.size()]));
		for (MIGroup mg : mi.getGroups()){
			if (!mg.getPath().equals("")){
				String prefix = getPrefix(mg.getName(), pathTree);
				String path = mg.getPath().substring(1);
				if (!prefix.equals(path) && !prefix.equals("")){
					MIGroup newGroup = getOrCreate(prefix, mi, path);
					String name = mg.getName();
					String mioType = mg.getMIOType();
					// instead of creating a new group and moving the objects,
					// it would be nicer to just move the group
					Set<Entry<Object, MIType>> mios = mg.getMIOs();
					mi.removeGroup(mg);
					MIGroup movedGroup = mi.newGroup(mioType, name.substring(newGroup.getName().length()), newGroup);
					for (Entry<Object, MIType> mio: mios){
						movedGroup.add(mio.getKey(), mio.getValue());
					}
				}
			}
		}
	}
	
	private static Trie buildTree(Trie pathTree, String[] names) {
		for (String s : names) {
			pathTree.add(s);
		}
		return pathTree;
	}


	public static void addInteractionMIOs(DataSet ds, Network network) {
		
		MIManager mi = ds.getMIManager();
		MasterTable mt = ds.getMasterTable();
		
		// map node names to interactions
		MultiHashMap<String, Interaction> byTarget = new MultiHashMap<String, Interaction>();
		MultiHashMap<String, Interaction> bySource = new MultiHashMap<String, Interaction>();
		for (Interaction intac : network.getInteractions()) {
			byTarget.put(intac.getTarget(), intac);
			bySource.put(intac.getSource(), intac);
		}
		
		for (int i = 0; i < network.nodeCount(); i++) {

			String nodeName = network.getNodes()[i];
			MIGroup mioTypeGroup = mi.newGroup("PAS.MIO.String", nodeName);
			HashMap<String, StringMIO> typeMios = new HashMap<String, StringMIO>();			
			
			for (Interaction incoming : byTarget.get(nodeName)) {
				Probe sourceProbe = mt.getProbe(incoming.getSource());
				if (sourceProbe!=null) {
					StringMIO mio = getOrCreate(typeMios, incoming.getType());
					mioTypeGroup.add(sourceProbe, mio);
				}
			}
			
			for (Interaction outgoing : bySource.get(nodeName)) {
				Probe targetProbe = mt.getProbe(outgoing.getTarget());
				if (targetProbe!=null) {
					StringMIO mio = getOrCreate(typeMios, outgoing.getType());
					mioTypeGroup.add(targetProbe, mio);
				}
				
			}

		}
		
	}
	
	
	protected static StringMIO getOrCreate(HashMap<String, StringMIO> map, String content) {
		StringMIO existing = map.get(content);
		if (existing==null)
			map.put(content, existing = new StringMIO(content) );
		return existing;
	}
	
}
