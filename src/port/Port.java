package port;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import ship.Ship;
import warehouse.Container;
import warehouse.Warehouse;

public class Port {
	private final static Logger logger = Logger.getRootLogger();

	private List<Berth> berthList; // очередь причалов
	//private BlockingQueue<Berth> berthList;
	private Warehouse portWarehouse; // хранилище порта
	private Map<Ship, Berth> usedBerths; // какой корабль у какого причала стоит

	public Port(int berthSize, int warehouseSize) {
		portWarehouse = new Warehouse(warehouseSize); // создаем пустое
														// хранилище
		berthList = new ArrayList<Berth>(berthSize); // создаем очередь причалов
		//berthList = new ArrayBlockingQueue<Berth>(berthSize);
		for (int i = 0; i < berthSize; i++) { // заполняем очередь причалов непосредственно самими причалами
			berthList.add(new Berth(i, portWarehouse));
		}
		usedBerths = new HashMap<Ship, Berth>(); // создаем объект, который
													// будет
		logger.debug("Порт создан. Причалов - " + berthSize + "; склад вмещает " + warehouseSize + " контейнеров");
	}

	public void setContainersToWarehouse(List<Container> containerList){
		 portWarehouse.addContainer(containerList);
	}

	public boolean lockBerth(Ship ship) {
		boolean result = false;
		Berth berth;

		//!!!!!!!!!
		synchronized (berthList) {
			if (berthList.isEmpty()) {
				return false;
			}
			berth = berthList.remove(0);
		}
		
		if (berth != null) {
			result = true;
			usedBerths.put(ship, berth);
		}
		
	
		/*try {
			berth = berthList.take();
			
			if (berth != null) {
				result = true;
				usedBerths.put(ship, berth);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/	
		
		return result;
	}

	public boolean unlockBerth(Ship ship) throws PortException {
		Berth berth = usedBerths.get(ship);
		if (berth == null) {
			throw new PortException("Try to unlock Berth without blocking.");
		}
		synchronized (berthList) {
			berthList.add(berth);
		}	
		usedBerths.remove(ship);	
		return true;
	}

	public Berth getBerth(Ship ship) throws PortException {

		Berth berth = usedBerths.get(ship);
		if (berth == null) {
			throw new PortException("Try to use Berth without blocking.");
		}
		return berth;
	}
	
	public Warehouse getPortWarehouse() {
		return this.portWarehouse;
	}
}
