package port;

import java.util.List;

import org.apache.log4j.Logger;

import warehouse.Container;
import warehouse.Warehouse;

public class Berth {

	private final static Logger logger = Logger.getRootLogger();
	private int id;
	private Warehouse portWarehouse;

	public Berth(int id, Warehouse warehouse) {
		this.id = id;
		portWarehouse = warehouse;
	}

	public int getId() {
		return id;
	}

	public boolean add(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;
		
		List<Container> conteinersFromShip = shipWarehouse.getContainer(numberOfConteiners);
		synchronized (portWarehouse) {
			if (portWarehouse.getFreeSize() >= numberOfConteiners) {
				result = portWarehouse.addContainer(conteinersFromShip);
			}
		}
		return result;
	}
	
	public boolean get(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;
		List<Container> conteinersFromPort = null;
		
		synchronized (portWarehouse) {
			conteinersFromPort = portWarehouse.getContainer(numberOfConteiners);
			if (conteinersFromPort == null) {
				logger.error("Port warehouse has not requested number of containers");
				return false;
			}
		}
		
		if (shipWarehouse.getFreeSize() >= numberOfConteiners && conteinersFromPort != null) {
			result = shipWarehouse.addContainer(conteinersFromPort);
		}
		
		return result;
	}
	
}
