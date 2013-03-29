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
 * Serveur linda. Gère un {@link CentralizedLinda} et les requêtes des clients.
 *
 * @author Alexandra Jacquet
 * @author Florian Vetu
 */
public class LindaServer extends UnicastRemoteObject implements LindaRMI {

    /**
     * Le {@link CentralizedLinda} associé au serveur
     */
    public Linda linda;

    /**
     * Initialize le linda server avec un {@link CentralizedLinda}.
     *
     * @throws RemoteException
     */
    public LindaServer() throws RemoteException {
        this.linda = new CentralizedLinda();
    }

    /**
     * Effectue un write sur le {@link CentralizedLinda}.
     *
     * @param t le tuple à écrire
     * @throws RemoteException
     * @see CentralizedLinda
     */
    @Override
    public void write(Tuple t) throws RemoteException {
        this.linda.write(t);
    }

    /**
     * Effectue un take sur le {@link CentralizedLinda}. Bloquant si aucun tuple
     * ne correspond.
     *
     * @param template le template du tuple à prendre.
     * @return le tuple pris.
     * @throws RemoteException
     * @see CentralizedLinda
     */
    @Override
    public Tuple take(Tuple template) throws RemoteException {
        return this.linda.take(template);
    }

    /**
     * Effectue un read sur le {@link CentralizedLinda}. Bloquant si aucun tuple
     * ne correspond.
     *
     * @param template le template du tuple à lire.
     * @return le tuple lut.
     * @throws RemoteException
     * @see CentralizedLinda
     */
    @Override
    public Tuple read(Tuple template) throws RemoteException {
        return this.linda.read(template);
    }

    /**
     * Effectue le tryTake sur le {@link CentralizedLinda}.
     *
     * @param template le template du tuple à prendre.
     * @return le tuple pris ou null si aucun tuple ne correspond.
     * @throws RemoteException
     * @see CentralizedLinda
     */
    @Override
    public Tuple tryTake(Tuple template) throws RemoteException {
        return this.linda.tryTake(template);
    }

    /**
     * Effectue le tryRead sur le {@link CentralizedLinda}.
     *
     * @param template le template du tuple à lire.
     * @return le tuple lut ou null si aucun tuple ne correspond.
     * @throws RemoteException
     * @see CentralizedLinda
     */
    @Override
    public Tuple tryRead(Tuple template) throws RemoteException {
        return this.tryRead(template);
    }

    /**
     * Effectue un takeAll sur le {@link CentralizedLinda}.
     *
     * @param template le template des tuples à prendre.
     * @return une collection de tuple correspondant au template, collection
     * vide si aucun.
     * @throws RemoteException
     * @see CentralizedLinda
     */
    @Override
    public Collection<Tuple> takeAll(Tuple template) throws RemoteException {
        return this.linda.takeAll(template);
    }

    /**
     * Effectue un readAll sur le {@link CentralizedLinda}.
     *
     * @param template le template des tuples à lire.
     * @return une collection de tuple correspondant au template, collection
     * vide si aucun
     * @throws RemoteException
     * @see CentralizedLinda
     */
    @Override
    public Collection<Tuple> readAll(Tuple template) throws RemoteException {
        return this.linda.readAll(template);
    }

    /**
     * Retourne le tuple d'un évènement. Pas de notion de callback ici.
     * Attention, bloquant tant que l'évènement n'a pas eu lieu.
     *
     * @param mode le mode de l'évènement (take ou read).
     * @param timing le timind de l'évènement (immédiat ou futur).
     * @param template le template du tuple associé à l'évènement.
     * @return le tuple trouvé lors de l'évènement.
     * @throws RemoteException
     */
    @Override
    public Tuple waitEvent(eventMode mode, eventTiming timing, Tuple template) throws RemoteException {
        System.out.println("Enregistrement d'un Event");
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        TupleBack tb = new TupleBack(lock, cond);
        this.linda.eventRegister(mode, timing, template, new AsynchronousCallback(tb));
        // prend le verrou
        tb.lock();
        try {
            // met en attente
            tb.await();
        } catch (InterruptedException ex) {

            Logger.getLogger(CentralizedLinda.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // debloque le verrou
            tb.unlock();
        }
        return tb.getTuple();
    }

    /**
     * Appel le debug sur le {@link CentralizedLinda}.
     * @param prefix le préfix du débug.
     * @see CentralizedLinda
     */
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
