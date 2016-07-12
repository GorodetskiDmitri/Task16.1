package ship;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import port.Berth;
import port.Port;
import port.PortException;
import warehouse.Container;
import warehouse.Warehouse;

public class Ship implements Runnable {

	private final static Logger logger = Logger.getRootLogger();
	private volatile boolean stopThread = false;

	private String name;
	private Port port;
	private Warehouse shipWarehouse;

	public Ship(String name, Port port, int shipWarehouseSize) {
		this.name = name;
		this.port = port;
		shipWarehouse = new Warehouse(shipWarehouseSize);
		logger.debug("Ship " + name + " created.");
	}

	public void setContainersToWarehouse(List<Container> containerList) {
		shipWarehouse.addContainer(containerList);
	}

	public String getName() {
		return name;
	}

	public void stopThread() {
		stopThread = true;
	}

	public void run() {
		try {
			while (!stopThread) {
				atSea();
				inPort();
			}
		} catch (InterruptedException e) {
			logger.error("On the ship there was a nuisance, and it is destroyed.", e);
		} catch (PortException e) {
			logger.error("In the port there was a nuisance.", e);
		}
	}

	private void atSea() throws InterruptedException {
		logger.debug("~~~~~ Ship " + name + " sails on the sea ~~~~~");
		Thread.sleep(1000);
	}

	private void inPort() throws PortException, InterruptedException {

		boolean isLockedBerth = false;
		Berth berth = null;
		try {
			isLockedBerth = port.lockBerth(this);
			
			if (isLockedBerth) {
				berth = port.getBerth(this);
				logger.debug("Ship " + name + " docked to the berth " + berth.getId());
				ShipAction action = getNextAction();
				executeAction(action, berth);
			} else {
				logger.debug("Ship " + name + " refused to docking berth.");
			}
		} finally {
			if (isLockedBerth){
				port.unlockBerth(this);
				logger.debug("Ship " + name + " moved away from the berth " + berth.getId());
			}
		}
	}

	private void executeAction(ShipAction action, Berth berth) throws InterruptedException {
		/*logger.debug("Êîðàáëü " + name + ": ÁÓÄÅÌ ÇÀÃÐÓÆÀÒÜÑß [íà ñêëàäå ïîðòà " 
				+ port.getPortWarehouse().getRealSize() + " êîíòåéíåðîâ]");
		loadFromPort(berth);*/
		switch (action) {
		case LOAD_TO_PORT:
				logger.debug("Ship " + name + ": UNLOAD [ship has " 
						+ shipWarehouse.getRealSize() + " containers].");
 				loadToPort(berth);
			break;
		case LOAD_FROM_PORT:
				logger.debug("Ship " + name + ": DOWNLOAD [port has " 
						+ port.getPortWarehouse().getRealSize() + " containers].");
				loadFromPort(berth);
			break;
		}
	}

	private boolean loadToPort(Berth berth) throws InterruptedException {

		int containersNumberToMove = conteinersCount(shipWarehouse.getRealSize());
		boolean result = false;

		logger.debug("Ship " + name + " wants to unload " + containersNumberToMove
				+ " containers to the port warehouse.");

		result = berth.add(shipWarehouse, containersNumberToMove);
		
		if (!result) {
			logger.debug("Not enough space in the port warehouse to unloading by ship "
					+ name + " " + containersNumberToMove + " containers.");
		} else {
			logger.debug("Ship " + name + " unloaded " + containersNumberToMove
					+ " containers to the port warehouse.");
			
		}
		return result;
	}

	private boolean loadFromPort(Berth berth) throws InterruptedException {
		int currentNumberOfPortContainers = port.getPortWarehouse().getRealSize();
		if (currentNumberOfPortContainers == 0) {
			logger.debug("No more containers in the port warehouse.");
			return false;
		}
		int count = currentNumberOfPortContainers > 20 ? 20 : port.getPortWarehouse().getRealSize();
		int containersNumberToMove = conteinersCount(count);
		
		boolean result = false;

		logger.debug("Ship " + name + " wants to download " + containersNumberToMove
				+ " containers from the port warehouse.");
		
		result = berth.get(shipWarehouse, containersNumberToMove);
		
		if (result) {
			logger.debug("Ship " + name + " downloaded " + containersNumberToMove
					+ " containers to the ship warehouse.");
		} else {
			logger.debug("Not enough space in the ship warehouse to downloading by ship " + name
					+ " " + containersNumberToMove + " containers.");
		}
		return result;
	}

	private int conteinersCount(int count) {
		Random random = new Random();
		return random.nextInt(count) + 1;
	}

	private ShipAction getNextAction() {
		if (shipWarehouse.getFreeSize() == 0) {
			return ShipAction.LOAD_TO_PORT;
		}
		if (shipWarehouse.getFreeSize() == shipWarehouse.getSize()) {
			return ShipAction.LOAD_FROM_PORT;
		}
		Random random = new Random();
		int value = random.nextInt(4000);
		if (value < 1000) {
			return ShipAction.LOAD_TO_PORT;
		} else if (value < 2000) {
			return ShipAction.LOAD_FROM_PORT;
		}
		return ShipAction.LOAD_TO_PORT;
	}

	enum ShipAction {
		LOAD_TO_PORT, LOAD_FROM_PORT
	}
}
