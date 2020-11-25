package turntabl.io.tradeEngine.taskQueue;

import redis.clients.jedis.Jedis;
import turntabl.io.tradeEngine.Utility;
import turntabl.io.tradeEngine.model.ClientOrder;
import turntabl.io.tradeEngine.model.PendingOrder;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MonitorQueue implements Runnable{
    Jedis jedis = new Jedis();
    @Override
    public void run() {
        while (true){
            String data = jedis.rpop("monitorqueue");
            if(data == null) continue;

            if(jedis.llen(data+"orderbook") == 2){


                System.out.println("Monitor queue");
                PendingOrder[] pendingOrderList1 = Utility.convertToObject(jedis.rpop(data+"orderbook"),PendingOrder[].class);
                PendingOrder[] pendingOrderList2 = Utility.convertToObject(jedis.rpop(data+"orderbook"),PendingOrder[].class);


                List<PendingOrder> pendingOrderList = new ArrayList<>();

                pendingOrderList.addAll(Arrays.asList(pendingOrderList1));
                pendingOrderList.addAll(Arrays.asList(pendingOrderList2));



//						Sorting orderbook
                ClientOrder clientOrder = Utility.convertToObject( jedis.get(data), ClientOrder.class);

                System.out.println("before sorting: " +pendingOrderList);


                double clientOrderPrice = clientOrder.getPrice();

                if (clientOrder.getSide().toLowerCase().equals("buy")){
                    pendingOrderList = pendingOrderList.stream()
                            .filter(price->price.getPrice() > clientOrderPrice)
                            .collect(Collectors.toList());
                    pendingOrderList.sort(Comparator.comparing(PendingOrder::getPrice));
                    System.out.println("OrderList: " +pendingOrderList);
                }else {
                    pendingOrderList =pendingOrderList.stream()
                            .filter(price->price.getPrice() > clientOrderPrice)
                            .collect(Collectors.toList());
                    pendingOrderList.sort(Comparator.comparing(PendingOrder::getPrice).reversed());
                    System.out.println("OrderList: " +pendingOrderList);
                }

//                System.out.println("after sorting: " +pendingOrderList);

                int clientOrderQuantity = clientOrder.getQuantity();

                for (PendingOrder pendingOrder : pendingOrderList) {
                    int availableOrderQuantity = pendingOrder.getQuantity() - pendingOrder.getCumulatitiveQuantity();

                    if (clientOrderQuantity == availableOrderQuantity && pendingOrder.getExchange().equals("exchange1")){
                        jedis.lpush("makeorder"+"exchange1",Utility.convertToString(clientOrder));
                        clientOrderQuantity = 0;
                        break;
                    }else if (clientOrderQuantity == availableOrderQuantity && pendingOrder.getExchange().equals("exchange2")){
                        jedis.lpush("makeorder"+"exchange2",Utility.convertToString(clientOrder));
                        clientOrderQuantity = 0;
                        break;
                    }else if(clientOrderQuantity >= availableOrderQuantity && pendingOrder.getExchange().equals("exchange1") ){
                        jedis.lpush("makeorder"+"exchange1",Utility.convertToString(clientOrder));
                        clientOrderQuantity -= availableOrderQuantity;
                    }else if(clientOrderQuantity >= availableOrderQuantity && pendingOrder.getExchange().equals("exchange2") ){
                        jedis.lpush("makeorder"+"exchange2",Utility.convertToString(clientOrder));
                        clientOrderQuantity -= availableOrderQuantity;
                    } else if(clientOrderQuantity <= availableOrderQuantity && pendingOrder.getExchange().equals("exchange1") ){
                        jedis.lpush("makeorder"+"exchange1",Utility.convertToString(clientOrder));
                        clientOrderQuantity = 0;
                        break;
                    }else if(clientOrderQuantity <= availableOrderQuantity && pendingOrder.getExchange().equals("exchange2") ){
                        jedis.lpush("makeorder"+"exchange2",Utility.convertToString(clientOrder));
                        clientOrderQuantity = 0;
                        break;
                    } else if (clientOrderQuantity != 0 && availableOrderQuantity == 0){
                        jedis.lpush("makeorder"+"exchange1",Utility.convertToString(clientOrder));
                        jedis.lpush("makeorder"+"exchange2",Utility.convertToString(clientOrder));
                    }

                }

                jedis.del(data);
            }else{
                jedis.lpush("monitorqueue",data);
            }
        }
    }
}
