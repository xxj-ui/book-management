package executor;

import io.netty.util.concurrent.SingleThreadEventExecutor;
import jodd.time.TimeUtil;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


public class TicketDemo{
    static Object lock=new Object();
    int ticketNum=10;
    public void getTicket(){

        synchronized (lock){
            if (ticketNum<=0){
                return ;
            }
            System.out.println(Thread.currentThread().getId()+"  "+ticketNum);
            ticketNum--;

        }
    }


    public static void main(String[] args) {
       TicketDemo ticketDemo = new TicketDemo();
        for (int i = 0; i < 20; i++) {
            new Thread(()->{
                ticketDemo.getTicket();
            }).start();
        }

        ReentrantLock lock1 = new ReentrantLock();
        lock1.unlock();


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,3,0, TimeUnit.MICROSECONDS,
                new ArrayBlockingQueue<>(2),
                new ThreadPoolExecutor.DiscardPolicy()
        );


        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put()

    }
}