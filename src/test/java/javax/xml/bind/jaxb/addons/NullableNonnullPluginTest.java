package javax.xml.bind.jaxb.addons;

import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NullableNonnullPluginTest {

  @Test
  public void testPlugin() throws Throwable {

    File xsd = new File("./src/test/resources/sample.xsd");
    File outputDir = new File("./target/temp/" + System.nanoTime());
    outputDir.mkdirs();

    compileSchemaSources(xsd, outputDir);
    compileJavaSources(outputDir);

    // Load compiled class.
    URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{outputDir.toURI().toURL()});
    Class<?> rootClass = Class.forName("generated.Root", true, classLoader);

    assertMethodIsAnnotated(rootClass, "getNullableList", Nonnull.class); // lists are never Nullable!
    assertMethodIsAnnotated(rootClass, "getNonnullList", Nonnull.class);

    assertMethodIsAnnotated(rootClass, "getNullableString", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setNullableString", Nullable.class);
    assertMethodIsAnnotated(rootClass, "getNullableString2", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setNullableString2", Nullable.class);
    assertMethodIsAnnotated(rootClass, "getNonnullString", Nonnull.class);
    assertParametersAreAnnotated(rootClass, "setNonnullString", Nonnull.class);
    assertMethodIsAnnotated(rootClass, "getNonnullString2", Nonnull.class);
    assertParametersAreAnnotated(rootClass, "setNonnullString2", Nonnull.class);

    // nullable primitive types
    assertMethodIsAnnotated(rootClass, "getNullableIntField", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setNullableIntField", Nullable.class);
    assertMethodIsAnnotated(rootClass, "getNullableLongField", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setNullableLongField", Nullable.class);
    assertMethodIsAnnotated(rootClass, "getNullableFloatField", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setNullableFloatField", Nullable.class);
    assertMethodIsAnnotated(rootClass, "isNullableBoolField", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setNullableBoolField", Nullable.class);
    assertMethodIsAnnotated(rootClass, "getNullableDoubleField", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setNullableDoubleField", Nullable.class);
    assertMethodIsAnnotated(rootClass, "getNullableByteField", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setNullableByteField", Nullable.class);
    assertMethodIsAnnotated(rootClass, "getNullableShortField", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setNullableShortField", Nullable.class);

    // non null primitive fields
    assertMethodIsNotAnnotated(rootClass, "getNonnullIntField");
    assertParameterIsNotAnnotated(rootClass, "setNonnullIntField");
    assertMethodIsNotAnnotated(rootClass, "getNonnullLongField");
    assertParameterIsNotAnnotated(rootClass, "setNonnullLongField");
    assertMethodIsNotAnnotated(rootClass, "getNonnullFloatField");
    assertParameterIsNotAnnotated(rootClass, "setNonnullFloatField");
    assertMethodIsNotAnnotated(rootClass, "isNonnullBoolField");
    assertParameterIsNotAnnotated(rootClass, "setNonnullBoolField");
    assertMethodIsNotAnnotated(rootClass, "getNonnullDoubleField");
    assertParameterIsNotAnnotated(rootClass, "setNonnullDoubleField");
    assertMethodIsNotAnnotated(rootClass, "getNonnullByteField");
    assertParameterIsNotAnnotated(rootClass, "setNonnullByteField");
    assertMethodIsNotAnnotated(rootClass, "getNonnullShortField");
    assertParameterIsNotAnnotated(rootClass, "setNonnullShortField");

    // fields compiled from XML attributes
    assertMethodIsAnnotated(rootClass, "getOptionalAttribute", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setOptionalAttribute", Nullable.class);
    assertMethodIsAnnotated(rootClass, "getRequiredAttribute", Nonnull.class);
    assertParametersAreAnnotated(rootClass, "setRequiredAttribute", Nonnull.class);

    // fields compiled from XML references
    assertMethodIsAnnotated(rootClass, "getOptionalAttribute", Nullable.class);
    assertParametersAreAnnotated(rootClass, "setOptionalAttribute", Nullable.class);
    assertMethodIsAnnotated(rootClass, "getRequiredAttribute", Nonnull.class);
    assertParametersAreAnnotated(rootClass, "setRequiredAttribute", Nonnull.class);

    // Currently, I cannot figured out how to handle references.
//    assertMethodIsAnnotated(rootClass, "getNullableReference", Nullable.class);
//    assertParametersAreAnnotated(rootClass, "setNullableReference", Nullable.class);
//    assertMethodIsAnnotated(rootClass, "getNonnullReference", Nullable.class);
//    assertParametersAreAnnotated(rootClass, "setNonnullReference", Nullable.class);
  }

  private static void assertParameterIsNotAnnotated(Class<?> clazz, String methodName) {
    Method method = findMethod(clazz, methodName);
    assertEquals(0, method.getParameterAnnotations()[0].length);
  }

  private static void assertMethodIsNotAnnotated(Class<?> clazz, String methodName) {
    Method method = findMethod(clazz, methodName);
    assertEquals(0, method.getAnnotations().length);
  }

  private static void compileJavaSources(File outputDir) throws IOException {

    List<File> javaFiles = Arrays.asList(
            new File(outputDir + "/generated", "ObjectFactory.java"),
            new File(outputDir + "/generated", "Root.java")
    );
    List<File> classFiles = Arrays.asList(
            new File(outputDir + "/generated", "ObjectFactory.class"),
            new File(outputDir + "/generated", "Root.class")
    );

    DiagnosticCollector<JavaFileObject> errors = new DiagnosticCollector<JavaFileObject>();
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(errors, null, null);
    try {

      Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(javaFiles);
      compiler.getTask(null, fileManager, errors, null, null, compilationUnit).call();

      if (classFiles.size() != javaFiles.size()) {
        StringBuilder message = new StringBuilder();
        for (Diagnostic diagnostic : errors.getDiagnostics()) {
          message.append("Error on line ");
          message.append(diagnostic.getLineNumber());
          message.append(" File: ");
          message.append(diagnostic.getSource()).append("\n");
        }
        fail("Compilation failure in java: \n" + message);
      }
    }
    finally {
      fileManager.close();
    }
  }

  private static void compileSchemaSources(File xsdFile, File outputDir) throws Exception {
    Class<?> clazz = Class.forName("com.sun.tools.xjc.Driver");
    Method mainMethod = clazz.getMethod("run", String[].class, PrintStream.class, PrintStream.class);
    mainMethod.invoke(null, new Object[]{
            new String[]{
                    "-d",
                    outputDir.getCanonicalPath(),
                    "-XNullable",
                    xsdFile.getCanonicalPath()
            },
            null,
            null
    });
  }

  private static void assertParametersAreAnnotated(Class<?> cls, String methodName, Class<?> annotation) {
    Method method = findMethod(cls, methodName);
    for (Annotation[] existingAnnotations : method.getParameterAnnotations()) {
      assertAnnotationPresent(annotation, existingAnnotations);
    }
  }

  private static void assertAnnotationPresent(Class<?> annotation, Annotation[] existingAnnotations) {
    for (Annotation ann : existingAnnotations) {
      if (ann.annotationType().equals(annotation)) {
        return;
      }
    }
    fail("Cannot find annotation " + annotation.getName());
  }


  private static Method findMethod(Class<?> clazz, String methodName) {
    for (Method method : clazz.getMethods()) {
      if (method.getName().equals(methodName)) {
        return method;
      }
    }
    throw new AssertionError("Cannot find method " + methodName);
  }

  private static void assertMethodIsAnnotated(Class<?> clazz, String methodName, Class<? extends Annotation> annotation) throws Exception {
    assertTrue(clazz.getMethod(methodName).isAnnotationPresent(annotation));
  }
}
