package getalp.wsd.common.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class File
{
    public static boolean exists(String filePath)
    {
        return Files.exists(Paths.get(filePath));
    }

    public static void moveFile(String from, String to)
    {
        try
        {
            Files.move(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void removeFile(String filepath)
    {
        try
        {
            Files.delete(Paths.get(filepath));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void appendFileToAnother(String inputPath, String outputPath)
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(inputPath));
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, true));
            String line;
            while ((line = br.readLine()) != null) 
            {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
            br.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String createTemporaryFileName()
    {
        try
        {
            return Files.createTempFile(null, null).toFile().getAbsolutePath();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String createTemporaryFileName(String directoryPath)
    {
        try
        {
            return Files.createTempFile(Paths.get(directoryPath), null, null).toFile().getAbsolutePath();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void waitUntilFileContentIs(String filePath, String expectedContent)
    {
        try
        {
            String content = "";
            while (content == null || !content.equals(expectedContent))
            {
                Thread.sleep(100);
                content = readFileContent(filePath);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static void writeFileContent(String filePath, String content)
    {
        try
        {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
            writer.write(content);
            writer.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static String readFileContent(String filePath)
    {
        try
        {
            if (exists(filePath))
            {
                BufferedReader reader = Files.newBufferedReader(Paths.get(filePath));
                String content = reader.readLine();
                reader.close();
                return content;
            }
            else
            {
                return "";
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static String readAllFileContent(String filePath)
    {
        try
        {
            if (exists(filePath))
            {
                return new String(Files.readAllBytes(Paths.get(filePath)));
            }
            else
            {
                return "";
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
