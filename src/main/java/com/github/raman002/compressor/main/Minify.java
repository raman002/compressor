/**
 *
 */
package com.github.raman002.compressor.main;

import com.github.raman002.compressor.util.UniqueArrayList;
import com.yahoo.platform.yui.compressor.YUICompressor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rewatiraman Singh Chandrol
 *
 */
public class Minify implements Serializable
{
    private static final long serialVersionUID = 4559488001612328837L;

    private static final Logger LOGGER = Logger.getLogger(Minify.class.getName());

    private final transient List<String> JS_FILE_LIST = new UniqueArrayList<>();
    private final transient List<String> CSS_FILE_LIST = new UniqueArrayList<>();

    private final Map<String, String> DECOMPRESSED_FILES = new HashMap<>();

    private Minify()
    {
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (args == null)
        {
            args = new String[]{};
        }
        else if (args.length > 1)
        {
            LOGGER.log(Level.SEVERE, "Only one argument is expected...");
        }

        final UniqueArrayList<String> argumentsList = UniqueArrayList.asUniqueList(args);
        final Minify minify = new Minify();

        if (argumentsList.contains("revertFiles"))
        {
            minify.revertMinifiedFiles();
            return;
        }

        final String directoryPath = argumentsList.stream().filter(arg -> arg.startsWith("path:")).findFirst().map(p -> p.split("path:")[1]).orElse("/src");

        minify.scanFiles(directoryPath);
        minify.startCompression();

        try
        {
            minify.serializeDecompressedFilesMap();
        }
        catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "\nError while serializing revert files map!\n", e);
        }
    }

    /**
     * @param directoryPath
     */
    private void scanFiles(final String directoryPath)
    {
        final File file = new File(Paths.get("").toAbsolutePath().toString().concat(directoryPath));

        LOGGER.info("\nScanning ".concat(file.toString()).concat(" for compression\n"));

        final BiPredicate<Path, BasicFileAttributes> fileFilter = (filePath, fileAttributes) ->
        {
            final String fileName = filePath.getFileName().toString();
            final String fileURL = filePath.toFile().toString();

            // Avoid files which are already minified.
            if (fileName.endsWith("min.js") || fileName.endsWith("min.css"))
            {
                return false;
            }
            else if (fileName.endsWith(".js"))
            {
                processJSFile(fileURL);
                return true;
            }
            else if (fileName.endsWith(".css"))
            {
                processCSSFile(fileURL);
                return true;
            }
            return false;
        };

        /* Find all the non-minified js and css files and add them to respective js and css arraylist.
         */
        try
        {
            Files.find(file.toPath(), Integer.MAX_VALUE, fileFilter)
                    .forEach(fileURL -> LOGGER.info("Found: " + fileURL));
        }
        catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "\nError while scanning files!\n", e);
        }
    }


    /**
     * Method to compress js and css resources using parallelstream.
     */
    private void startCompression()
    {
        LOGGER.info("Starting JS files' compression...");

        // Start compression for JS files.
        if (!JS_FILE_LIST.isEmpty())
        {
            JS_FILE_LIST.parallelStream().forEach(file ->
            {
                try
                {
                    YUICompressor.main(new String[]{"-o", ".js$:.js", file});
                    LOGGER.info("Compressed: ".concat(file));
                }
                catch (Exception ex)
                {
                    LOGGER.log(Level.SEVERE, "\nFailed compression: " + file, ex);
                }
            });
        }

        LOGGER.info("Starting CSS files' compression...");

        // Start compression for CSS files.
        if (!CSS_FILE_LIST.isEmpty())
        {
            CSS_FILE_LIST.parallelStream().forEach(file ->
            {
                try
                {
                    YUICompressor.main(new String[]{"-o", ".css$:.css", file});
                    LOGGER.info("Compressed: ".concat(file));
                }
                catch (Exception ex)
                {
                    LOGGER.log(Level.SEVERE, "\nFailed compression: " + file, ex);
                }
            });
        }

        LOGGER.info(String.format("TOTAL JS FILE COMPRESSED: %s", JS_FILE_LIST.size()));
        LOGGER.info(String.format("TOTAL CSS FILE COMPRESSED: %s", CSS_FILE_LIST.size()));
    }

    /**
     * Method which will revert all the minified files so that they can be edited later after the minification
     * and build process is done.
     */
    private void revertMinifiedFiles()
    {
        deserializeDecompressedFilesMap();

        DECOMPRESSED_FILES.forEach((String compressedFile, String tempFile) ->
        {
            try
            {
                // Delete the existing minified file so that ".temp" file with original contents can be restored.
                Files.delete(new File(compressedFile).toPath());
                LOGGER.info("\nDeleted: ".concat(compressedFile));

                // Renaming the file with extension ".temp" with original contents.
                new File(tempFile).renameTo(new File(tempFile.replace(".temp", "")));
            }
            catch (IOException e)
            {
                LOGGER.log(Level.SEVERE, "\nCould not delete ".concat(compressedFile), e);
            }
        });
    }

    /**
     * @param fileURL - URL for the file which needs to be processed i.e. A new temporary file will be created and original contents
     * will be moved to this temporary file in the same directory.
     */
    private void processJSFile(final String fileURL)
    {
        try
        {
            JS_FILE_LIST.add(fileURL);

            createIfAbsent(fileURL);
        }
        catch (Exception e)
        {
            LOGGER.log(Level.SEVERE, "\nError while processing JS file!\n", e);
        }
    }

    private void createIfAbsent(final String fileURL) throws IOException
    {
        // Create a path for creating a .temp file.
        final String newFile = fileURL.concat(".temp");

        // Create a file object for the new file url.
        final File file = new File(newFile);

        // Get path from file object.
        final Path path = file.toPath();

        if (file.exists())
        {
            Files.delete(path);
        }

        Files.createFile(path);

        Files.copy(new File(fileURL).toPath(), file
                .toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

        DECOMPRESSED_FILES.putIfAbsent(fileURL, newFile);
    }

    private void processCSSFile(String fileURL)
    {
        try
        {
            CSS_FILE_LIST.add(fileURL);

            createIfAbsent(fileURL);
        }
        catch (Exception e)
        {
            LOGGER.log(Level.SEVERE, "\nError while processing CSS file!\n", e);
        }
    }

    private void serializeDecompressedFilesMap() throws IOException
    {
        try (final FileOutputStream fileOutputStream = new FileOutputStream(new File(Paths.get("").toAbsolutePath().toString()
                .concat("/map.jar")));
             final ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream))
        {
            objectOutputStream.writeObject(this);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, "\nError while processing writing Revert Files map to the file!\n", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void deserializeDecompressedFilesMap()
    {
        final File outputFile = new File(Paths.get("").toAbsolutePath().toString().concat("/map.jar"));

        try (final FileInputStream fileInputStream = new FileInputStream(outputFile);
             final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream))
        {
            DECOMPRESSED_FILES.putAll(((Minify) objectInputStream.readObject()).getDecompressedFilesMap());
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, "\nError while processing writing Revert Files map to the file!\n", ex);
        }
		/* Calling this method will automatically delete this file as soon as the Java virtual machine
		is terminated. To put it simply this file will be deleted as soon as the build process
		is completed.
		NOTE : Once this action is registered no matter what we do it will not be reverted and the file will be
		deleted anyway.
		*/
        outputFile.deleteOnExit();
    }

    /**
     * @return - This method is access the map which will be written inside map.jar file so that all the minified files
     * can be reverted to their original state.
     */
    private Map<String, String> getDecompressedFilesMap()
    {
        return DECOMPRESSED_FILES;
    }
}
