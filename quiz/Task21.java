import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Task21 {
    
    public static void main(String[] args) throws IOException {
    
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromStrings(Arrays.asList("Task21/X.java"));
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
                null, null, compilationUnits);
        boolean success = task.call();
        System.out.println("success: " + success);
        fileManager.close();
        System.out.println(new File("Task21").listFiles().length);
        System.out.println(Arrays.toString(new File("Task21").listFiles()));
    }
}
