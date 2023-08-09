package com.netty.nio.file;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * 复制 整个 文件夹
 *
 *
 */
public class FilesWalk {

    @Test
    public void testCopyDirAll() throws IOException {
        String source = "E:\\aio1";
        String target = "E:\\aio2";

        Files.walk(Paths.get(source)).forEach(path -> {

            String targetName = path.toString().replace(source, target);

            try {
                // 如果是 目录 创建
                if(Files.isDirectory(path))
                {
                    Files.createDirectories(Paths.get(targetName));

                // 如果是 文件 复制
                }else if(Files.isRegularFile(path))
                {
                    Files.copy(path, Paths.get(targetName));
                }
            } catch (IOException e) {

                e.printStackTrace();
            }


        });


    }





}
