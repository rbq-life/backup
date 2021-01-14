package me.kuku.backup.logic.impl;

import com.backblaze.b2.client.B2ListFilesIterable;
import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.B2StorageClientFactory;
import com.backblaze.b2.client.contentSources.B2ByteArrayContentSource;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.structures.B2Bucket;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.backblaze.b2.client.structures.B2ListBucketsResponse;
import com.backblaze.b2.client.structures.B2UploadFileRequest;
import me.kuku.backup.logic.ObjectStorage;
import me.kuku.backup.utils.MyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
public class B2Storage implements ObjectStorage {

	@Value("${me.kuku.os.b2.applicationKeyId}")
	private String applicationKeyId;
	@Value("${me.kuku.os.b2.applicationKey}")
	private String applicationKey;
	@Value("${me.kuku.os.b2.bucketName}")
	private String bucket;

	public void execute(BiConsumer<B2StorageClient, String> consumer){
		try (B2StorageClient client = B2StorageClientFactory.createDefaultFactory().create(applicationKeyId, applicationKey, "kuku")) {
			List<B2Bucket> list = client.listBuckets().getBuckets();
			String bucketId = null;
			for (B2Bucket b2Bucket : list) {
				if (b2Bucket.getBucketName().equals(bucket)) {
					bucketId = b2Bucket.getBucketId();
				}
			}
			consumer.accept(client, bucketId);
		} catch (B2Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void uploadFile(File file, String... path) {
		execute((client, bucketId) -> {
			try {
				byte[] bytes = MyUtils.readFile(file);
				B2UploadFileRequest request = B2UploadFileRequest.builder(bucketId, MyUtils.generateFolderPath(path) + file.getName(), "", B2ByteArrayContentSource.build(bytes)).build();
				client.uploadSmallFile(request);
			} catch (B2Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void downloadFile(File file, String... path) {
	}

	@Override
	public void deleteFile(String... path) {
		execute((client, bucketId) -> {
			try {
				B2ListFilesIterable b2FileVersions = client.fileVersions(bucketId);
				String fileName = MyUtils.generateFilePath(path);
				for (B2FileVersion b2FileVersion : b2FileVersions) {
					if (b2FileVersion.getFileName().equals(fileName)){
						client.deleteFileVersion(fileName, b2FileVersion.getFileId());
					}
				}
			} catch (B2Exception e) {
				e.printStackTrace();
			}
		});
	}
}
