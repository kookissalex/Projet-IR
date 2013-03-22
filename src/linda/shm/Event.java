/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package linda.shm;

import linda.Callback;
import linda.Tuple;

/**
 *
 * @author Zanatoshi
 */
public class Event {
    
    private Tuple motif;
    private Callback callback;

    public Event(Tuple motif, Callback callback) {
        this.motif = motif;
        this.callback = callback;
    }
    
    public boolean isMatching(Tuple tuple)
    {
        return tuple.matches(this.motif);
    }

    public void call(Tuple t) {
        this.callback.call(t);
    }
    
}
