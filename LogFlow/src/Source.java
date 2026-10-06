public interface Source<O> {
    void produce(Emitter<O> out) throws Exception;
}