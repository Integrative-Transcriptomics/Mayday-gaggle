package mayday.gaggle.incoming;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import mayday.core.DataSet;
import mayday.core.Probe;
import mayday.core.ProbeList;
import mayday.core.meta.GenericMIO;
import mayday.core.meta.MIGroup;
import mayday.core.meta.MIType;
import mayday.core.settings.SettingDialog;
import mayday.core.settings.generic.BooleanHierarchicalSetting;
import mayday.core.settings.generic.HierarchicalSetting;
import mayday.core.settings.generic.SelectableHierarchicalSetting;
import mayday.core.settings.typed.BooleanSetting;
import mayday.core.settings.typed.MIGroupSetting;
import mayday.gaggle.GaggleType;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;

public class GaggleToProbelist {


	public static ProbeList convert(DataSet ds, GaggleData data, String sender) {
		
		BooleanSetting useProbeName = new BooleanSetting("Search in Probe names", 
				"Find probes with a given name. Only perfect matches are considered. \n" +
						"If a probe is found, the corresponding query is not considered for further searches.", true);
		BooleanSetting useDisplayName = new BooleanSetting("Search in Probe display names", 
				"Find probes with a given display name. Only perfect matches are considered.", true);
		String USE_ALL="use all MI groups";
		MIGroupSetting migroup = new MIGroupSetting("select one", null, null, ds.getMIManager(), false);
		SelectableHierarchicalSetting whichMIOs = new SelectableHierarchicalSetting("MI groups to use", null, 0, new Object[]{USE_ALL, migroup});
		BooleanHierarchicalSetting useMIOs = new BooleanHierarchicalSetting("Search in meta information",
				"Find probes with meta information matching the query, substring matches are allowed.\n" +
						"All meta information values are converted to character strings." , false)
		.addSetting(whichMIOs);
		HierarchicalSetting setting = new HierarchicalSetting("Create ProbeList")
		.addSetting(useProbeName)
		.addSetting(useDisplayName)			
		.addSetting(useMIOs);

		SettingDialog sdlg = new SettingDialog(null, "Create ProbeList", setting);
		if (!sdlg.showAsInputDialog().closedWithOK())
			return null;
		
		LinkedList<String> queries = new LinkedList<String>(GaggleType.extractStrings(data));
		
		ProbeList pl = createProbeList(
				data.getName(), 			
				"From: "+sender+" via Gaggle",
				ds,
				true,
				useProbeName.getBooleanValue(),
				useDisplayName.getBooleanValue(),
				useMIOs.getBooleanValue(),
				whichMIOs.getObjectValue()==USE_ALL,
				migroup.getMIGroup(),
				queries
				);
		return pl;
	}
	
	@SuppressWarnings("rawtypes")
	public static ProbeList createProbeList(String name, String source, DataSet ds, boolean asSticky, 
			boolean useName, boolean useDisplayName, boolean useMIOs, 
			boolean useAllMIOs, MIGroup useThisMIO, 
			List<String> queries) {
		
		HashSet<Probe> probesToFind = new HashSet<Probe>();


		// first search probe names, remove those found
		if (useName) {
			for (String query : new ArrayList<String>(queries)) {
				Probe p = ds.getMasterTable().getProbe(query);
				if (p!=null) {
					probesToFind.add(p);
					queries.remove(query);
				}
			}
		}

		// display names in remaining set
		if (useDisplayName) {
			for (String query : new ArrayList<String>(queries)) {
				// slow...
				for (Probe npb : ds.getMasterTable().getProbes().values()) {
					if (npb.getDisplayName().equals(query)) {
						probesToFind.add(npb);
					}
				}
			}
		}

		if (useMIOs) {			

			for (String query : new ArrayList<String>(queries)) {
				// slow...
				for (Probe npb : ds.getMasterTable().getProbes().values()) {
					if (useAllMIOs) {
						for (MIGroup mg : ds.getMIManager().getGroupsForObject(npb)) {
							MIType mt = mg.getMIO(npb);
							if (mt!=null) {
								String asString = ((GenericMIO)mt).getValue().toString();
								if (asString.contains(query)) 
									probesToFind.add(npb);
							}
						}
					} else {
						MIType mt = useThisMIO.getMIO(npb);
						if (mt!=null) {
							String asString = ((GenericMIO)mt).getValue().toString();
							if (asString.contains(query)) 
								probesToFind.add(npb);
						}
					}
				}
			}
		}


		ProbeList tmpList = new ProbeList(ds, asSticky);
		tmpList.setName(name);
		tmpList.getAnnotation().setQuickInfo(source);
		for (Probe pb : probesToFind)
			tmpList.addProbe(pb);
				return tmpList;
	}

	

	
}
