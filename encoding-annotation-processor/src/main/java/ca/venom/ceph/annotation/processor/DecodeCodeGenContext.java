package ca.venom.ceph.annotation.processor;

import ca.venom.ceph.annotation.processor.parser.ParsedClass;

import javax.annotation.processing.Messager;
import java.util.Map;

public class DecodeCodeGenContext extends CodeGenContext {
    private final int indentation;
    private final String valueAccessor;
    private final String valueString;
    private final boolean isMethod;
    private final Messager messager;

    public DecodeCodeGenContext(int indentation,
                                String valueAccessor,
                                String valueString,
                                boolean isMethod,
                                Map<String, ParsedClass> parsedClasses,
                                Messager messager) {
        super(parsedClasses);
        this.indentation = indentation;
        this.valueAccessor = valueAccessor;
        this.valueString = valueString;
        this.isMethod = isMethod;
        this.messager = messager;
    }

    public int getIndentation() {
        return indentation;
    }

    public String getValueAccessor() {
        return valueAccessor;
    }

    public String getValueSetString(String newValue) {
        if (isMethod) {
            return valueString + "(" + newValue + ")";
        } else {
            return valueString + " = " + newValue;
        }
    }

    public Messager getMessager() {
        return messager;
    }
}
