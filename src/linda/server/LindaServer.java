/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package linda.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import linda.AsynchronousCallback;
import linda.Linda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.Tuple;
import linda.shm.CentralizedLinda;
import linda.shm.TupleBack;

/**
 *
 * @author Zanatoshi
 */
public class LindaServer extends UnicastRemoteObject implements LindaRMI {

    public Linda linda;
    
    public LindaServer() throws RemoteException {
        this.linda = new CentralizedLinda();
    }

    @Override
    public void write(Tuple t) throws RemoteException  {
        this.linda.write(t);
    }

    @Override
    public Tuple take(Tuple template) throws RemoteException  {
        return this.linda.take(template);
    }

    @Override
    public Tuple read(Tuple template) throws RemoteException  {
        return this.linda.read(template);
    }

    @Override
    public Tuple tryTake(Tuple template) throws RemoteException  {
        return this.linda.tryTake(template);
    }

    @Override
    public Tuple tryRead(Tuple template) throws RemoteException  {
        return this.tryRead(template);
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) throws RemoteException  {
        return this.linda.takeAll(template);
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) throws RemoteException  {
        return this.linda.readAll(template);
    }

    @Override
    public Tuple waitEvent(eventMode mode, eventTiming timing, Tuple template) throws RemoteException  {
        System.out.println("Enregistrement d'un Event");
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        TupleBack tb = new TupleBack(lock,cond);
        this.linda.eventRegister(mode, timing, template, new AsynchronousCallback(tb));
        // prend le verrou
        tb.lock();
        try {
            // met en attente
            tb.await();
        } catch (InterruptedException ex) {

            Logger.getLogger(CentralizedLinda.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            // debloque le verrou
            tb.unlock();
        }
        return tb.getTuple();
    }

    @Override
    public void debug(String prefix) {
        this.linda.debug(prefix);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            LindaServer server = new LindaServer();
            Registry rs = LocateRegistry.createRegistry(8080);
            rs.rebind("linda", server);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
