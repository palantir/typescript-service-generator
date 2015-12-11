package com.palantir.code.ts.generator;

import java.io.OutputStream;
import java.io.PrintWriter;

import cz.habarta.typescript.generator.Settings;

public class OutputWriter {

    private int indent;
    private final PrintWriter writer;
    private final GenerationSettings settings;
    private final OutputStream output;
    private final Settings typeSettings;

    public OutputWriter(int initialIndent, OutputStream stream, GenerationSettings settings) {
        this.output = stream;
        this.writer = new PrintWriter(stream);
        this.indent = initialIndent;
        this.settings = settings;
        this.typeSettings = GenerationSettings.Utils.getSettings(this.settings.getCustomTypeProcessor());
    }

    public void decreaseIndent() {
        indent--;
    }

    public void increaseIndent() {
        indent++;
    }

    public void writeLine(String line) {
        String indentString = "";
        for (int i = 0; !line.isEmpty() && i < indent; i++) {
            indentString += this.typeSettings.indentString;
        }
        write(indentString + line + this.typeSettings.newline);
    }

    public void write(String line) {
        writer.write(line);
    }

    public void close() {
        this.writer.close();
    }

    public OutputStream getOutputStream() {
        return this.output;
    }
}
