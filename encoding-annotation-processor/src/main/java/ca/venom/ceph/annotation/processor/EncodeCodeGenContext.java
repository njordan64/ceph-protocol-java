package ca.venom.ceph.annotation.processor;

import ca.venom.ceph.annotation.processor.parser.ParsedClass;

import java.util.Map;

public class EncodeCodeGenContext extends CodeGenContext {
    private final int indentation;
    private final String valueAccessor;

    public EncodeCodeGenContext(int indentation, String valueAccessor, Map<String, ParsedClass> parsedClasses) {
        super(parsedClasses);
        this.indentation = indentation;
        this.valueAccessor = valueAccessor;
    }

    public int getIndentation() {
        return indentation;
    }

    public String getValueAccessor() {
        return valueAccessor;
    }
}
