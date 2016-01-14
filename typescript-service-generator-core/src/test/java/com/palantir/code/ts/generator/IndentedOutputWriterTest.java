package com.palantir.code.ts.generator;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import cz.habarta.typescript.generator.Settings;

public class IndentedOutputWriterTest {

    private TypescriptServiceGeneratorConfiguration settings;
    private ByteArrayOutputStream stream;
    private Settings typeSettings;
    private IndentedOutputWriter writer;

    @Before
    public void before() {
        stream = new ByteArrayOutputStream();
        settings = Mockito.mock(TypescriptServiceGeneratorConfiguration.class);
        typeSettings = new Settings();
        typeSettings.indentString = "  ";
        Mockito.when(settings.getSettings()).thenReturn(typeSettings);

        writer = new IndentedOutputWriter(stream, settings);
    }

    @Test
    public void testWriteLine() {
        writer.writeLine("asdf");
        writer.close();
        assertEquals("asdf\n", new String(stream.toByteArray()));
    }

    @Test
    public void testIndent() {
        writer.writeLine("foo");
        writer.increaseIndent();
        writer.writeLine("bar");
        writer.writeLine("");
        writer.decreaseIndent();
        writer.writeLine("baz");
        writer.close();
        assertEquals("foo\n  bar\n\nbaz\n", new String(stream.toByteArray()));
    }

    @Test
    public void testWrite() {
        writer.write("fo");
        writer.write("o\n");
        writer.increaseIndent();
        writer.write("bar");
        writer.close();
        assertEquals("foo\nbar", new String(stream.toByteArray()));
    }
}
