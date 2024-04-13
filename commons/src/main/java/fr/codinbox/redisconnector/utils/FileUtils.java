package fr.codinbox.redisconnector.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public final class FileUtils {

    public static @Nullable String getExtension(final @NotNull String filePath) {
        final int index = filePath.lastIndexOf('.');
        if (index == -1)
            return null;
        return filePath.substring(index + 1);
    }

}
