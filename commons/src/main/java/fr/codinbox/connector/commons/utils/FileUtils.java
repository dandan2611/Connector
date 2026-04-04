package fr.codinbox.connector.commons.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * File-related utility methods.
 */
public final class FileUtils {

    /**
     * Extracts the file extension from the given file path.
     *
     * <p>The extension is the substring after the last {@code '.'} character.
     * For example, {@code "config.yml"} returns {@code "yml"}, and
     * {@code "path/to/file.tar.gz"} returns {@code "gz"}.</p>
     *
     * @param filePath the file path to extract the extension from
     * @return the file extension (without the dot), or {@code null} if no extension is present
     */
    public static @Nullable String getExtension(final @NotNull String filePath) {
        final int index = filePath.lastIndexOf('.');
        if (index == -1)
            return null;
        return filePath.substring(index + 1);
    }
}
