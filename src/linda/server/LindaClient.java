package linda.server;

import java.util.Collection;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        // TO BE COMPLETED
    }
    
    // TO BE COMPLETED

    @Override
    public void write(Tuple t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Tuple take(Tuple template) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Tuple read(Tuple template) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Tuple tryTake(Tuple template) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Tuple tryRead(Tuple template) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void debug(String prefix) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
