package ca.venom.ceph.annotation.processor;

import ca.venom.ceph.annotation.processor.fields.FieldCodeGenerator;

import javax.annotation.processing.Messager;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Contains some context information for generating source code files.
 */
public class CodeGenerationContext {
    private Messager messager;
    private Map<String, FieldCodeGenerator> fieldCodeGenerators;
    private Map<String, EncodableClass> encodableClasses;
    private Set<String> imports = new HashSet<>();

    /**
     * Creates a new CodeGenerationContext object.
     *
     * @param messager used for reporting errors/warnings
     * @param fieldCodeGenerators map of code generators for field types
     * @param encodableClasses map of classes annotated with @CephType
     */
    public CodeGenerationContext(Messager messager,
                                 Map<String, FieldCodeGenerator> fieldCodeGenerators,
                                 Map<String, EncodableClass> encodableClasses) {
        this.messager = messager;
        this.fieldCodeGenerators = fieldCodeGenerators;
        this.encodableClasses = encodableClasses;
    }

    /**
     * Get the messager
     * @return messager
     */
    public Messager getMessager() {
        return messager;
    }

    /**
     * Get the map of code generators for field types
     * @return map of code generators for field types
     */
    public Map<String, FieldCodeGenerator> getFieldCodeGenerators() {
        return fieldCodeGenerators;
    }

    /**
     * Get the map of classes annotated with @CephType
     * @return map of classes annotated with @CephType
     */
    public Map<String, EncodableClass> getEncodableClasses() {
        return encodableClasses;
    }

    /**
     * Update the map of classes annotated with @CephType
     * @param encodableClasses map of classes annotated with @CephType
     */
    public void setEncodableClasses(Map<String, EncodableClass> encodableClasses) {
        this.encodableClasses = encodableClasses;
    }

    /**
     * Get the set of classes that should be added as import statements in the source code file
     * @return set of classes that should be added as import statements in the source code file
     */
    public Set<String> getImports() {
        return imports;
    }
}
