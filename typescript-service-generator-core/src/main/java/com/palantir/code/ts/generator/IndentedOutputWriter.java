package com.palantir.code.ts.generator;

import java.io.OutputStream;
import java.io.PrintWriter;

import cz.habarta.typescript.generator.Settings;

public final class IndentedOutputWriter {

    private int indent;
    private final PrintWriter writer;
    private final TypescriptServiceGeneratorConfiguration settings;
    private final Settings typeSettings;

    public IndentedOutputWriter(OutputStream stream, TypescriptServiceGeneratorConfiguration settings) {
        this.writer = new PrintWriter(stream);
        this.indent = 0;
        this.settings = settings;
        this.typeSettings = this.settings.getSettings();
    }

    public void close() {
        this.writer.close();
    }

    public void decreaseIndent() {
        indent--;
    }

    public void increaseIndent() {
        indent++;
    }

    public void write(String line) {
        writer.write(line);
    }

    public void writeLine(String line) {
        String indentString = "";
        for (int i = 0; !line.isEmpty() && i < indent; i++) {
            indentString += this.typeSettings.indentString;
        }
        write(indentString + line + this.typeSettings.newline);
    }
}
