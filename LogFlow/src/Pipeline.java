import java.util.ArrayList;
import java.util.List;

public class Pipeline<I, O> {
    private final Source<I> source;
    private final Sink<O> sink;
    private final List<Stage> stages = new ArrayList<>();

    public Pipeline(Source<I> source, Sink<O> sink) {
        this.source = source;
        this.sink = sink;
    }

    // دالة إضافة مرحلة جديدة للأنبوب
    public void addStage(Stage stage) {
        this.stages.add(stage);
    }

    @SuppressWarnings("unchecked")
    public void run() throws Exception {
        if (stages.isEmpty()) {
            // في حالة عدم وجود مراحل، نمرر البيانات مباشرة من المصدر إلى المخرج
            Emitter<I> emitter = item -> ((Sink<I>) sink).consume(item);
            source.produce(emitter);
        } else {
            // تنفيذ البيانات عبر سلسلة المراحل حتى الوصول للـ Sink
            Emitter currentEmitter = item -> ((Sink<Object>) sink).consume(item);

            // الربط العكسي للمراحل من النهاية إلى البداية
            for (int i = stages.size() - 1; i >= 0; i--) {
                Stage stage = stages.get(i);
                Emitter nextEmitter = currentEmitter;
                currentEmitter = item -> {
                    try {
                        stage.process(item.toString(), nextEmitter);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            source.produce(currentEmitter);
        }
    }
}