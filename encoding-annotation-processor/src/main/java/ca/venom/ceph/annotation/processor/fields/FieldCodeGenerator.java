package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;

import javax.tools.Diagnostic;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FieldCodeGenerator {
    protected String getIndentString(int indentation) {
        return "    ".repeat(indentation);
    }

    protected String getGetterName(EncodableField encodableField, String typeName, String variableName, String fieldName) {
        if (encodableField.getType().equals(typeName)) {
            String prefix;
            if ("boolean".equals(typeName) || "java.lang.Boolean".equals(typeName)) {
                prefix = "is";
            } else {
                prefix = "get";
            }

            return String.format(
                    "%s.%s%s%s()",
                    variableName,
                    prefix,
                    fieldName.substring(0, 1).toUpperCase(),
                    fieldName.substring(1));
        } else {
            return variableName;
        }
    }

    protected String getSetter(EncodableField encodableField,
                             String typeName,
                             String variableName,
                             String fieldName,
                             String value) {
        if (encodableField.getType().equals(typeName)) {
            String prefix = "set";
            return String.format(
                    "%s.%s%s%s(%s);",
                    variableName,
                    prefix,
                    fieldName.substring(0, 1).toUpperCase(),
                    fieldName.substring(1),
                    value);
        } else {
            return String.format(
                    "%s = %s;",
                    variableName,
                    value
            );
        }
    }

    public static FieldCodeGenerator getFieldCodeGenerator(CodeGenerationContext context,
                                                           EncodableField field,
                                                           String typeName) {
        if (context.getFieldCodeGenerators().containsKey(typeName)) {
            return context.getFieldCodeGenerators().get(typeName);
        }

        if (context.getEncodableClasses().containsKey(typeName)) {
            return context.getFieldCodeGenerators().get("CephType");
        }

        if (field.getInterfaces() != null &&
                field.getInterfaces().contains("ca.venom.ceph.types.EnumWithIntValue")) {
            return context.getFieldCodeGenerators().get("Enum");
        }

        Pattern pattern = Pattern.compile("^([^<]+)<.*>$");
        Matcher matcher = pattern.matcher(typeName);
        if (matcher.matches()) {
            String outerType = matcher.group(1);
            if (context.getFieldCodeGenerators().containsKey(outerType)) {
                return context.getFieldCodeGenerators().get(outerType);
            }
        }

        return null;
    }

    public abstract void generateEncodeJavaCode(StringBuilder sb,
                                                EncodableField field,
                                                int indentation,
                                                String variableName,
                                                String typeName);

    public abstract void generateDecodeJavaCode(StringBuilder sb,
                                                EncodableField field,
                                                int indentation,
                                                String variableName,
                                                String typeName);

    public void setTypeCode(String typeCode) {
    }
}
