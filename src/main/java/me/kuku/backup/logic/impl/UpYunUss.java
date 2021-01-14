package me.kuku.backup.logic.impl;

import com.upyun.RestManager;
import com.upyun.UpException;
import com.upyun.UpYunUtils;
import me.kuku.backup.logic.ObjectStorage;
import me.kuku.backup.utils.MyUtils;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Service
public class UpYunUss implements ObjectStorage {

	@Value("${me.kuku.os.upyun.bucketName}")
	private String bucketName;
	@Value("${me.kuku.os.upyun.username}")
	private String username;
	@Value("${me.kuku.os.upyun.password}")
	private String password;

	private void execute(Consumer<RestManager> consumer){
		RestManager restManager = new RestManager(bucketName, username, password);
		consumer.accept(restManager);
	}

	@Override
	public void uploadFile(File file, String... path) {
		Map<String, String> params = new HashMap<>();
		params.put(RestManager.PARAMS.CONTENT_MD5.getValue(), UpYunUtils.md5(file, 1024));
		execute(restManager -> {
			try {
				Response response = restManager.writeFile(MyUtils.generateFolderPath(path) + file.getName(), file, params);
				String str = response.body().string();
				System.out.println(str);
			} catch (IOException | UpException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void downloadFile(File file, String... path) {
		execute(restManager -> {
			FileOutputStream fos = null;
			try {
				Response response = restManager.readFile(MyUtils.generateFolderPath(path) + file);
				byte[] bytes = Objects.requireNonNull(response.body()).bytes();
				fos = new FileOutputStream(file);
				fos.write(bytes);
			} catch (IOException | UpException e) {
				e.printStackTrace();
			} finally {
				if (fos != null){
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void deleteFile(String... path) {
		execute(restManager -> {
			try {
				restManager.deleteFile(MyUtils.generateFilePath(path), null);
			} catch (IOException | UpException e) {
				e.printStackTrace();
			}
		});
	}
}
