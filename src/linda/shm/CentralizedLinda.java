package linda.shm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    private List<Tuple> memory;
    
    private List<Event> registryTake;
    private List<Event> registryRead;

    public CentralizedLinda() {
        this.memory = Collections.synchronizedList(new ArrayList<Tuple>());
        this.registryRead = Collections.synchronizedList(new ArrayList<Event>());
        this.registryTake = Collections.synchronizedList(new ArrayList<Event>());
    }

    // TO BE COMPLETED

    @Override
    public void write(Tuple t) {
        synchronized (this.registryRead)
        {
            for(Event readEvent : this.registryRead)
            {
                if(readEvent.isMatching(t))
                {
                    readEvent.call(t);
                    this.registryRead.remove(readEvent);
                }
            }
        }
        boolean taken = false;
        synchronized (this.registryTake)
        {
            for(Event takeEvent : this.registryTake)
            {
                if(takeEvent.isMatching(t) && !taken)
                {
                    takeEvent.call(t);
                    this.registryTake.remove(takeEvent);
                    taken = true;
                }
            }
        }
        if(!taken)
        {
            synchronized(this.memory)
            {
                this.memory.add(t);
            }
        }
    }

    @Override
    public Tuple take(Tuple template) {
        Tuple tuple = this.tryTake(template);
        if(tuple != null)
        {
            return tuple;
        }
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        TupleBack tb = new TupleBack(lock,cond);
        this.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template, tb);
        lock.lock();
        try {
            cond.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(CentralizedLinda.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            lock.unlock();
        }
        return tb.getTuple();
    }

    @Override
    public Tuple read(Tuple template) {
        Tuple tuple = this.tryRead(template);
        if(tuple != null)
        {
            return tuple;
        }
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        TupleBack tb = new TupleBack(lock,cond);
        this.eventRegister(eventMode.READ, eventTiming.FUTURE, template, tb);
        lock.lock();
        try {
            cond.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(CentralizedLinda.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            lock.unlock();
        }
        System.out.println("I read : " + tb.getTuple().toString());
        return tb.getTuple();
    }

    @Override
    public Tuple tryTake(Tuple template) {
        synchronized(this.memory)
        {
            for(Tuple tuple : this.memory)
            {
                if (tuple.matches(template))
                {
                    this.memory.remove(tuple);
                    return tuple;
                }
            }
        }
        return null;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        synchronized(this.memory)
        {
            for(Tuple tuple : this.memory)
            {
                if (tuple.matches(template))
                {
                    System.out.println("I try read : " + tuple.toString());
                    return tuple;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        if(timing.equals(eventTiming.IMMEDIATE))
        {
            if(mode.equals(eventMode.READ))
            {
                Tuple tuple = this.tryRead(template);
                if(tuple == null)
                {
                    synchronized(this.registryRead)
                    {
                        this.registryRead.add(new Event(template,callback));
                    }
                }
                else
                {
                    callback.call(tuple);
                }
            }
            else
            {
                Tuple tuple = this.tryTake(template);
                if(tuple == null)
                {
                    synchronized(this.registryTake)
                    {
                        this.registryTake.add(new Event(template, callback));
                    }
                }
                else
                {
                    callback.call(tuple);
                }
            }
        }
        else
        {
            if(mode.equals(eventMode.READ))
            {
                synchronized(this.registryRead)
                {
                    this.registryRead.add(new Event(template,callback));
                }
            }
            else
            {
                synchronized(this.registryTake)
                {
                    this.registryTake.add(new Event(template,callback)); 
                }
            }
        }
        System.out.print("I registred : " + template.toString());
    }

    @Override
    public void debug(String prefix) {
        synchronized(this.memory)
        {
            System.out.println("Debug " + prefix + " : " + this.memory.toString());
        }
    }

}
