package mayday.gaggle;

import java.rmi.RemoteException;

import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.Cluster;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.datatypes.GaggleTuple;
import org.systemsbiology.gaggle.core.datatypes.Namelist;
import org.systemsbiology.gaggle.core.datatypes.Network;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;

public class MaydayGoose implements Goose {

	private RmiGaggleConnector connector = new RmiGaggleConnector(this);
	protected String assignedName;
	protected String[] otherGeese;

	public MaydayGoose() {}
	
	@Override
	public void doBroadcastList() throws RemoteException {	}

	@Override
	public void doExit() throws RemoteException {	}

	@Override
	public void doHide() throws RemoteException {	}

	@Override
	public void doShow() throws RemoteException {	}

	@Override
	public String getName() throws RemoteException {
		return GagglePlugin.gaggleName;
	}

	@Override
	public void handleCluster(String source, Cluster data) throws RemoteException {
		GagglePlugin.handleData(source, data);
	}

	@Override
	public void handleMatrix(String source, DataMatrix data) throws RemoteException {
		GagglePlugin.handleData(source, data);
	}

	@Override
	public void handleNameList(String source, Namelist data) throws RemoteException {
		GagglePlugin.handleData(source, data);
	}

	@Override
	public void handleNetwork(String source, Network data) throws RemoteException {
		GagglePlugin.handleData(source, data);
	}

	@Override
	public void handleTuple(String source, GaggleTuple data) throws RemoteException {
		GagglePlugin.handleData(source, data);
	}

	@Override
	public void setName(String newName) throws RemoteException {
		assignedName = newName;		
	}

	@Override
	public void update(String[] activeGooseNames) throws RemoteException {
		otherGeese = activeGooseNames;
	}
	
	public RmiGaggleConnector getConnector(){
		return connector;
	}	
	
	public boolean isConnected() {
		return connector.isConnected();
	}
	
	public void addConnectionListener(GaggleConnectionListener gcl) {
		connector.addListener(gcl);
	}

	public void connect() throws Exception {
		connector.connectToGaggle();
	}
	
	public void disconnect() {
		connector.disconnectFromGaggle(false);
	}
	
	public String getAssignedName() {
		return this.assignedName;
	}
	
	public String[] getGeese() {
		return otherGeese;
	}

} 

