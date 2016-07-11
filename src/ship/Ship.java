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
		logger.debug("������ ������� " + name + ", ����� �������� ������� " + shipWarehouseSize + " �����������.");
	}

	/*public void setContainersToWarehouse(List<Container> containerList) {
		shipWarehouse.addContainer(containerList);
	}*/
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
			logger.error("� �������� ��������� ������������ � �� ���������.", e);
		} catch (PortException e) {
			logger.error("� ������ ��������� ������������ � �� ���������.", e);//!!! ���������� ���������
		}
	}

	private void atSea() throws InterruptedException {
		logger.debug("~~~~~ ������� " + name + " ������ �� ���� ~~~~~\n");
		Thread.sleep(1000);
	}


	private void inPort() throws PortException, InterruptedException {

		boolean isLockedBerth = false;
		Berth berth = null;
		try {
			isLockedBerth = port.lockBerth(this);
			
			if (isLockedBerth) {
				berth = port.getBerth(this);
				logger.debug("������� " + name + " �������������� � ������� " + berth.getId());
				ShipAction action = getNextAction();
				executeAction(action, berth);
			} else {
				logger.debug("������� " + name + " �������� � ��������� � ������� ");
			}
		} finally {
			if (isLockedBerth){
				port.unlockBerth(this);
				logger.debug("������� " + name + " ������ �� ������� " + berth.getId());
			}
		}
		
	}

	private void executeAction(ShipAction action, Berth berth) throws InterruptedException {
		switch (action) {
		case LOAD_TO_PORT:
				logger.debug("������� " + name + ": ����� ����������� [�� ������ ������� " 
						+ shipWarehouse.getRealSize() + " �����������]");
 				loadToPort(berth);
			break;
		case LOAD_FROM_PORT:
				logger.debug("������� " + name + ": ����� ����������� [�� ������ ����� " 
						+ port.getPortWarehouse().getRealSize() + " �����������]");
				loadFromPort(berth);
			break;
		}
	}

	private boolean loadToPort(Berth berth) throws InterruptedException {

		int containersNumberToMove = conteinersCount(shipWarehouse.getRealSize());
		boolean result = false;

		logger.debug("������� " + name + " ����� ��������� " + containersNumberToMove
				+ " ����������� �� ����� �����.");

		result = berth.add(shipWarehouse, containersNumberToMove);
		
		if (!result) {
			logger.debug("������������ ����� �� ������ ����� ��� �������� �������� "
					+ name + " " + containersNumberToMove + " �����������.");
		} else {
			logger.debug("������� " + name + " �������� " + containersNumberToMove
					+ " ����������� � ����.");
			
		}
		System.out.println("������ �� ������� " + name + " " + shipWarehouse.getRealSize() + " �����������, �� ������ - "
				+ port.getPortWarehouse().getRealSize() + " �����������");
		return result;
	}

	private boolean loadFromPort(Berth berth) throws InterruptedException {
		int count = port.getPortWarehouse().getRealSize() > 20 ? 20 : port.getPortWarehouse().getRealSize();
		int containersNumberToMove = conteinersCount(count);
		
		boolean result = false;

		logger.debug("������� " + name + " ����� ��������� " + containersNumberToMove
				+ " ����������� �� ������ �����.");
		
		result = berth.get(shipWarehouse, containersNumberToMove);
		
		if (result) {
			logger.debug("������� " + name + " �������� " + containersNumberToMove
					+ " ����������� �� �����.");
		} else {
			logger.debug("������������ ����� �� �� ������� " + name
					+ " ��� �������� " + containersNumberToMove + " ����������� �� �����.");
		}
		System.out.println("������ �� ������� " + name + " " + shipWarehouse.getRealSize() + " �����������, �� ������ - "
				+ port.getPortWarehouse().getRealSize() + " �����������");
		return result;
	}

	private int conteinersCount(int count) {//!!!!
		Random random = new Random();
		return random.nextInt(count) + 1;
	}

	private ShipAction getNextAction() {
		if (shipWarehouse.getFreeSize() == 0) {
			logger.debug("������� " + name + " ������ ��������� �����������");
			return ShipAction.LOAD_TO_PORT;
		}
		if (shipWarehouse.getFreeSize() == shipWarehouse.getSize()) {
			logger.debug("������� " + name + " ������ ������ ������");
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
