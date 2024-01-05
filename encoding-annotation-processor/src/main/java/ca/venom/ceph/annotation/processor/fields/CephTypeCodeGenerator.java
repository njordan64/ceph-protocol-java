package ca.venom.ceph.annotation.processor.fields;

import ca.venom.ceph.annotation.processor.CodeGenerationContext;
import ca.venom.ceph.annotation.processor.EncodableField;

public class CephTypeCodeGenerator extends FieldCodeGenerator {
    private CodeGenerationContext context;

    public CephTypeCodeGenerator(CodeGenerationContext context) {
        this.context = context;
    }

    @Override
    public void generateEncodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        String packageName = typeName.substring(0, typeName.lastIndexOf('.'));
        String className = typeName.substring(packageName.length() + 1);
        packageName = getEncodingPackageName(packageName);
        String getter = getGetterName(field, typeName, variableName, field.getName());

        sb.append(String.format(
                "%s%s.%sEncodable.encode(%s, byteBuf, le);\n",
                getIndentString(indentation),
                packageName,
                className,
                getter
        ));
    }

    private String getEncodingPackageName(String packageName) {
        String encodingPackageName = packageName;
        if (packageName.matches(".*[A-Z].*")) {
            String outerClassName = packageName.substring(packageName.lastIndexOf('.') + 1);
            encodingPackageName = packageName.substring(0, packageName.lastIndexOf('.'));
            encodingPackageName += "._generated." + outerClassName;
        } else {
            encodingPackageName += "._generated";
        }

        return encodingPackageName;
    }

    @Override
    public void generateDecodeJavaCode(StringBuilder sb,
                                       EncodableField field,
                                       int indentation,
                                       String variableName,
                                       String typeName) {
        String packageName = typeName.substring(0, typeName.lastIndexOf('.'));
        String className = typeName.substring(packageName.length() + 1);
        packageName = getEncodingPackageName(packageName);

        String value = String.format(
                "%s.%sEncodable.decode(byteBuf, le)",
                packageName,
                className
        );
        String setter = getSetter(
                field,
                typeName,
                variableName,
                field.getName(),
                value
        );
        sb.append(getIndentString(indentation));
        sb.append(setter);
        sb.append("\n");
    }
}
