package ca.venom.ceph.annotation.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor14;
import javax.tools.Diagnostic;

public class CephTypeElementVisitor extends AbstractElementVisitor14<Void, String> {
    private Messager messager;
    private CephTypeVisitor typeVisitor;

    public CephTypeElementVisitor(Messager messager) {
        this.messager = messager;
        this.typeVisitor = new CephTypeVisitor(messager);
    }

    @Override
    public Void visitRecordComponent(RecordComponentElement recordComponentElement, String prefix) {
        return null;
    }

    @Override
    public Void visitModule(ModuleElement moduleElement, String prefix) {
        return null;
    }

    @Override
    public Void visitPackage(PackageElement packageElement, String prefix) {
        return null;
    }

    @Override
    public Void visitType(TypeElement typeElement, String prefix) {
        messager.printMessage(Diagnostic.Kind.WARNING, prefix + ">>> Type: " + typeElement.toString());
        messager.printMessage(Diagnostic.Kind.WARNING, "Type");
        typeElement.asType().accept(typeVisitor, "   ");
        messager.printMessage(Diagnostic.Kind.WARNING, "Enclosed Elements");
        typeElement.getEnclosedElements().forEach(e -> e.accept(this, "   "));
        return null;
    }

    @Override
    public Void visitVariable(VariableElement variableElement, String prefix) {
        messager.printMessage(Diagnostic.Kind.WARNING, prefix + ">>> Variable: " + variableElement.toString());
        variableElement.asType().accept(typeVisitor, "   ");
        return null;
    }

    @Override
    public Void visitExecutable(ExecutableElement executableElement, String prefix) {
        return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameterElement typeParameterElement, String prefix) {
        return null;
    }
}
