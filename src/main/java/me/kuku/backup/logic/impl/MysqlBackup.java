package me.kuku.backup.logic.impl;

import me.kuku.backup.logic.DataBaseBackup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class MysqlBackup implements DataBaseBackup {

	@Value("${me.kuku.db.mysql.username}")
	private String username;
	@Value("${me.kuku.db.mysql.password}")
	private String password;
	@Value("${me.kuku.db.mysql.path}")
	private String path;

	@Override
	public void backup(String databaseName, String path) {
		Runtime runtime = Runtime.getRuntime();
		String name = System.getProperty("os.name");
		String suffix = " -u" + username + " -p" + password + " " + databaseName + " > " + path + databaseName + ".sql";
		String command;
		if (name.contains("Windows")){
			if ("".equals(this.path)){
				command = "cmd /c mysqldump" + suffix;
			}else {
				command = "cmd /c \"" + this.path + File.separator + "mysqldump\"" + suffix;
			}
		}else {
			if ("".equals(this.path)) {
				command = "mysqldump" + suffix;
			}else {
				command = "\"" + this.path + File.separator + "mysqldump\"" + suffix;
			}
		}
		try {
			Process process = runtime.exec(command);
			if (process.waitFor() == 0){
				process.destroy();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			runtime.gc();
		}
	}
}
