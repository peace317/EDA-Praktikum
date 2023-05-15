package gui;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;

public class OutStream extends OutputStream {
    private final JTextArea outputArea;
    private final PrintStream out;
    private final VPRListener vprListener;
    private StringBuilder buffer = new StringBuilder();

    public OutStream(JTextArea textArea, PrintStream out, VPRListener vprListener) {
        super();
        this.outputArea = textArea;
        this.out = out;
        this.vprListener = vprListener;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        outputArea.append(String.valueOf((char) b));
        buffer.append((char) b);
        if (b == '\n') {
            String line = buffer.toString();
            buffer = new StringBuilder();
            vprListener.accept(line);
        }
    }

}


