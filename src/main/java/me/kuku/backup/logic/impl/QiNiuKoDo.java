package me.kuku.backup.logic.impl;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import me.kuku.backup.logic.ObjectStorage;
import me.kuku.backup.utils.MyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Service
public class QiNiuKoDo implements ObjectStorage {

	@Value("${me.kuku.os.qiniu.accessKey}")
	private String accessKey;
	@Value("${me.kuku.os.qiniu.secretKey}")
	private String secretKey;
	@Value("${me.kuku.os.qiniu.buckName}")
	private String buckName;
	@Value("${me.kuku.os.qiniu.region}")
	private String region;
	@Value("${me.kuku.os.quniu.domain}")
	private String domain;
	@Value("${me.kuku.os.quniu.isPrivate}")
	private Boolean isPrivate;

	private Region formatRegion(){
		switch (region){
			case "huadong": return Region.huadong();
			case "huabei": return Region.huabei();
			case "huanan": return Region.huanan();
			case "beimei": return Region.beimei();
			case "xinjiapo": return Region.xinjiapo();
			default: return Region.autoRegion();
		}
	}

	private void execute(Consumer<Auth> consumer){
		Auth auth = Auth.create(accessKey, secretKey);
		consumer.accept(auth);
	}

	@Override
	public void uploadFile(File file, String... path) {
		Configuration cfg = new Configuration(formatRegion());
		UploadManager uploadManager = new UploadManager(cfg);
		execute(auth -> {
			try {
				String upToken = auth.uploadToken(buckName);
				uploadManager.put(file, MyUtils.generateFolderPath(path) + file.getName(), upToken);
			} catch (QiniuException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void downloadFile(File file, String... path) {
		String fileName = MyUtils.generateFilePath(path);
		String encodeFileName = null;
		try {
			encodeFileName = URLEncoder.encode(fileName, "utf-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		AtomicReference<String> atomicUrl = new AtomicReference<>(domain + encodeFileName);
		if (isPrivate){
			execute(auth -> {
				atomicUrl.set(auth.privateDownloadUrl(atomicUrl.get(), 3600));
			});
		}
		String url = atomicUrl.get();
	}

	@Override
	public void deleteFile(String... path) {
		Configuration cfg = new Configuration(formatRegion());
		execute(auth -> {
			BucketManager bucketManager = new BucketManager(auth, cfg);
			try {
				bucketManager.delete(buckName, MyUtils.generateFilePath(path));
			} catch (QiniuException e) {
				e.printStackTrace();
			}
		});
	}
}
