package edu.ecu.cs.seng6245.primes;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.ArrayList;

public class Primes {

    /** 
     * The queue holding candidates we want to check for primality.
     */
    private BlockingQueue<Integer> candidateQueue;
    
    /** 
     * The queue holding primes we want to print before inserting into the result set.
     */
    private BlockingQueue<Integer> primesQueue;

    /**
     * The set holding the numbers that we have determined are prime.
     */
    private Set<Integer> primeNumbers;
    
    /**
     * Create a new instance of the Primes checker program.
     */
    public Primes() {
        // TODO: If this the best type of BlockingQueue to use, and is this the best size?
        // Feel free to change both.
        candidateQueue = new ArrayBlockingQueue<>(10000);
        
        // TODO: If this the best type of BlockingQueue to use, and is this the best size?
        // Feel free to change both.
        primesQueue = new ArrayBlockingQueue<>(10000);

        // TODO: Is a HashSet the best option here, and are there any options that would
        // help make it perform better? Feel free to change to a different type of Set or
        // to add parameters to the constructor.
        primeNumbers = new HashSet<>();
    }

    /**
     * Actually run the primes finder, looking for primes between smallest and biggest (inclusive).
     * 
     * @param smallest the smallest number to check
     * @param biggest the biggest number to check
     * 
     * @return a {@link Set} containing the prime numbers we have found
     */

    /** Commit out original code to develop faster example
    public Set<Integer> findPrimesInRange(int smallest, int biggest) {
        // TODO: You should create the number generator and primes printer, as well
        // as some number of primality checkers. You should create these all as
        // threads that you can run to look for prime numbers. You should have at least
        // two instances of {@link PrimalityChecker}, but could have more if this makes
        // your program faster.
        
        // TODO: This is just here to make the compiler happy, you should return something real...
        Thread ngThread = new Thread(new NumberGenerator(smallest, biggest, candidateQueue));
        ngThread.start();
        Thread pcThread1 = new Thread(new PrimalityChecker(candidateQueue, primesQueue, 1));
        Thread pcThread2 = new Thread(new PrimalityChecker(candidateQueue, primesQueue, 2));
        pcThread1.start();
        pcThread2.start();
        Thread ppThread = new Thread(new PrimesPrinter(primesQueue, primeNumbers));
        ppThread.start();
        while(true){
            try {
                ngThread.join();
                pcThread1.join();
                pcThread2.join();
                ppThread.join();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return primeNumbers;
    }
     */

    public Set<Integer> findPrimesInRange(int smallest, int biggest) throws InterruptedException, ExecutionException {

        ExecutorService executor = Executors.newCachedThreadPool();

        ArrayList<PrimalityChecker> threadArray = new ArrayList();
        ArrayList<Future> threadFuture = new ArrayList();

        Runnable taskOne = new NumberGenerator(smallest, biggest, candidateQueue);


        int x;
        if (biggest <= 10){
            x=biggest;
        }else if (10 < biggest && biggest <= 100000){
            x= biggest/1000;
        }else{
            x=100;
        }

        for(int i=0; i<=x; i++){
            threadArray.add(new PrimalityChecker(candidateQueue,primesQueue,x));
        }
        /**used to test early use of PC
        //Runnable taskPC1 = new PrimalityChecker(candidateQueue, primesQueue, 1);
        //Runnable taskPC2 = new PrimalityChecker(candidateQueue, primesQueue, 2);
        */
        Runnable taskPP = new PrimesPrinter(primesQueue, primeNumbers);

        executor.execute(taskOne);
        for(int i = 0; i<threadArray.size(); i++){
            threadFuture.add(i,executor.submit(threadArray.get(i)));
        }
        /**used to early test execution of PC
        //executor.invokeAll(threadArray);
        //executor.execute(taskPC1);
        //executor.execute(taskPC2);
        */
        executor.execute(taskPP);

        for(int i=0; i<threadFuture.size();i++){
            if(threadFuture.get(i).get()!=null){
                i--;
            }
        }
        executor.shutdown();
        System.out.println("________________");
        executor.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println("All Tasks Are Finished");

        return primeNumbers;
    }

    
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Primes p = new Primes();
        // Remember, 1 is not prime! http://en.wikipedia.org/wiki/Prime_number
        long startTime = System.currentTimeMillis();
        p.findPrimesInRange(2, 50000000);
        long endTime = System.currentTimeMillis();
        System.out.println();
        System.out.println("There are "+p.primeNumbers.size()+" prime numbers");
        System.out.println("The process took "+(endTime-startTime)+" Milliseconds to complete");
        System.out.println("End of Program");
    }
}
