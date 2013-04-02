package linda.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/**
 * Client part of a client/server implementation of Linda. It implements the
 * Linda interface and propagates everything to the server it is connected to.
 *
 * @author Alexandra Jacquet
 * @author Florian Vetu
 */
public class LindaClient implements Linda {

    /**
     * Le {@link LindaServer}.
     */
    private LindaRMI linda;

    /**
     * Initializes the Linda implementation.
     *
     * @param serverURI the URI of the server, e.g.
     * "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        try {
            System.out.println("LindaServer URI : " + serverURI);
            this.linda = (LindaRMI) Naming.lookup(serverURI);
        } catch (NotBoundException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Procédure write. Appel la méthode write du {@link LindaServer}.
     *
     * @param t le tuple à écrire.
     * @see Tuple
     */
    @Override
    public void write(Tuple t) {
        try {
            this.linda.write(t);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Procédue take. Appel le take du {@link LindaServer}.
     *
     * @param template le template recherché.
     * @return le tuple trouvé.
     * @see Tuple
     */
    @Override
    public Tuple take(Tuple template) {
        try {
            return this.linda.take(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Procédure read. Appel le read du {@link LindaServer}.
     *
     * @param template le template recherché.
     * @return le tuple trouvé.
     * @see Tuple
     */
    @Override
    public Tuple read(Tuple template) {
        try {
            return this.linda.read(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Procédure trytake. Appel le tryTake du {@link LindaServer}.
     *
     * @param template le template recherché.
     * @return le tuple trouvé.
     * @see Tuple
     */
    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return this.linda.tryTake(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Procédure tryRead. Appel du tryRead du {@link LindaServer}.
     *
     * @param template le template recherché.
     * @return le tuple trouvé.
     * @see Tuple
     */
    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return this.linda.tryRead(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Procédure takeAll. Appel du takeAll du {@link LindaServer}.
     *
     * @param template le template recherché.
     * @return la collection de tuple trouvée.
     * @see Tuple
     */
    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return this.linda.takeAll(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Procédure readAll. Appel du readAll du {@link LindaServer}.
     *
     * @param template le template recherché.
     * @return la collection de tuple trouvée.
     * @see Tuple
     */
    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return this.linda.readAll(template);
        } catch (RemoteException ex) {
            Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Procédure eventRegister. Enregistre un évènement au près du serveur.
     * Appel le callback lorsque l'èvenement à eu lieu.
     *
     * @param mode le mode de l'évènement.
     * @param timing le timing de l'évènement.
     * @param template le template recherché.
     * @param callback le callback à appeler.
     * @see Tuple
     */
    @Override
    public void eventRegister(final eventMode mode, final eventTiming timing, final Tuple template, final Callback callback) {
        // Création d'un thread pour ne pas bloquer le client.
        new Thread() {

            @Override
            public void run() {
                System.out.println("Callback waiting : " + template);
                try {
                    // attend d'obtenir le tuple associé à l'évènement.
                    Tuple tuple = linda.waitEvent(mode, timing, template);
                    // appel du callback.
                    callback.call(tuple);
                } catch (RemoteException ex) {
                    Logger.getLogger(LindaClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Callback done : " + template);
            }
        }.start();
    }

    /**
     * Procédure debug. Appel du debug du {@link LindaServer}.
     *
     * @param prefix le préfix lors du debug.
     */
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
