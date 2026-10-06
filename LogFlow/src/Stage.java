public interface Stage {
    void process(String item, Emitter emitter) throws Exception;
}