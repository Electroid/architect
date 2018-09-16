package app.ashcon.architect.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility to compress and decompress zip files.
 */
public interface Zip {

    /**
     * Compress a directory into an output stream.
     *
     * @param dir The directory to compress.
     * @param out The output stream to put the files.
     * @throws IOException
     */
    static void compress(File dir, ZipOutputStream out) throws IOException {
        File[] files = dir.listFiles();
        if(files == null || files.length == 0) {
            return;
        }
        for(File file : files) {
            if(file.isDirectory()) {
                compress(file, out);
                continue;
            }
            int size;
            byte[] buffer = new byte[2048];
            FileInputStream in = new FileInputStream(file);
            out.putNextEntry(new ZipEntry(file.getName()));
            while((size = in.read(buffer)) > 0) {
                out.write(buffer, 0, size);
            }
            out.closeEntry();
            in.close();
        }
    }

    /**
     * Decompress a zipped input stream into a directory.
     *
     * @param dir The directory to put the files.
     * @param in The zipped input stream.
     * @throws IOException
     */
    static void decompress(File dir, ZipInputStream in) throws IOException {
        ZipEntry entry;
        String root = null;
        while((entry = in.getNextEntry()) != null) {
            String name;
            if(root == null) {
                root = entry.getName();
                name = dir.toString();
            } else {
                name = dir.toString() + File.separator + entry.getName().replaceFirst(root, "");
            }
            if(entry.isDirectory()) {
                continue;
            } else {
                new File(name).getParentFile().mkdirs();
            }
            int size;
            byte[] buffer = new byte[2048];
            FileOutputStream file = new FileOutputStream(name);
            BufferedOutputStream out = new BufferedOutputStream(file, buffer.length);
            while((size = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, size);
            }
            out.flush();
            out.close();
        }
        in.close();
    }

}
