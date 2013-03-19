package linda.shm;

import java.util.Collection;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
    public CentralizedLinda() {
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
