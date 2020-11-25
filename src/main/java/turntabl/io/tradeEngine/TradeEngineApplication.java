package turntabl.io.tradeEngine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redis.clients.jedis.Jedis;
import turntabl.io.tradeEngine.model.ClientOrder;
import turntabl.io.tradeEngine.taskQueue.MakeOrder;
import turntabl.io.tradeEngine.taskQueue.MonitorQueue;

@SpringBootApplication
public class TradeEngineApplication {

	public static void main(String[] args) {
//		ClientOrder clientOrder = new ClientOrder("1011","IBM",1.7 ,5,"BUY");
//		String validatedOrderString = Utility.convertToString(clientOrder);
//
//		System.out.println(validatedOrderString);
//		new Jedis().lpush("makeorder",validatedOrderString);

		SpringApplication.run(TradeEngineApplication.class, args);

		MakeOrder makeOrder = new MakeOrder();

		Thread makeOrderThread = new Thread(makeOrder);
		makeOrderThread.start();

		MonitorQueue monitorQueue = new MonitorQueue();

		Thread monitorQueueThread = new Thread(monitorQueue);
		monitorQueueThread.start();


	}

}
