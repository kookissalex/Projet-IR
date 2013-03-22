/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package linda.shm;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zanatoshi
 */
public class WaitingBack implements Runnable{
    
    private boolean waiting;

    public WaitingBack() {
        this.waiting = true;
    }
    
    @Override
    public void run() {
        while(waiting)
        {
        }
    }
    
    
}
