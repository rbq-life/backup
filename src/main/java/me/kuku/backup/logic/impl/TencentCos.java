package me.kuku.backup.logic.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import me.kuku.backup.logic.ObjectStorage;
import me.kuku.backup.utils.MyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.function.Consumer;

@Service
public class TencentCos implements ObjectStorage {

	@Value("${me.kuku.os.tencent.secretId}")
	private String secretId;
	@Value("${me.kuku.os.tencent.secretKey}")
	private String secretKey;
	@Value("${me.kuku.os.tencent.region}")
	private String region;
	@Value("${me.kuku.os.tencent.bucketName}")
	private String bucketName;


	private void execute(Consumer<COSClient> consumer){
		BasicCOSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
		Region region = new Region(this.region);
		ClientConfig clientConfig = new ClientConfig(region);
		COSClient cosClient = new COSClient(cred, clientConfig);
		consumer.accept(cosClient);
		cosClient.shutdown();
	}

	@Override
	public void uploadFile(File file, String... path) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, MyUtils.generateFolderPath(path) + file.getName(), file);
		execute(cosClient -> System.out.println(cosClient.putObject(putObjectRequest)));
	}

	@Override
	public void downloadFile(File file, String... path) {
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, MyUtils.generateFolderPath(path) + file.getName());
		execute(cosClient -> cosClient.getObject(getObjectRequest, file));
	}

	@Override
	public void deleteFile(String... path) {
		execute(cosClient -> cosClient.deleteObject(bucketName, MyUtils.generateFilePath(path)));
	}
}
