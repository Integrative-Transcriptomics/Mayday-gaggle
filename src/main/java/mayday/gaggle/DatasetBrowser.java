package mayday.gaggle;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import mayday.core.DataSet;
import mayday.core.MasterTable;
import mayday.core.Probe;
import mayday.core.ProbeList;
import mayday.core.gui.MaydayFrame;
import mayday.core.gui.ProbeListImage;
import mayday.core.gui.components.ExcellentBoxLayout;
import mayday.core.meta.MIGroup;
import mayday.core.meta.MIGroupSelection;
import mayday.core.meta.MIManager;
import mayday.core.meta.MIType;
import mayday.core.meta.gui.MIGroupSelectionDialog;
import mayday.core.structures.maps.MultiHashMap;
import mayday.vis3.PlotPlugin;
import mayday.vis3.model.Visualizer;

@SuppressWarnings("serial")
public class DatasetBrowser extends MaydayFrame {

	private  List<String> nameList;
	protected DataSet dataSet;

	private HashSet<Probe> selectedProbes = new HashSet<Probe>();
	private HashSet<Entry<Object, MIType>> selectedMIO = new HashSet<Entry<Object, MIType>>();
	
	private HashMap<Object, DefaultMutableTreeNode> resultTreeNodes = new HashMap<Object, DefaultMutableTreeNode>();
	private HashMap<Object, DefaultMutableTreeNode> metaNodes = new HashMap<Object, DefaultMutableTreeNode>();
	
	private JTree tree;
	private DefaultMutableTreeNode top;
	
	
	public DatasetBrowser(DataSet ds, List<String> nameList) {
		super("Browse DataSet");		
		this.nameList = nameList;
		dataSet = ds;
		createGUI();
	}

	
	protected void createGUI() {
		setLayout(new BorderLayout());
		
		PreviewPanel gaggleStringPreview = new PreviewPanel();
		ActionPanel actions = new ActionPanel();
		ResultTreePane resultPane = new ResultTreePane();		
		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.add(actions, BorderLayout.WEST);
		searchPanel.add(resultPane, BorderLayout.CENTER);

		JTabbedPane tabs = new JTabbedPane();
		tabs.add("Search in \""+dataSet.getName()+"\"", searchPanel);
		tabs.add("Received data from Gaggle", gaggleStringPreview);
		
		add(tabs, BorderLayout.CENTER);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);		
		pack();
		setSize(600,600);
	}

	
	

	public void searchMeta() {
		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode MIGroup = null;
		DefaultMutableTreeNode metaInf = null;
		DefaultMutableTreeNode probeNode = null;
		DefaultMutableTreeNode probeLists = null;
		DefaultMutableTreeNode parentNode = null;

		category = getOrCreateNode(resultTreeNodes, "Meta Information");
		top.add(category);
		MIManager miManager = dataSet.getMIManager();
		MIGroupSelectionDialog mioGroupSelectionDialog = new MIGroupSelectionDialog(miManager);
		mioGroupSelectionDialog.setDialogDescription("Select one or more MI groups to search in");				
		mioGroupSelectionDialog.setVisible(true);
		MIGroupSelection<MIType> mioSelection = mioGroupSelectionDialog.getSelection();
		
		for (MIGroup mg : mioSelection) {
			MIGroup = getOrCreateNode(resultTreeNodes, mg);
			category.add(MIGroup);
			for (Entry<Object, MIType> mio : mg.getMIOs()) {
				for (int i = 0; i < nameList.size(); i++) {
					if (mio.getValue().toString().equals(nameList.get(i))) {
						selectedMIO.add(mio);
						metaInf = getOrCreateNode(resultTreeNodes, mio.getValue().toString());
						if (!metaNodes.containsKey(mio.getKey())){
							probeNode = getOrCreateNode(metaNodes, mio.getKey());
							metaInf.add(probeNode);
							addProbeLists((Probe) mio.getKey(), probeLists, parentNode, probeNode);
							MIGroup.add(metaInf);
						}
						break;
					}
				}
			}
		}
		tree.updateUI();
	}

	public void searchProbe() {
		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode probe = null;
		DefaultMutableTreeNode probeLists = null;
		DefaultMutableTreeNode parentNode = null;
		category = getOrCreateNode(resultTreeNodes, "Probe");
		top.add(category);
	
		MasterTable mata = dataSet.getMasterTable();
		for (String name : nameList) {
			Probe pb = mata.getProbe(name);
			if (pb != null) { 
				selectedProbes.add(pb);
				if (!resultTreeNodes.containsKey(pb)){
					probe = getOrCreateNode(resultTreeNodes, pb);
					category.add(probe);
					addProbeLists(pb, probeLists, parentNode, probe);
				}
			}
		}
		tree.updateUI();
	}
	
	public void searchProbeDisplayName() {
		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode probe = null;
		DefaultMutableTreeNode probeLists = null;
		DefaultMutableTreeNode parentNode = null;
		category = getOrCreateNode(resultTreeNodes, "Probe");
		top.add(category);
	
		MultiHashMap<String, Probe> byDisplayName = new MultiHashMap<String, Probe>();
		MasterTable mata = dataSet.getMasterTable();
		for (Probe pb : mata.getProbes().values())
			byDisplayName.put(pb.getDisplayName(), pb);
		
		for (String name : nameList) {
			for (Probe pb : byDisplayName.get(name)) {
				selectedProbes.add(pb);
				if (!resultTreeNodes.containsKey(pb)){
					probe = getOrCreateNode(resultTreeNodes, pb);
					category.add(probe);
					addProbeLists(pb, probeLists, parentNode, probe);
				}
			}
		}
		tree.updateUI();
	}

	public void searchProbeList() {
		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode probeLists = null;
		DefaultMutableTreeNode parentNode = null;
		category = getOrCreateNode(resultTreeNodes, "ProbeList");
		top.add(category);		
		for (String name : nameList) {
			ProbeList probeList = dataSet.getProbeListManager().getProbeList(name);
			if (probeList!=null) {
				probeLists = getOrCreateNode(resultTreeNodes, probeList);
				parentNode = parentListSearch(probeList, probeLists, resultTreeNodes);
				category.add(parentNode);
				for (Probe pb : probeList.getAllProbes()) {
					selectedProbes.add(pb);
				}
				break;
			}
		}
		tree.updateUI();
	}

	public void addProbeLists(Probe probes, DefaultMutableTreeNode probeLists,
			DefaultMutableTreeNode parentNode, DefaultMutableTreeNode probe) 
	{
		HashMap<Object, DefaultMutableTreeNode> nodes = new HashMap<Object, DefaultMutableTreeNode>();
		for (ProbeList pList : probes.getProbeLists()) {
			DefaultMutableTreeNode newNode = getOrCreateNode(nodes, pList);
			if (pList.getParent() != null) {
				newNode = parentListSearch(pList, newNode, nodes);
			}
			probe.add(newNode);
		}
	}

	/**
	 * if the tree contains object o, the method returns the according node
	 * otherwise a new node is created
	 */
	public DefaultMutableTreeNode getOrCreateNode(HashMap<Object, DefaultMutableTreeNode> nodes, Object o) {
		DefaultMutableTreeNode res = null;
		if (nodes!=null) {
			res = nodes.get(o);
			if (res==null)
				nodes.put(o, res=new DefaultMutableTreeNode(o));
		} else {
			res = new DefaultMutableTreeNode(o);
		}
		return res;
	}

	public void probeListClick(ProbeList pl, DefaultMutableTreeNode clickedNode) {
		PlotPlugin plp = ProbeListImage.doubleclickplot.getInstance();
		if (plp != null) {
			LinkedList<ProbeList> lpl = new LinkedList<ProbeList>();
			lpl.add(pl);
			Visualizer viz = Visualizer.createWithPlot(dataSet, lpl, plp.getComponent());
			Probe pb = getProbeForSelection(clickedNode);
			if (pb != null) 
				viz.getViewModel().setProbeSelection(pb);
		} else {
			System.err.println("Could not open plot plugin");
		}
	}

	public Probe getProbeForSelection(DefaultMutableTreeNode searchNode) {
		while (searchNode!=null) {
			if (searchNode.getUserObject() instanceof Probe)
				return ((Probe)searchNode.getUserObject());
			searchNode = (DefaultMutableTreeNode)searchNode.getParent();
		}
		return null;
	}

	public DefaultMutableTreeNode parentListSearch(
			ProbeList probeList, DefaultMutableTreeNode probeListNode,
			HashMap<Object, DefaultMutableTreeNode> nodes) 
	{
		if (probeList.getParent() != null) {
			DefaultMutableTreeNode parentNode = getOrCreateNode(nodes, probeList.getParent());
			parentNode.add(probeListNode);
			probeListNode = parentListSearch(probeList.getParent(), parentNode, nodes);
		}
		return probeListNode;
	}
	
	
	protected class PreviewPanel extends JPanel {		
		public PreviewPanel() {
			super(new BorderLayout());
			add(new JScrollPane(new JList(nameList.toArray(new String[0]))), BorderLayout.CENTER);
		}
	}	
	
	protected class ActionPanel extends JPanel {
		public ActionPanel() {
			super(new ExcellentBoxLayout(true, 5));
			setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			
			add(new JButton(new AbstractAction("Search in probe names") {
				public void actionPerformed(ActionEvent ev) {
					searchProbe();
				}
			}));
			add(new JButton(new AbstractAction("Search in probe display names") {
				public void actionPerformed(ActionEvent ev) {
					searchProbeDisplayName();
				}
			}));
			add(new JButton(new AbstractAction("Search in probelist names") {
				public void actionPerformed(ActionEvent ev) {
					searchProbeList();	
				}
			}));
			add(new JButton(new AbstractAction("Search in meta information") {
				public void actionPerformed(ActionEvent ev) {
					searchMeta();					
				}
			}));
			
			add(new JSeparator());
			
			add(new JButton(new AbstractAction("Clear result list") {
				public void actionPerformed(ActionEvent ev) {
					metaNodes.clear(); 
					resultTreeNodes.clear();
					selectedProbes.clear();
					selectedMIO.clear();
					top.removeAllChildren();
					tree.updateUI();
				}
			}));
			add(new JButton(new AbstractAction("Create a ProbeList") {
				public void actionPerformed(ActionEvent ev) {
					if (!selectedProbes.isEmpty()) {
						ProbeList pl = new ProbeList(dataSet, true);
						pl.setName("ProbeList from Gaggle data");
						for (Probe pb : selectedProbes)
							pl.addProbe(pb);
						dataSet.getProbeListManager().addObjectAtTop(pl);
					}
				}
			}));
			add(new JButton(new AbstractAction("Create a MIGroup") {
				public void actionPerformed(ActionEvent ev) {
					if (!selectedMIO.isEmpty()){						
						String type = selectedMIO.iterator().next().getValue().getType();
						MIGroup newGroup = dataSet.getMIManager().newGroup(type, "MIGroup from Gaggle data");
						for (Entry<Object, MIType> mio : selectedMIO) 					
							newGroup.add(mio.getKey(), mio.getValue());						
					}
				}
			}));
		}
	}
	
	protected class ResultTreePane extends JScrollPane {
		
		public ResultTreePane() {
			top = new DefaultMutableTreeNode("Results found");
			tree = new JTree(top);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			// Listen for when the selection changes.
			tree.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent evt) {
					DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
					if (evt.getClickCount() == 2 && dmtn!=null && (dmtn.getUserObject() instanceof ProbeList)) 
						probeListClick((ProbeList)dmtn.getUserObject(), dmtn);
				}
			});
			this.setViewportView(tree);
		}
	}

	
}
