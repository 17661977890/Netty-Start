package com.netty.nio.file;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  遍历目录文件  (使用 访问者模式)
 *
 *
 *
 *
 */
public class FilesWalkFileTree {

    /**
     * 遍历 检查 jar包
     */
    @Test
    public void test1() throws IOException {

        // 这里不能使用 局部变量 int, 因为 内部类使用局部变量 必须都是 final 的，java1.8后可以不写final
        final AtomicInteger jarCount  = new AtomicInteger();

        Files.walkFileTree(Paths.get("E:\\JAVA\\code\\springboot_code\\032-springboot-servlet-2"), new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                if(file.toString().endsWith(".jar"))
                {
                    jarCount.incrementAndGet();
                    System.out.println(file);
                }

                return super.visitFile(file, attrs);
            }
        });

        System.out.println("jar 包 ：" + jarCount);

    }

    /**
     *  遍历 检查 文件夹 和 文件数
     *  使用  Files.walkFileTree( )
     */
    @Test
    public void test2() throws IOException {

        // 这里不能使用 局部变量 int, 因为 内部类使用局部变量 必须都是 final 的，java1.8后可以不写final
        final AtomicInteger dirCount  = new AtomicInteger();
        final AtomicInteger fileCount = new AtomicInteger();

        Files.walkFileTree(Paths.get("E:\\aio1"), new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                System.out.println("================================>>>>>>>>>>>>> 进入 目录 " + dir);
                dirCount.incrementAndGet();

                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                System.out.println(file);
                fileCount.incrementAndGet();

                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                System.out.println("<<<<<<<<<<<<<================================ 退出 目录 " + dir);
                return super.postVisitDirectory(dir, exc);
            }


        });

        System.out.println("文件夹：" + dirCount);
        System.out.println("文件数：" + fileCount);
        /*
            ================================>>>>>>>>>>>>> 进入 目录 E:\aio1
            E:\aio1\1.jpg
            ================================>>>>>>>>>>>>> 进入 目录 E:\aio1\aio2
            ================================>>>>>>>>>>>>> 进入 目录 E:\aio1\aio2\aio3
            ================================>>>>>>>>>>>>> 进入 目录 E:\aio1\aio2\aio3\aio4
            <<<<<<<<<<<<<================================ 退出 目录 E:\aio1\aio2\aio3\aio4
            <<<<<<<<<<<<<================================ 退出 目录 E:\aio1\aio2\aio3
            E:\aio1\aio2\hello.txt
            <<<<<<<<<<<<<================================ 退出 目录 E:\aio1\aio2
            ================================>>>>>>>>>>>>> 进入 目录 E:\aio1\aio21
            E:\aio1\aio21\a.txt
            <<<<<<<<<<<<<================================ 退出 目录 E:\aio1\aio21
            E:\aio1\hello.txt
            E:\aio1\m.png
            E:\aio1\m1.png
            <<<<<<<<<<<<<================================ 退出 目录 E:\aio1
            文件夹：5
            文件数：6
        */

    }

    /**
     * 遍历 删除 文件 (先删除文件， 删除文件夹必须在 访问后处理方法 postVisitDirectory()里 )
     */
    @Test
    public void test3() throws IOException {

        // 这里不能使用 局部变量 int, 因为 内部类使用局部变量 必须都是 final 的，java1.8后可以不写final
        final AtomicInteger fileCount = new AtomicInteger();

        Files.walkFileTree(Paths.get("E:\\aio1 - 副本 - 副本"), new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                System.out.println(file);
                fileCount.incrementAndGet();
                Files.delete(file);  // =================== 删除文件
                return super.visitFile(file, attrs);
            }

            // 退出目录 处理
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                Files.delete(dir);  // =================== 删除文件夹

                return super.postVisitDirectory(dir, exc);
            }


        });

        System.out.println("文件数：" + fileCount);

    }


    @Test
    public void test100()
    {
        int a = 88;

        a = a ++;

        System.out.println(a);
    }




}
