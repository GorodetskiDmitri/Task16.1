package port;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ship.Ship;
import warehouse.Container;
import warehouse.Warehouse;

public class Port {
	private final static Logger logger = Logger.getRootLogger();

	private List<Berth> berthList;
	private Warehouse portWarehouse; 
	private Map<Ship, Berth> usedBerths; 

	public Port(int berthSize, int warehouseSize) {
		portWarehouse = new Warehouse(warehouseSize); 
		berthList = new ArrayList<Berth>(berthSize); 			
		
		for (int i = 0; i < berthSize; i++) {
			berthList.add(new Berth(i, portWarehouse));
		}
		usedBerths = new HashMap<Ship, Berth>();
		logger.debug("Port created.");
	}

	public void setContainersToWarehouse(List<Container> containerList){
		 portWarehouse.addContainer(containerList);
	}
	
	public boolean lockBerth(Ship ship) {
		boolean result = false;
		Berth berth = null;

		synchronized (berthList) {
			if (!berthList.isEmpty()) {
				berth = berthList.remove(0);
			}
		}
		
		if (berth != null) {
			result = true;
			synchronized (usedBerths) {
				usedBerths.put(ship, berth);
			}
		}
		
		return result;
	}

	public boolean unlockBerth(Ship ship) {
		Berth berth = usedBerths.get(ship);
		synchronized (berthList) {
			synchronized (usedBerths) {
				berthList.add(berth);
				usedBerths.remove(ship);
			}
		}
		return true;
	}

	public Berth getBerth(Ship ship) throws PortException {
		Berth berth;
		synchronized (usedBerths) {
			berth = usedBerths.get(ship);
		}
		if (berth == null) {
			throw new PortException("Try to use Berth without blocking.");
		}
		return berth;
	}
	
	public Warehouse getPortWarehouse() {
		return this.portWarehouse;
	}
}
