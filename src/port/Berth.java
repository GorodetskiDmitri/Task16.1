package port;

import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.Lock;

import warehouse.Container;
import warehouse.Warehouse;

public class Berth {

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
		//boolean portLock = false;
		
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
		//boolean portLock = false;
		
		synchronized (portWarehouse) {
				if (shipWarehouse.getFreeSize() >= numberOfConteiners) {
					List<Container> conteinersFromPort = portWarehouse.getContainer(numberOfConteiners);
					if (conteinersFromPort != null) {
						result = shipWarehouse.addContainer(conteinersFromPort);
					}
				}
		}
		return result;
	}
	
	
}
