package javax.xml.bind.jaxb.addons;

import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This JAX-B plugin adds the @Nonnull and @Nullable annotations to methods.
 */
public class NullableNonnullPlugin extends Plugin {

  private static final List<String> PRIMITIVE_TYPES = Arrays.asList(
          "void", "int", "long", "float", "boolean", "double", "byte", "short", "char"
  );

  @Override
  public boolean isCustomizationTagName(String nsUri, String localName) {
    return false;
  }

  @Override
  public String getOptionName() {
    return "XNullable";
  }

  @Override
  public List<String> getCustomizationURIs() {
    return Collections.singletonList("http://jaxb.dev.java.net/plugin/nullable");
  }

  @Override
  public String getUsage() {
    return "  -XNullable    : adds Nullable and Nonnull annotations to classes generated by JAX-B";
  }

  @Override
  public boolean run(Outline outline, Options options, ErrorHandler errorHandler) throws SAXException {

    for (ClassOutline classOutline : outline.getClasses()) {
      for (JMethod method : classOutline.implClass.methods()) {
        if (isSetter(method)) {
          annotateSetter(classOutline, method);
        } else if (isList(method)) {
          annotateList(method);
        } else {
          annotateGetter(classOutline, method);
        }
      }
    }
    return true;
  }

  private static boolean isList(JMethod method) {
    return method.type().name().matches("List<.*>");
  }

  private static boolean isSetter(JMethod method) {
    return "void".equals(method.type().name())
            && method.params() != null && method.params().size() == 1
            && method.name().matches("set.*");
  }

  private static void annotateGetter(ClassOutline classOutline, JMethod method) {

    if (!PRIMITIVE_TYPES.contains(method.type().name())) {
      if (isPropertyRequired(getPropertyInfo(classOutline, method))) {
        method.annotate(Nonnull.class);
      } else {
        method.annotate(Nullable.class);
      }
    }
  }

  private static void annotateList(JMethod method) {
    // JAX-B uses lazy instantiation for lists, so they are never null
    method.annotate(Nonnull.class);
  }

  private static void annotateSetter(ClassOutline classOutline, JMethod method) {
    JVar parameter = method.params().get(0);
    String parameterTypeName = parameter.type().name();
    if (!PRIMITIVE_TYPES.contains(parameterTypeName)) {
      CPropertyInfo property = getPropertyInfo(classOutline, method);
      if (isPropertyRequired(property)) {
        parameter.annotate(Nonnull.class);
      } else {
        parameter.annotate(Nullable.class);
      }
    }
  }

  private static boolean isPropertyRequired(CPropertyInfo property) {
    if (property instanceof ElementPropertyInfo ) {
      return ((ElementPropertyInfo) property).isRequired();
    }
    if (property instanceof AttributePropertyInfo) {
      return ((AttributePropertyInfo) property).isRequired();
    }
    if (property instanceof ReferencePropertyInfo) {
      return ((ReferencePropertyInfo) property).isRequired();
    }
    return false; // should never happen
  }

  @Nullable
  private static CPropertyInfo getPropertyInfo(ClassOutline classOutline, JMethod method) {
    String methodName = method.name();

    String mixedCasePropertyName = convertMethodNameToPropertyName(methodName);

    for (CPropertyInfo prop : classOutline.target.getProperties()) {
      if (prop.getName(true).equalsIgnoreCase(mixedCasePropertyName)) {
        return prop;
      }
    }
    throw new RuntimeException("Could not find the property for the method " + methodName + ". We think the property name should be " + mixedCasePropertyName);
  }

  private static String convertMethodNameToPropertyName(String methodName) {
    if (methodName.matches("(g|s)et.*") && methodName.length() > 3) {
      return methodName.substring(3);
    } else if (methodName.startsWith("is") && methodName.length() > 2) {
      return methodName.substring(2);
    } else {
      throw new RuntimeException("Could not determine the property name for the method " + methodName);
    }
  }
}
