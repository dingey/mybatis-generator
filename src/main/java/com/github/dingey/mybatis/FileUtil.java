package com.github.dingey.mybatis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;

class FileUtil {
	static void writeClassPath(String packageOrPath, String filename, String content) {
		String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		if (path.endsWith("/target/test-classes/")) {
			path = path.replaceFirst("/target/test-classes/", "/src/main/java/");
			path = path + packageOrPath.replace(".", "/") + "/" + filename;
		}
		write(path, content);
	}

	static void writeResourcePath(String filePath, String filename, String content) {
		String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		if (path.endsWith("/target/test-classes/")) {
			path = path.replaceFirst("/target/test-classes/", "/src/main/resources/");
		}
		if (filePath.startsWith("/") || filePath.startsWith("\\")) {
			filePath = filePath.substring(1, filePath.length());
		}
		if (!filePath.endsWith("/") && !filePath.endsWith("\\")) {
			filePath += "/";
		}
		path += filePath + filename;
		write(path, content);
	}

	static void write(String pathAndName, String content) {
		File f = new File(pathAndName);
		if (!f.exists()) {
			System.out.println("Generate " + pathAndName);
			try {
				FileWriter fileWriter = new FileWriter(f);
				fileWriter.write(content);
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Generate Error Exist " + pathAndName);
		}
	}
}
