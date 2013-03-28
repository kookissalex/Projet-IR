package linda.shm;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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

    // liste des tuples en mémoire
    private List<Tuple> memory;
    
    // liste des take qui n'ont pas pu se réaliser immédiatement
    private List<Event> registryTake;
    // liste des read qui n'ont pas pu se réaliser immédiatement
    private List<Event> registryRead;

    // Constructeur
    public CentralizedLinda() 
    {
        this.memory = new CopyOnWriteArrayList<>();
        this.registryRead = new CopyOnWriteArrayList<>();
        this.registryTake = new CopyOnWriteArrayList<>();
    }

    // procédure write :
    // écrit un tuple dans la mémoire si un take en attente n'en a pas besoin
    // écrit un tuple dans la mémoire et peut être lu si des read en attente correspondent
    // param IN : Tuple t : tuple à écrire en mémoire
    @Override
    public void write(Tuple t) 
    {
        boolean taken;

        
        // pour tous les  éléments de la liste des read en attente
        for(Event readEvent : this.registryRead)
        {
            // si le tuple à écrire correspond au read en attente
            if(readEvent.isMatching(t))
            {
                // read du tuple à écrire
                readEvent.call(t);
                // enlève le read effectué de la liste des read en attente
                this.registryRead.remove(readEvent);
            }
        }
        
        taken = false;
        
        Event takeEvent; 
        
        Iterator<Event> itEvent = this.registryTake.iterator();
        // tant qu'il y a un take en attente et que le tuple n'a pas été pris
         while ( itEvent.hasNext() && !taken )
         {
             takeEvent = itEvent.next();
              // si le tuple à écrire correspond au take en attente et qu'il n'a pas été pris auparavant
            if(takeEvent.isMatching(t))
            {
                // take du tuple
                takeEvent.call(t);
                // enlève le take de la liste des take en attente
                this.registryTake.remove(takeEvent);
                // arret du parcour des take
                taken = true;
            }
        }
      
         // si le tuple n'a pas été take, on ajoute le tuple dans la mémoire partagée
        if(!taken)
        {
            this.memory.add(t);
        }
    }

    
    // fonction take :
    // réalise un take sur un tuple s'il existe dans la mémoire
    // sinon se met en attente
    // param IN : Tuple template : template du take que l'on veut faire
    // param OUT : Tuple trouvé en mémoire correspondant au template
    @Override
    public Tuple take(Tuple template) 
    {
        // essai du take sur le template
        Tuple tuple = this.tryTake(template);
        // si un tuple en mémoire correspond au template
        if(tuple != null)
        {
            // fin de la procédure, on retourne le tuple
            return tuple;
        }
        // si aucun tuple ne correspond au template
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        TupleBack tb = new TupleBack(lock,cond);
        // enregistrement du take en attente 
        this.eventRegister(eventMode.TAKE, eventTiming.FUTURE, template, tb);
        // prend le verrou
        lock.lock();
        try {
            // met en attente
            cond.await();
        } catch (InterruptedException ex) {
            
            Logger.getLogger(CentralizedLinda.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            // debloque le verrou
            lock.unlock();
        }
        // débloqué par un signal (si on a fait un write correspondant)
        // renvoi le tuple qui a été pris et appelé dans le callback
        return tb.getTuple();
    }

    // fonction read : 
    // réalise un read sur un tuple s'il existe dans la mémoire
    // sinon se met en attente
    // param IN : Tuple template : template du read que l'on veut faire
    // param OUT : Tuple trouvé en mémoire correspondant au template 
    @Override
    public Tuple read(Tuple template) 
    {
        // essai du read
        Tuple tuple = this.tryRead(template);
        // si un tuple correspond
        if(tuple != null)
        {
            // renvoi le tuple
            return tuple;
        }
        // si aucun tuple ne correspond pas
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        TupleBack tb = new TupleBack(lock,cond);
        // enregistrement du read en attente
        this.eventRegister(eventMode.READ, eventTiming.FUTURE, template, tb);
        lock.lock();
        try 
        {
            cond.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(CentralizedLinda.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            lock.unlock();
        }
        System.out.println("I read : " + tb.getTuple().toString());
        // débloqué par un signal (si on a fait un write correspondant)
        // renvoi le tuple qui a été pris et appelé dans le callback
        return tb.getTuple();
    }

    // fonction tryTake
    // Fait un take non bloquant.
    // param IN : Tuple template : template du take que l'on veut faire
    // param OUT : Tuple trouvé en mémoire correspondant au template. Null si aucun tuple trouvé
    @Override
    public Tuple tryTake(Tuple template) 
    {
        // pour chaque tuple en mémoire partagée
        for(Tuple tuple : this.memory)
        {
            // si le tuple courant correspond 
            if (tuple.matches(template))
            {
                // enlève le premier tuple trouvé de la mémoire
                this.memory.remove(tuple);
                // retourne le tuple
                return tuple;
            }
        }
        // si aucun tuple n'a pas été trouvé : renvoi null
        return null;
    }

    
    // fonction tryRead
    // Fait un read non bloquant.
    // param IN : Tuple template : template du read que l'on veut faire
    // param OUT : Tuple trouvé en mémoire correspondant au template. Null si aucun tuple trouvé
    @Override
    public Tuple tryRead(Tuple template) 
    {
        // pour chaque tuple de la mémoire
        for(Tuple tuple : this.memory)
        {
            // si le tuple courrant correspond au template
            if (tuple.matches(template))
            {
                // retourne le premier tuple trouvé 
                System.out.println("I try read : " + tuple.toString());
                return tuple;
            }
        }
         // si aucun tuple n'a pas été trouvé : renvoi null
        return null;
    }

    // fonction takeAll :
    // Recupère tous les tuples correspondants au template et les enlève de la mémoire partagée
    // param IN : Tuple template : template du take que l'on veut faire
    // param OUT : collection Tuple trouvés en mémoire correspondant au template. 
    //              Vide si aucun tuple n'a été trouvé
    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

        
    // fonction readAll :
    // Recupère tous les tuples correspondants au template et les laisse dans la mémoire partagée
    // param IN : Tuple template : template du read que l'on veut faire
    // param OUT : collection Tuple trouvés en mémoire correspondant au template. 
    //              Vide si aucun tuple n'a été trouvé
    @Override
    public Collection<Tuple> readAll(Tuple template) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // procédure eventRegister :
    // Enregistre les évenements dans la liste des registres s'ils sont en attente. Sinon execute le mode demandé si possible.
    // param IN : mode : mode d'evenement (read ou take), timing : immédiat ou futur
    //          template : template du tuple à chercher, callback : callback qui va réactiver le take ou le read
    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        // si c'est un évenement immédiat
        if(timing.equals(eventTiming.IMMEDIATE))
        {
            // si c'est un read
            if(mode.equals(eventMode.READ))
            {
                // essai d'un read
                Tuple tuple = this.tryRead(template);
                if(tuple == null)
                {
                    // si aucun tuple à lire n'a été trouvé en mémoire : ajout de l'évenement en attente au registre
                    this.registryRead.add(new Event(template,callback));
                }
                else
                {
                    // sinon débloque le read qui était en attente 
                    callback.call(tuple);
                }
            }
            else
            {
                // si c'est un take
                // essai d'un take sur la mémoire partagée
                Tuple tuple = this.tryTake(template);
                if(tuple == null)
                {
                    // si aucun tuple n'a été trouvé : enregistrement de l'evenement dans le registre des take
                    this.registryTake.add(new Event(template, callback));
                }
                else
                {
                    // si un tuple a été trouvé : débloque le take qui était en attente
                    callback.call(tuple);
                }
            }
        }
        else
        {
            // si c'est un évenement futur
            
            if(mode.equals(eventMode.READ))
            {
                // si c'est un read : enregistrement dans le registre des read en attente
                this.registryRead.add(new Event(template,callback));
            }
            else
            {
                // si c'est un take : enregistrement dans le registre des take en attente
                this.registryTake.add(new Event(template,callback));
            }
        }
        System.out.println("I registred : " + mode.name() + " " + template.toString());
    }

    /** To debug, prints any information it wants (e.g. the tuples in tuplespace or the registered callbacks), prefixed by <code>prefix</code. */
    @Override
    public void debug(String prefix) {
        System.out.println("Debug " + prefix + " : " + this.memory.toString());
    }

}
