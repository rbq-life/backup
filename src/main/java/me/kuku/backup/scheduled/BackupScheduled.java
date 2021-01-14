package me.kuku.backup.scheduled;

import lombok.extern.slf4j.Slf4j;
import me.kuku.backup.logic.ObjectStorage;
import me.kuku.backup.logic.impl.MysqlBackup;
import me.kuku.backup.utils.ZipUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;

@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "ResultOfMethodCallIgnored"})
@Component
@Slf4j
public class BackupScheduled {
	@Value("${me.kuku.backup.path}")
	private String paths;
	@Value("${me.kuku.db.mysql.name}")
	private String names;
	@Value("${me.kuku.os.aliyun.status}")
	private Boolean aliYunStatus;
	@Value("${me.kuku.os.tencent.status}")
	private Boolean tencentStatus;
	@Value("${me.kuku.os.upyun.status}")
	private Boolean upYunStatus;
	@Value("${me.kuku.os.qiniu.status}")
	private Boolean qiNiuStatus;
	@Value("${me.kuku.os.b2.status}")
	private Boolean b2Status;
	@Value("${me.kuku.backup.day}")
	private Integer day;
	@Autowired
	private ObjectStorage aliYunOss;
	@Autowired
	private ObjectStorage tencentCos;
	@Autowired
	private ObjectStorage upYunUss;
	@Autowired
	private ObjectStorage qiNiuKoDo;
	@Autowired
	private ObjectStorage b2Storage;
	@Autowired
	private MysqlBackup mysqlBackup;

	private void backup(File file, String...path){
		if (aliYunStatus){
			aliYunOss.uploadFile(file, path);
		}
		if (tencentStatus){
			tencentCos.uploadFile(file, path);
		}
		if (upYunStatus){
			upYunUss.uploadFile(file, path);
		}
		if (qiNiuStatus){
			qiNiuKoDo.uploadFile(file, path);
		}
		if (b2Status){
			b2Storage.uploadFile(file, path);
		}
	}

	private void del(String...path){
		if (aliYunStatus){
			aliYunOss.deleteFile(path);
		}
		if (tencentStatus){
			tencentCos.deleteFile(path);
		}
		if (upYunStatus){
			upYunUss.deleteFile(path);
		}
		if (qiNiuStatus){
			qiNiuKoDo.deleteFile(path);
		}
		if (b2Status){
			b2Storage.deleteFile(path);
		}
	}

	@Scheduled(cron = "${me.kuku.backup.cron}")
	public void backup(){
		File backupExternalFile = new File("backup");
		if (!backupExternalFile.exists()) backupExternalFile.mkdir();
		DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
		LocalDate localDate = LocalDate.now();
		LocalDate beforeLocalDate = localDate.minusDays(day);
		String dateStr = dtf.format(localDate);
		String beforeDateStr = dtf.format(beforeLocalDate);
		File backupInternalFile = new File("backup" + File.separator + dateStr);
		if (!backupInternalFile.exists()) backupInternalFile.mkdir();
		String[] pathArr = paths.split("\\|");
		for (String path : pathArr) {
			try {
				char c = path.charAt(path.length() - 1);
				if (c == '/' || c == '\\'){
					path = path.substring(0, path.length() - 1);
				}
				int index = path.lastIndexOf("/");
				if (index == -1) index = path.lastIndexOf("\\");
				String name = path.substring(index + 1);
				File file = new File("backup" + File.separator + dateStr + File.separator + name + ".zip");
				ZipUtils.toZip(path, file, true);
				backup(file, "backup", dateStr);
				del("backup", beforeDateStr, name + ".zip");
				file.deleteOnExit();
				log.info(path + " 备份文件成功");
			} catch (Exception e) {
				log.error(path + " 备份文件失败");
			}
		}
		String[] nameArr = names.split("\\|");
		for (String name: nameArr){
			try {
				String prefix = "backup" + File.separator + dateStr + File.separator;
				mysqlBackup.backup(name, prefix);
				File file = new File(prefix + name + ".sql");
				backup(file, "backup", dateStr);
				del("backup", beforeDateStr, name + ".sql");
				log.info(name + " 备份数据库成功");
			} catch (Exception e) {
				log.error(name + " 备份数据库失败");
			}
		}
	}
}