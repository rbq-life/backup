package me.kuku.backup.utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

	private static final int BUFFER_SIZE = 1024;

	public static void toZip(String srcDir, File file, boolean keepDirStructure){
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			File sourceFile = new File(srcDir);
			fos = new FileOutputStream(file);
			zos = new ZipOutputStream(fos);
			compress(sourceFile, zos, sourceFile.getName(), keepDirStructure, file.getName());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean keepDirStructure, String zosName){
		byte[] buf = new byte[BUFFER_SIZE];
		if (sourceFile.isFile()){
			FileInputStream fis = null;
			try {
				zos.putNextEntry(new ZipEntry(name));
				int len;
				fis = new FileInputStream(sourceFile);
				while ((len = fis.read(buf)) != -1){
					zos.write(buf, 0, len);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (zos != null) {
					try {
						zos.closeEntry();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else {
			File[] listFiles = sourceFile.listFiles();
			if (listFiles == null || listFiles.length == 0){
				if (keepDirStructure){
					try {
						zos.putNextEntry(new ZipEntry(name + "/"));
						zos.closeEntry();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}else {
				for (File file : listFiles) {
					if (file.getName().equals(zosName)) continue;
					if (keepDirStructure){
						compress(file, zos, name + "/" + file.getName(), true, zosName);
					}else {
						compress(file, zos, file.getName(), false, zosName);
					}
				}
			}
		}
	}
}
