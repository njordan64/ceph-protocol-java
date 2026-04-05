package ca.venom.ceph.annotation.processor.parser;

import ca.venom.ceph.annotation.processor.CodeGenContext;
import ca.venom.ceph.annotation.processor.FieldTypeVisitor;
import ca.venom.ceph.encoding.annotations.ByteOrderPreference;
import ca.venom.ceph.encoding.annotations.CephEncodingSize;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephParentTypeValue;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Set;

public class VersionField {
    public interface FieldType {
        <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context);
    }

    public static class DeclaredFieldType implements FieldType {
        private final String className;

        public DeclaredFieldType(DeclaredType declaredType) {
            TypeElement element = (TypeElement) declaredType.asElement();
            this.className = element.getQualifiedName().toString();
        }

        public DeclaredFieldType(String className) {
            this.className = className;
        }

        @Override
        public <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context) {
            return visitor.visitDeclaredType(this, field, context);
        }

        public String getClassName() {
            return className;
        }
    }

    public static class PrimitiveFieldType implements FieldType {
        private final TypeKind typeKind;

        public PrimitiveFieldType(TypeKind typeKind) {
            switch (typeKind) {
                case BOOLEAN:
                case BYTE:
                case INT:
                case LONG:
                case SHORT:
                    this.typeKind = typeKind;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported TypeKind: " + typeKind.name());
            }
        }

        @Override
        public <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context) {
            return switch (typeKind) {
                case BOOLEAN -> visitor.visitBooleanType(this, field, context);
                case BYTE -> visitor.visitByteType(this, field, context);
                case INT -> visitor.visitIntType(this, field, context);
                case LONG -> visitor.visitLongType(this, field, context);
                default -> visitor.visitShortType(this, field, context);
            };
        }

        public TypeKind getTypeKind() {
            return typeKind;
        }
    }

    public static class WrappedPrimitiveFieldType implements FieldType {
        private final TypeKind typeKind;

        public WrappedPrimitiveFieldType(DeclaredType declaredType) {
            TypeElement element = (TypeElement) declaredType.asElement();
            switch (element.getQualifiedName().toString()) {
                case "java.lang.Boolean" -> this.typeKind = TypeKind.BOOLEAN;
                case "java.lang.Byte" -> this.typeKind = TypeKind.BYTE;
                case "java.lang.Integer" -> this.typeKind = TypeKind.INT;
                case "java.lang.Long" -> this.typeKind = TypeKind.LONG;
                case "java.lang.Short" -> this.typeKind = TypeKind.SHORT;
                default -> throw new IllegalArgumentException("Not a wrapped primitive: " + element.getQualifiedName().toString());
            }
        }

        @Override
        public <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context) {
            return switch (typeKind) {
                case BOOLEAN -> visitor.visitWrappedBooleanType(this, field, context);
                case BYTE -> visitor.visitWrappedByteType(this, field, context);
                case INT -> visitor.visitWrappedIntType(this, field, context);
                case LONG -> visitor.visitWrappedLongType(this, field, context);
                default -> visitor.visitWrappedShortType(this, field, context);
            };
        }
    }

    public static class StringFieldType implements FieldType {
        @Override
        public <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context) {
            return visitor.visitStringType(this, field, context);
        }
    }

    public static class BitSetFieldType implements FieldType {
        @Override
        public <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context) {
            return visitor.visitBitSetType(this, field, context);
        }
    }

    public static class ByteArrayFieldType implements FieldType {
        @Override
        public <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context) {
            return visitor.visitByteArrayType(this, field, context);
        }
    }

    public static class EnumFieldType implements FieldType {
        private final String className;

        public EnumFieldType(DeclaredType declaredType) {
            TypeElement element = (TypeElement) declaredType.asElement();
            this.className = element.getQualifiedName().toString();
        }

        @Override
        public <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context) {
            return visitor.visitEnumType(this, field, context);
        }

        public String getClassName() {
            return className;
        }
    }

    public static class ListFieldType implements FieldType {
        private final FieldType elementFieldType;

        public ListFieldType(DeclaredType declaredType, Set<String> parsedClassNames) {
            DeclaredType elementDeclaredType = ((DeclaredType) declaredType.getTypeArguments().get(0));
            this.elementFieldType = createFieldType(elementDeclaredType, parsedClassNames);
        }

        @Override
        public <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context) {
            return visitor.visitListType(this, field, context);
        }

        public FieldType getElementFieldType() {
            return elementFieldType;
        }
    }

    public static class SetFieldType implements FieldType {
        private final FieldType elementFieldType;

        public SetFieldType(DeclaredType declaredType, Set<String> parsedClassNames) {
            DeclaredType elementDeclaredType = ((DeclaredType) declaredType.getTypeArguments().get(0));
            this.elementFieldType = createFieldType(elementDeclaredType, parsedClassNames);
        }

        @Override
        public <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context) {
            return visitor.visitSetType(this, field, context);
        }

        public FieldType getElementFieldType() {
            return elementFieldType;
        }
    }

    public static class MapFieldType implements FieldType {
        private final FieldType keyFieldType;
        private final FieldType valueFieldType;

        public MapFieldType(DeclaredType declaredType, Set<String> parsedClassNames) {
            DeclaredType keyDeclaredType = (DeclaredType) declaredType.getTypeArguments().get(0);
            this.keyFieldType = createFieldType(keyDeclaredType, parsedClassNames);
            DeclaredType valueDeclaredType = (DeclaredType) declaredType.getTypeArguments().get(1);
            this.valueFieldType = createFieldType(valueDeclaredType, parsedClassNames);
        }

        @Override
        public <R, P extends CodeGenContext> R accept(FieldTypeVisitor<R, P> visitor, VersionField field, P context) {
            return visitor.visitMapType(this, field, context);
        }

        public FieldType getKeyFieldType() {
            return keyFieldType;
        }

        public FieldType getValueFieldType() {
            return valueFieldType;
        }
    }

    private static FieldType createFieldType(Element element, Set<String> parsedClassNames) {
        return createFieldType(element.asType(), parsedClassNames);
    }

    private static FieldType createFieldType(TypeMirror elementType, Set<String> parsedClassNames) {
        if (elementType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) elementType;
            TypeElement typeElement = (TypeElement) declaredType.asElement();

            switch (typeElement.getQualifiedName().toString()) {
                case "java.lang.Boolean":
                case "java.lang.Byte":
                case "java.lang.Integer":
                case "java.lang.Long":
                case "java.lang.Short":
                    return new WrappedPrimitiveFieldType(declaredType);
                case "java.lang.String":
                    return new StringFieldType();
                case "java.util.BitSet":
                    return new BitSetFieldType();
                case "java.util.List":
                    return new ListFieldType(declaredType, parsedClassNames);
                case "java.util.Set":
                    return new SetFieldType(declaredType, parsedClassNames);
                case "java.util.Map":
                    return new MapFieldType(declaredType, parsedClassNames);
            }

            for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                if (interfaceType instanceof DeclaredType interfaceDeclaredType) {
                    Element interfaceElement = interfaceDeclaredType.asElement();
                    if (interfaceElement instanceof TypeElement interfaceTypeElement) {
                        if ("ca.venom.ceph.types.EnumWithIntValue".equals(interfaceTypeElement.getQualifiedName().toString())) {
                            return new EnumFieldType(declaredType);
                        }
                    }
                }
            }

            if (parsedClassNames.contains(typeElement.getQualifiedName().toString())) {
                return new DeclaredFieldType(declaredType);
            }

            throw new IllegalArgumentException("Unsupported type for encoding: " + declaredType.toString());
        } else if (elementType.getKind() == TypeKind.ARRAY) {
            ArrayType arrayType = (ArrayType) elementType;
            if (arrayType.getComponentType().getKind() == TypeKind.BYTE) {
                return new ByteArrayFieldType();
            } else {
                throw new IllegalArgumentException("Invalid array type: " + arrayType.getComponentType().toString());
            }
        } else {
            System.out.println(">>> " + elementType.getKind().name());
            return new PrimitiveFieldType(elementType.getKind());
        }
    }

    private final FieldType fieldType;
    private final String name;
    private final int order;
    private final ByteOrderPreference byteOrderPreference;
    private final Integer encodingSize;
    private final boolean includeTypeSize;
    private final int sizeLength;
    private final String sizeProperty;
    private final byte minVersion;
    private final byte maxVersion;
    private final String parameterTypeValue;

    public VersionField(VariableElement element, CephField fieldAnnotation, Set<String> parsedClassNames) {
        this.fieldType = createFieldType(element, parsedClassNames);
        this.name = element.getSimpleName().toString();

        this.order = fieldAnnotation.order();
        this.byteOrderPreference = fieldAnnotation.byteOrderPreference();
        this.includeTypeSize = fieldAnnotation.includeSize();
        this.sizeLength = fieldAnnotation.sizeLength();
        this.sizeProperty = fieldAnnotation.sizeProperty();
        this.minVersion = fieldAnnotation.minVersion();
        this.maxVersion = fieldAnnotation.maxVersion();

        final CephEncodingSize encodingSize = element.getAnnotation(CephEncodingSize.class);
        if (encodingSize != null) {
            this.encodingSize = encodingSize.value();
        } else {
            this.encodingSize = null;
        }

        final CephParentTypeValue parentTypeValue = element.getAnnotation(CephParentTypeValue.class);
        if (parentTypeValue != null) {
            this.parameterTypeValue = parentTypeValue.value();
        } else {
            this.parameterTypeValue = null;
        }
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public ByteOrderPreference getByteOrderPreference() {
        return byteOrderPreference;
    }

    public Integer getEncodingSize() {
        return encodingSize;
    }

    public boolean isIncludeTypeSize() {
        return includeTypeSize;
    }

    public int getSizeLength() {
        return sizeLength;
    }

    public String getSizeProperty() {
        return sizeProperty;
    }

    public byte getMinVersion() {
        return minVersion;
    }

    public byte getMaxVersion() {
        return maxVersion;
    }

    public String getParameterTypeValue() {
        return parameterTypeValue;
    }
}
