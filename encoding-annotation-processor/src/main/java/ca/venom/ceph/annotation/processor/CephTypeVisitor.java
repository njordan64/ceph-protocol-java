package ca.venom.ceph.annotation.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor14;
import javax.tools.Diagnostic;

public class CephTypeVisitor extends AbstractTypeVisitor14<Void, String> {
    private Messager messager;

    public CephTypeVisitor(Messager messager) {
        this.messager = messager;
    }

    @Override
    public Void visitIntersection(IntersectionType intersectionType, String prefix) {
        return null;
    }

    @Override
    public Void visitUnion(UnionType unionType, String prefix) {
        return null;
    }

    @Override
    public Void visitPrimitive(PrimitiveType primitiveType, String prefix) {
        return null;
    }

    @Override
    public Void visitNull(NullType nullType, String prefix) {
        return null;
    }

    @Override
    public Void visitArray(ArrayType arrayType, String prefix) {
        return null;
    }

    @Override
    public Void visitDeclared(DeclaredType declaredType, String prefix) {
        messager.printMessage(Diagnostic.Kind.WARNING, prefix + "*** Declared: " + declaredType.toString() + ", " + ((TypeElement) declaredType.asElement()).getQualifiedName());
        declaredType.getTypeArguments().forEach(e -> e.accept(this, "      "));
        return null;
    }

    @Override
    public Void visitError(ErrorType errorType, String prefix) {
        return null;
    }

    @Override
    public Void visitTypeVariable(TypeVariable typeVariable, String prefix) {
        return null;
    }

    @Override
    public Void visitWildcard(WildcardType wildcardType, String prefix) {
        return null;
    }

    @Override
    public Void visitExecutable(ExecutableType executableType, String prefix) {
        return null;
    }

    @Override
    public Void visitNoType(NoType noType, String prefix) {
        return null;
    }
}
