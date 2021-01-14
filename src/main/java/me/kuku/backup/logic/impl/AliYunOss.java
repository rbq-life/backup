package me.kuku.backup.logic.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import lombok.Getter;
import lombok.Setter;
import me.kuku.backup.logic.ObjectStorage;
import me.kuku.backup.utils.MyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.function.Consumer;

@Service
public class AliYunOss implements ObjectStorage {

	@Value("${me.kuku.os.aliyun.endpoint}")
	private String endpoint;
	@Value("${me.kuku.os.aliyun.accessKeyId}")
	private String accessKeyId;
	@Value("${me.kuku.os.aliyun.accessKeySecret}")
	private String accessKeySecret;
	@Value("${me.kuku.os.aliyun.bucketName}")
	private String bucketName;

	private void execute(Consumer<OSS> consumer){
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
		consumer.accept(ossClient);
		ossClient.shutdown();
	}

	@Override
	public void uploadFile(File file, String...path) {
		execute(ossClient -> ossClient.putObject(bucketName, MyUtils.generateFolderPath(path) + file.getName(), file));
	}

	@Override
	public void downloadFile(File file, String...path){
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, MyUtils.generateFolderPath(path) + file.getName());
		execute(ossClient -> ossClient.getObject(getObjectRequest, file));
	}

	@Override
	public void deleteFile(String... path) {
		execute(ossClient -> ossClient.deleteObject(bucketName, MyUtils.generateFilePath(path)));

	}
}
