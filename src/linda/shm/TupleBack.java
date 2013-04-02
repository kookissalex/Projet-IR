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
 * Callback utilisé. Stock un verrou et une condition associée. Récupère et
 * stock le tuple trouvé.
 *
 * @author Alexandra Jacquet
 * @author Florian Vetu
 */
public class TupleBack implements Callback, Serializable {

    private Lock lock;
    private Tuple tuple;
    private Condition cond;

    /**
     * Crée le TupleBack.
     *
     * @param l le verrou à débloquer.
     * @param cond la condition à signaler.
     */
    public TupleBack(Lock l, Condition cond) {
        this.lock = l;
        this.cond = cond;
    }

    /**
     * Le call. Stocke le tuple, signal la condition et libère le verrou.
     *
     * @param t le tuple trouvé.
     * @see Tuple
     */
    @Override
    public void call(Tuple t) {
        System.out.println("I was called with : " + t.toString());
        this.tuple = t;
        this.lock.lock();
        try {
            this.cond.signal();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     *
     * @return le tuple trouvé.
     */
    public Tuple getTuple() {
        return tuple;
    }

    /**
     * Prend le verrou.
     */
    public void lock() {
        this.lock.lock();
    }

    /**
     * Await sur le verrou.
     *
     * @throws InterruptedException
     */
    public void await() throws InterruptedException {
        this.cond.await();
    }

    /**
     * Libère le verrou.
     */
    public void unlock() {
        this.lock.unlock();
    }
}
