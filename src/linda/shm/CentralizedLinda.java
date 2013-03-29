package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/**
 * Shared memory implementation of Linda.
 *
 * @author Alexandra Jacquet
 * @author Florian Vetu
 */
public class CentralizedLinda implements Linda {

    /**
     * La liste des tuples en mémoire
     */
    private List<Tuple> memory;
    /**
     * La liste des évènements take
     */
    private List<Event> registryTake;
    /**
     * La liste des évènements read
     */
    private List<Event> registryRead;

    /**
     * Crée la mémoire et les registres.
     */
    public CentralizedLinda() {
        this.memory = new CopyOnWriteArrayList<>();
        this.registryRead = new CopyOnWriteArrayList<>();
        this.registryTake = new CopyOnWriteArrayList<>();
    }

    /**
     * Procédure write. Appel les callbacks en mode read qui match le tuple
     * passé en paramètre. Appel le plus ancien callback en mode take qui match
     * le tuple passé en paramètre. Enlève les callbacks appelés des registres.
     * Si aucun callback en mode take n'a été appelé, enregistre le tuple en
     * mémoire.
     *
     * @param t le tuple à écrire en mémoire
     * @see Tuple
     */
    @Override
    public void write(Tuple t) {
        boolean taken;

        // pour tous les  évènements en mode read du registre
        for (Event readEvent : this.registryRead) {
            // si le tuple à écrire correspond au template associé à l'évènement
            if (readEvent.isMatching(t)) {
                // appel du callback de l'évènement
                readEvent.call(t);
                // enlève l'évènement du registre
                this.registryRead.remove(readEvent);
            }
        }

        taken = false;
        Event takeEvent;

        Iterator<Event> itEvent = this.registryTake.iterator();
        // tant qu'il y a un évènement en mode take et que le tuple n'a pas été pris
        while (itEvent.hasNext() && !taken) {
            takeEvent = itEvent.next();
            // si le tuple à écrire correspond au template associé à l'évènement
            if (takeEvent.isMatching(t)) {
                // appel du callback de l'évènement
                takeEvent.call(t);
                // enlève l'évènement du registre
                this.registryTake.remove(takeEvent);
                // on signifie qu'on a consommé le tuple pour arrêter de chercher
                taken = true;
            }
        }

        // si le tuple n'a pas été consommé, on ajoute le tuple dans la mémoire partagée
        if (!taken) {
            this.memory.add(t);
        }
    }

    /**
     * Fonction take. réalise un take sur un tuple s'il existe dans la mémoire
     * sinon se met en attente jusqu'à avoir un tuple de disponible
     *
     * @param template le template du take que l'on veut faire
     * @return le tuple trouvé en mémoire correspondant au template
     * @see Tuple
     */
    @Override
    public Tuple take(Tuple template) {
        // essai du take sur le template
        Tuple tuple = this.tryTake(template);
        // si un tuple en mémoire correspond au template
        if (tuple != null) {
            // fin de la procédure, on retourne le tuple
            return tuple;
        }
        // si aucun tuple ne correspond au template
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        TupleBack tb = new TupleBack(lock, cond);
        // enregistrement du take en attente 
        this.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template, tb);
        // prend le verrou
        lock.lock();
        try {
            // met en attente
            cond.await();
        } catch (InterruptedException ex) {

            Logger.getLogger(CentralizedLinda.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // debloque le verrou
            lock.unlock();
        }
        // débloqué par un signal (si on a fait un write correspondant)
        // renvoi le tuple qui a été pris et appelé dans le callback
        return tb.getTuple();
    }

    /**
     * Fonction read. Réalise un read sur un tuple s'il existe dans la mémoire
     * sinon se met en attente de l'écriture d'un tuple correspondant
     *
     * @param template le template du tuple que l'on veut read
     * @return le tuple trouvé.
     * @see Tuple
     */
    @Override
    public Tuple read(Tuple template) {
        // essai du read
        Tuple tuple = this.tryRead(template);
        // si un tuple correspond
        if (tuple != null) {
            // renvoi le tuple
            return tuple;
        }
        // si aucun tuple ne correspond pas
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        TupleBack tb = new TupleBack(lock, cond);
        // enregistrement du read en attente
        this.eventRegister(eventMode.READ, eventTiming.FUTURE, template, tb);
        // prend le verrou
        lock.lock();
        try {
            // met en attente
            cond.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(CentralizedLinda.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // libère le verrou
            lock.unlock();
        }
        System.out.println("I read : " + tb.getTuple().toString());
        // débloqué par un signal (si on a fait un write correspondant)
        // renvoi le tuple qui a été pris et appelé dans le callback
        return tb.getTuple();
    }

    /**
     * Fonction tryTake. Fait un take non bloquant.
     *
     * @param template le template du tuple que l'on souhaite prendre
     * @return le tuple trouvé en mémoire correspondant au template. Null si
     * aucun tuple trouvé
     */
    @Override
    public Tuple tryTake(Tuple template) {
        synchronized (this.memory) {
            // pour chaque tuple en mémoire partagée
            for (Tuple tuple : this.memory) {
                // si le tuple courant correspond 
                if (tuple.matches(template)) {
                    // enlève le premier tuple trouvé de la mémoire
                    this.memory.remove(tuple);
                    // retourne le tuple
                    return tuple;
                }
            }
        }
        // si aucun tuple n'a pas été trouvé : renvoi null
        return null;
    }

    /**
     * Fonction tryRead. Fait un read non bloquant.
     *
     * @param template le template du read que l'on veut faire
     * @return le tuple trouvé en mémoire correspondant au template. Null si
     * aucun tuple trouvé
     */
    @Override
    public Tuple tryRead(Tuple template) {
        // pour chaque tuple de la mémoire
        for (Tuple tuple : this.memory) {
            // si le tuple courrant correspond au template
            if (tuple.matches(template)) {
                // retourne le premier tuple trouvé 
                System.out.println("I try read : " + tuple.toString());
                return tuple;
            }
        }
        // si aucun tuple n'a pas été trouvé : renvoi null
        return null;
    }

    /**
     * Fonction takeAll. Recupère tous les tuples correspondants au template et
     * les enlève de la mémoire partagée.
     *
     * @param template le template du take que l'on veut faire
     * @return une collection de Tuple trouvés en mémoire correspondant au
     * template. Vide si aucun tuple n'a été trouvé.
     * @see Tuple
     */
    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        Tuple tuple;
        Collection<Tuple> list = new ArrayList();
        while ((tuple = tryTake(template)) != null) {
            list.add(tuple);
        }
        System.out.println("I take all : " + list.toString());
        return list;
    }

    /**
     * Fonction readAll. Recupère tous les tuples correspondants au template et
     * les laisse dans la mémoire partagée.
     *
     * @param template le template du read que l'on veut faire
     * @return une collection de tuple trouvés en mémoire correspondant au
     * template. Vide si aucun tuple n'a été trouvé
     * @see Tuple
     */
    @Override
    public Collection<Tuple> readAll(Tuple template) {
        Collection<Tuple> list = new ArrayList();
        for (Tuple tuple : this.memory) {
            if (tuple.matches(template)) {
                list.add(tuple);
            }
        }
        System.out.println("I read all : " + list.toString());
        return list;
    }

    /**
     * Procédure eventRegister. Enregistre les évenements dans la liste des
     * registres s'ils sont en attente. Sinon execute le mode demandé si
     * possible.
     *
     * @param mode le mode de l'évènement (read ou take)
     * @param timing le timing de l'évènement (immédiat ou futur)
     * @param template le template du tuple à chercher
     * @param callback le callback a appeler lors de l'évènement
     * @see Tuple
     */
    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        // si c'est un évenement immédiat
        if (timing.equals(eventTiming.IMMEDIATE)) {
            // si c'est un read
            if (mode.equals(eventMode.READ)) {
                // essai d'un read
                Tuple tuple = this.tryRead(template);
                if (tuple == null) {
                    // si aucun tuple à lire n'a été trouvé en mémoire : ajout de l'évenement en attente au registre
                    this.registryRead.add(new Event(template, callback));
                } else {
                    // sinon appel le callback associé à l'évènement 
                    callback.call(tuple);
                }
            } else {
                // si c'est un take
                // essai d'un take sur la mémoire partagée
                Tuple tuple = this.tryTake(template);
                if (tuple == null) {
                    // si aucun tuple n'a été trouvé : enregistrement de l'evenement dans le registre des take
                    this.registryTake.add(new Event(template, callback));
                } else {
                    // sinon appel le callback associé à l'évènement
                    callback.call(tuple);
                }
            }
        } else {
            // si c'est un évenement futur
            if (mode.equals(eventMode.READ)) {
                // si c'est un read : enregistrement dans le registre des read en attente
                this.registryRead.add(new Event(template, callback));
            } else {
                // si c'est un take : enregistrement dans le registre des take en attente
                this.registryTake.add(new Event(template, callback));
            }
        }
        System.out.println("I registred : " + mode.name() + " " + template.toString());
    }

    /**
     * To debug, prints any information it wants (e.g. the tuples in tuplespace
     * or the registered callbacks), prefixed by
     * <code>prefix</code.
     */
    @Override
    public void debug(String prefix) {
        System.out.println("Debug " + prefix + " : " + this.memory.toString());
    }
}
