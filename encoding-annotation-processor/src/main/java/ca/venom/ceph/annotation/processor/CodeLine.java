package ca.venom.ceph.annotation.processor;

public class CodeLine {
    private final int indentation;
    private final String text;

    public CodeLine(int indentation, String text) {
        this.indentation = indentation;
        this.text = text;
    }

    public int getIndentation() {
        return indentation;
    }

    public String getText() {
        return text;
    }
}
