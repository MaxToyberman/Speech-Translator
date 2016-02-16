package com.toyberman.speechtranslator;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Toyberman Maxim on 25-Jul-15.
 */
public class IOUtils {

    public static List<String> getFilesNameInCurrentDirectory(Context context) {

        File[] Files = context.getFilesDir().listFiles();
        List<String> filesNames = new ArrayList<String>();

        for (File f : Files) {
            filesNames.add(f.getName());
        }

        return filesNames;
    }

    public static void deleteAllFiles(Context context) {

        File directory = context.getFilesDir();

        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (int i = 0; i < children.length; i++) {
                new File(directory, children[i]).delete();
            }
        }
    }

    public static void close(Closeable stream) {

        try {
            if (stream != null)
                stream.close();

        } catch (IOException e) {

        }
    }

    public static String readHistoryFromInternalStorage(Context context, String fileName) {
        File file = null;

        file = new File(context.getFilesDir(), fileName);
        InputStream bis = null;
        StringBuilder sb = new StringBuilder();

        try {
            bis = new BufferedInputStream(new FileInputStream(file));

            byte[] buffer = new byte[1024];
            int bytesRead = 0;

            while ((bytesRead = bis.read(buffer)) != -1) {
                // Process the chunk of bytes read
                // in this case we just construct a String and print it out
                String chunk = new String(buffer, 0, bytesRead);
                sb.append(chunk);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void writeHistoryToInternalStorage(String text, Context context) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy-HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        File file = new File(context.getFilesDir(), formattedDate);


        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(formattedDate, Context.MODE_PRIVATE);
            outputStream.write(text.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
