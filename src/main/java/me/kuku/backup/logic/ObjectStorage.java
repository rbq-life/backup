package me.kuku.backup.logic;

import java.io.File;

public interface ObjectStorage {
	void uploadFile(File file, String...path);
	void downloadFile(File file, String...path);
	void deleteFile(String...path);
}
