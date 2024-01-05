package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;

public class BooleanPrimitiveCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public BooleanPrimitiveCodeGenerator(CodeGenerationContext context) {
        this.context = context;
    }

    @Override
    public void generateEncodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        sb.append(String.format(
                "%sbyteBuf.writeByte(Boolean.TRUE.equals(%s) ? 1 : 0);\n",
                getIndentString(indentation),
                getGetterName(field, typeName, variableName, field.getName())
        ));
    }

    @Override
    public void generateDecodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        String setter = getSetter(
                field,
                typeName,
                variableName,
                field.getName(),
                "byteBuf.readByte() != 0");
        sb.append(getIndentString(indentation));
        sb.append(setter);
        sb.append("\n");
    }
}
