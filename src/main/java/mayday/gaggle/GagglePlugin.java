package mayday.gaggle;

import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import mayday.core.Mayday;
import mayday.core.pluma.AbstractPlugin;
import mayday.core.pluma.Constants;
import mayday.core.pluma.PluginInfo;
import mayday.core.pluma.PluginManagerException;
import mayday.core.pluma.prototypes.GenericPlugin;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.datatypes.GaggleData;


public class GagglePlugin extends AbstractPlugin implements GenericPlugin{
	
	public final static String gaggleName = "Mayday";
	
	@SuppressWarnings("unchecked")
	public PluginInfo register() throws PluginManagerException{
		//System.out.println("PL1: Register");		
		PluginInfo pli= new PluginInfo(
				(Class)this.getClass(),
				"PAS.Gaggle",
				new String[0], 
				Constants.MC_SESSION,
				(HashMap<String,Object>)null,
				"Claudia Broelemann",
				"broelema@informatik.uni-tuebingen.de",
				"Starts the Gaggle integration",
				"Gaggle Integration");
		pli.setIcon("mayday/gaggle/gaggle.gif");
		return pli;
	}

	public static MaydayGoose goose;	
	public static GaggleGUIElement guiElement;
	public static boolean isGuiVisible = false;
	
	protected static LinkedList<GaggleData> recievedObjects = new LinkedList<GaggleData>();
	protected static LinkedList<String> senders = new LinkedList<String>();
	
	public void run() {
		if (!showGUI()) {
			JOptionPane.showMessageDialog(Mayday.sharedInstance, 
				"The Gaggle plugin is already active.\n" +
				"You can use it at the right of Mayday's main window.",
				"Gaggle Plugin already active", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public static boolean showGUI() {
		if (guiElement==null) {
			guiElement = new GaggleGUIElement(getGoose());
		}
		if (!isGuiVisible) {
			Mayday.sharedInstance.addPluggableViewElement(guiElement, "Gaggle");
			isGuiVisible = true;
			return true;
		}
		return false;
	}
	
	public void init() {
	}
	
	public static MaydayGoose getGoose() {
		if (goose==null) {
			goose = new MaydayGoose();			
		}
		return goose;
	}
	
	@Override
	public void unload() {
		if (goose!=null)
			goose.getConnector().disconnectFromGaggle(false);
	}

	public static void handleData(String source, GaggleData data) {
		System.out.println("Now handling data of type "+data.getClass());
		showGUI();
		recievedObjects.add(data);
		senders.add(source);
		guiElement.notifyObject();
	}
	
	public static  int getNumberOfWaitingObjects() {
		return recievedObjects.size();
	}
	
	public static GaggleData getWaitingObject(int index) {
		return recievedObjects.get(index);		
	}
	
	public static String getSender(int index) {
		return senders.get(index);
	}
	
	public static void dismissObject(int index) {
		recievedObjects.remove(index);
		senders.remove(index);
		guiElement.notifyObject();
	}
	
	public static void dismissObject(GaggleData data) {
		int i = recievedObjects.indexOf(data);
		if (i>-1) {
			dismissObject(i);
		}
				
	}
	
	public static boolean ensureConnected()  {
		MaydayGoose goose = getGoose();
		
		if (!goose.isConnected()) {
			JOptionPane.showMessageDialog(Mayday.sharedInstance, 
					"You are not connected to a Gaggle Boss. \n" +
					"Please connect before sending data.",
					"Not connected.", JOptionPane.INFORMATION_MESSAGE);
			showGUI();
			return false;
		}
		
		return true;
	}
	
	public static Boss getBoss() {
		return goose.getConnector().getBoss();
	}
	
}
