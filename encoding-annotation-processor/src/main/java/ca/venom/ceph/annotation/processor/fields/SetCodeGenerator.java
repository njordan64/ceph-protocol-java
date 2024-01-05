package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;

import javax.tools.Diagnostic;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public SetCodeGenerator(CodeGenerationContext context) {
        this.context = context;
    }

    @Override
    public void generateEncodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        String getter = getGetterName(field, typeName, variableName, field.getName());

        sb.append(String.format(
                "%sif (%s == null) {\n",
                getIndentString(indentation),
                getter
        ));
        sb.append(String.format(
                "%sbyteBuf.writeZero(4);\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%s} else {\n",
                getIndentString(indentation)
        ));
        sb.append(String.format(
                "%sif (le) {\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sbyteBuf.writeIntLE(%s.size());\n",
                getIndentString(indentation + 2),
                getter
        ));
        sb.append(String.format(
                "%s} else {\n",
                getIndentString(indentation + 1)
        ));
        sb.append(String.format(
                "%sbyteBuf.writeInt(%s.size());\n",
                getIndentString(indentation + 2),
                getter
        ));
        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.Set<(.+)>$");
        Matcher matcher = pattern.matcher(typeName);
        if (matcher.matches()) {
            String innerType = matcher.group(1);
            sb.append(String.format(
                    "%sfor (%s element : %s) {\n",
                    getIndentString(indentation + 1),
                    innerType,
                    getter
            ));

            FieldCodeGenerator fieldCodeGenerator = getFieldCodeGenerator(context, field, innerType);
            if (fieldCodeGenerator != null) {
                fieldCodeGenerator.generateEncodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        "element",
                        innerType
                );
            } else {
                context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to encode set elements");
            }

            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation + 1)
            ));
        }

        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation)
        ));
    }

    @Override
    public void generateDecodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        sb.append(String.format(
                "%s{\n",
                getIndentString(indentation)
        ));
        sb.append(String.format(
                "%sint setSize = le ? byteBuf.readIntLE() : byteBuf.readInt();\n",
                getIndentString(indentation + 1)
        ));

        Pattern pattern = Pattern.compile("^java\\.util\\.Set<(.+)>$");
        Matcher matcher = pattern.matcher(typeName);
        if (matcher.matches()) {
            context.getImports().add("java.util.Set");
            context.getImports().add("java.util.HashSet");

            String innerType = matcher.group(1);
            sb.append(String.format(
                    "%sSet<%s> decodedSet = new HashSet<>();\n",
                    getIndentString(indentation + 1),
                    innerType
            ));
            sb.append(String.format(
                    "%sfor (int i = 0; i < setSize; i++) {\n",
                    getIndentString(indentation + 1)
            ));

            FieldCodeGenerator fieldCodeGenerator = getFieldCodeGenerator(context, field, innerType);
            if (fieldCodeGenerator != null) {
                sb.append(String.format(
                        "%s%s element;\n",
                        getIndentString(indentation + 2),
                        innerType
                ));
                fieldCodeGenerator.generateDecodeJavaCode(
                        sb,
                        field,
                        indentation + 2,
                        "element",
                        innerType
                );
                sb.append(String.format(
                        "%sdecodedSet.add(element);\n",
                        getIndentString(indentation + 2)
                ));
            } else {
                context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to decode set elements");
            }

            sb.append(String.format(
                    "%s}\n",
                    getIndentString(indentation + 1)
            ));
            String setter = getSetter(
                    field,
                    typeName,
                    variableName,
                    field.getName(),
                    "decodedSet"
            );
            sb.append(getIndentString(indentation + 1));
            sb.append(setter);
            sb.append("\n");
        } else {
            context.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to determine element type for set");
        }

        sb.append(String.format(
                "%s}\n",
                getIndentString(indentation)
        ));
    }
}
