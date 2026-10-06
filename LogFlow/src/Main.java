// تعريف الفئة الرئيسية للمشروع واسمها Main (يجب أن يطابق اسم الملف Main.java)
public class Main {

    // الدالة الرئيسية التي يبدأ تنفيذ البرنامج منها تلقائيًا عند التشغيل
    // تاخذ مصفوفة من النصوص (args) تحتوي على الوسائط القادمة من السطر البرمجي (Command Line Arguments)
    public static void main(String[] args) {

        // التحقق مما إذا كان المستخدم قد مرّر مسار الملف عند تشغيل البرنامج
        // إذا كان طول المصفوفة أقل من 1 (أي لم يُكتب مسار الملف)
        if (args.length < 1) {

            // طباعة رسالة تنبيه للمستخدم في مجرى الأخطاء (System.err) توضح كيفية الاستخدام الصحيح
            System.err.println("Usage: java Main <file-path>");

            // إنهاء تنفيذ الدالة الخروج من البرنامج فورًا
            return;
        }

        // تخزين الوسيط الأول (وهو مسار الملف الذي أدخله المستخدم) في متغير نصي اسمه filePath
        String filePath = args[0];


        // إنشاء كائن (Source) يمثل مصدر البيانات لقراءة الملف سطرًا بسطر
        Source<String> source = new FileLineSource(filePath);

        // إنشاء مرحلة التحليل (ParserStage) لتحويل النصوص إلى كائنات LogRecord وإحصاء الأخطاء
        ParserStage parserStage = new ParserStage();

        // إنشاء كائن (Sink) يمثل الوجهة النهائية لطباعة النتائج على الشاشة
        Sink<Object> sink = new ConsoleSink();


        // إنشاء أنبوب المعالجة (Pipeline) وتوصيل المصدر بالوجهة وإضافة مرحلة الـ Parser في المنتصف
        Pipeline pipeline = new Pipeline<>(source, sink);
        pipeline.addStage(parserStage);


        // استخدام كتلة try-catch للتعامل مع أي أخطاء أو استثناءات قد تحدث أثناء القراءة أو معالجة الأنبوب
        try {
            // البدء الفعلي لعملية تشغيل الأنبوب وقراءة البيانات ومعالجتها حتى الوجهة
            pipeline.run();

            // طباعة الإحصائيات في النهاية (إجمالي السطور، السطور الخاطئة، والسطور الصحيحة)
            System.out.println("\n--- Log Processing Summary ---");
            System.out.println("Total lines processed: " + parserStage.getTotalLines());
            System.out.println("Error lines: " + parserStage.getErrorLines());
            System.out.println("Valid records emitted: " + (parserStage.getTotalLines() - parserStage.getErrorLines()));

        } catch (Exception e) {
            // في حال حدوث خطأ (مثل عدم وجود الملف أو مشكلة في القراءة)، يتم طباعة نص الخطأ
            System.err.println("Error running pipeline: " + e.getMessage());

            // طباعة تتبع كامل لمكان وتفاصيل حدوث الخطأ المعتمد في Java لتسهيل عملية التصحيح (Debugging)
            e.printStackTrace();
        }
    }
}