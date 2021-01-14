package me.kuku.backup.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

@SuppressWarnings("DuplicatedCode")
public class MyUtils {

	public static String generateFolderPath(String[] arr){
		if (arr.length  == 0) return "";
		StringBuilder sb = new StringBuilder();
		Arrays.stream(arr).forEach(str -> sb.append(str).append("/"));
		return sb.toString();
	}

	public static String generateFilePath(String[] arr){
		String path = generateFolderPath(arr);
		return path.substring(0, path.length() - 1);
	}

	public static byte[] readFile(File file){
		ByteArrayOutputStream bos = null;
		FileInputStream fis = null;
		try {
			bos = new ByteArrayOutputStream();
			fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) != -1){
				bos.write(buffer, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bos != null){
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bos.toByteArray();
	}
}
