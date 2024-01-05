package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;

public class BytePrimitiveCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public BytePrimitiveCodeGenerator(CodeGenerationContext context) {
        this.context = context;
    }

    @Override
    public void generateEncodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        sb.append(String.format(
                "%sbyteBuf.writeByte(%s);\n",
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
                "byteBuf.readByte()");
        sb.append(getIndentString(indentation));
        sb.append(setter);
        sb.append("\n");
    }
}
