/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package linda.shm;

import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import linda.Callback;
import linda.Tuple;

/**
 *
 * @author Zanatoshi
 */
public class TupleBack implements Callback, Serializable {

    private Lock lock;
    private Tuple tuple;
    private Condition cond;
    public TupleBack(Lock l, Condition cond) {
        this.lock = l;
        this.cond = cond;
    }

    @Override
    public void call(Tuple t) 
    {
        System.out.println("I was called with : " + t.toString());
        this.tuple = t;
        this.lock.lock();
        try{
            this.cond.signal();
        }
        finally
        {
            this.lock.unlock();
        }
    }

    public Tuple getTuple() {
        return tuple;
    }

    public void lock() {
        this.lock.lock();
    }

    public void await() throws InterruptedException {
        this.cond.await();
    }

    public void unlock() {
        this.lock.unlock();
    }
}
