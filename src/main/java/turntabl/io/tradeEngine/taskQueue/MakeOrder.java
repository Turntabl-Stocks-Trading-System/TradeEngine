package turntabl.io.tradeEngine.taskQueue;

import redis.clients.jedis.Jedis;
import turntabl.io.tradeEngine.Utility;
import turntabl.io.tradeEngine.model.ClientOrder;
import turntabl.io.tradeEngine.model.OrderBookRequest;


public class MakeOrder implements Runnable {
    Jedis jedis = new Jedis();

    @Override
    public void run() {

        while (true){
            String data = jedis.rpop("makeorder");
            if(data == null) continue;
            System.out.println("makeorder: " +data);

            ClientOrder clientOrder = Utility.convertToObject(data,ClientOrder.class);

            jedis.set(clientOrder.getId(),data);
            jedis.lpush("monitorqueue",clientOrder.getId());
//            System.out.println("data pushed");

            OrderBookRequest orderBookRequest = new OrderBookRequest(
                    clientOrder.getId(),
                    clientOrder.getProduct(),
                    clientOrder.getSide()
            );
            String requestString = Utility.convertToString(orderBookRequest);

            jedis.lpush("exchange1-orderrequest",requestString);
            jedis.lpush("exchange2-orderrequest",requestString);
        }
    }
}
