package linda.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;
import linda.shm.TupleBack;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {
    
    private LindaRMI linda;
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        try {
            System.out.println("LindaServer URI : " + serverURI);
            this.linda = (LindaRMI)Naming.lookup(serverURI);
        } catch (NotBoundException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // TO BE COMPLETED

    @Override
    public void write(Tuple t) {
        try {
            this.linda.write(t);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            return this.linda.take(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            return this.linda.read(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return this.linda.tryTake(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return this.linda.tryRead(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return this.linda.takeAll(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return this.linda.readAll(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void eventRegister(final eventMode mode, final eventTiming timing, final Tuple template, final Callback callback) {
        new Thread(){
            public void run(){
                System.out.println("Callback waiting : " + template);
                
                try {
                    Tuple tuple = linda.waitEvent(mode, timing, template);
                    // débloqué par un signal (si on a fait un write correspondant)
                    // renvoi le tuple qui a été pris et appelé dans le callback
                    callback.call(tuple);
                } catch (RemoteException ex) {
                    Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Callback done : " + template);
            }
        }.start();
    }

    @Override
    public void debug(String prefix) {
        try {
            this.linda.debug(prefix);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LindaClient client = new LindaClient("rmi://127.0.0.1:4000/server");
    }  

}
