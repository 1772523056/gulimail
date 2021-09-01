package thread;

import java.security.PublicKey;
import java.util.concurrent.*;

public class ThreadTest {
    public static ExecutorService executorService = Executors.newFixedThreadPool(10);

//    public static void main(String[] args) {
//        System.out.println("主线程开始");
//        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getName());
//            int i = 10 / 4;
//            System.out.println("计算结果" + i);
//            return i;
//        }, executorService).thenRunAsync(() -> System.out.println("串行的线程" + Thread.currentThread().getName()), Executors.newFixedThreadPool(10))
//                .thenRunAsync(() -> System.out.println("串行的线程" + Thread.currentThread().getName()))
//                .thenRun(() -> System.out.println("串行的线程" + Thread.currentThread().getName()));
//        System.out.println("主线程结束");
//    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            System.out.println("1商品信息查询玩");
        }, executorService);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            System.out.println("2商品信息查询玩");
        }, executorService);

        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("3商品信息查询玩");
        }, executorService);

        CompletableFuture.allOf(future1,future2,future3).get();
        System.out.println("所有都执行完");
    }
}
