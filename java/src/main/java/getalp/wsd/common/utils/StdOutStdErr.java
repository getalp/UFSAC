package getalp.wsd.common.utils;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public abstract class StdOutStdErr
{
    public static void stfu()
    {
        stfuOut();
        stfuErr();
    }

    public static void speak()
    {
        speakOut();
        speakErr();
    }

    public static void stfuOut()
    {
        System.setOut(new PrintStream(new OutputStream() {public void write(int b) {}}));
    }

    public static void stfuErr()
    {
        System.setErr(new PrintStream(new OutputStream() {public void write(int b) {}}));
    }

    public static void speakOut()
    {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }

    public static void speakErr()
    {
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
    }
}
