package net.projectfuse.mail.secretsanta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MailConfiguration {

	private File m_file;
	
	private String smtpHost;
	private String smtpPort;
	private String senderFromEmail;
	private String emailSubject;
	private String emailBody;
	
	public MailConfiguration(File configFile) {
		m_file = configFile;
		
		parseConfigFile();
	}
	
	private void parseConfigFile() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(m_file));
			String line = null;
			while((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith(COMMENT_CHAR) || line.isEmpty()) {
					continue;
				}
				String[] parts = line.split(SEPARATOR_CHAR);
				if (parts.length != 2) {
					continue;
				}
				if (parts[0].equals(SMTP_HOST)) {
					smtpHost = parts[1].trim();
				} else if (parts[0].equals(SMTP_PORT)) {
					smtpPort = parts[1].trim();
				} else if (parts[0].equals(SENDER_FROM_EMAIL)) {
					senderFromEmail = parts[1].trim();
				} else if (parts[0].equals(SUBJECT)) {
					emailSubject = parts[1];
				} else if (parts[0].equals(BODY)) {
					emailBody = parts[1];
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static final String substituePatterns(
			String giverName,
			String giverTargetName,
			String modify) {
		modify = modify.replaceAll(GIVER_NAME_PATTERN, giverName);
		modify = modify.replaceAll(GIVER_TARGET_NAME_PATTERN, giverTargetName);
		
		return modify;
	}
	
	public String getSmtpHost() {
		return smtpHost;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public String getSenderFromEmail() {
		return senderFromEmail;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	private static final String SMTP_HOST = "SMTP_HOST";
	private static final String SMTP_PORT = "SMTP_PORT";
	private static final String SENDER_FROM_EMAIL = "SENDER_FROM_EMAIL";
	private static final String SUBJECT = "SUBJECT";
	private static final String BODY = "BODY";
	public static final String SEPARATOR_CHAR = "=";
	public static final String COMMENT_CHAR = "#";
	private static final String GIVER_NAME_PATTERN = "%GIVER_NAME%";
	private static final String GIVER_TARGET_NAME_PATTERN = "%GIVER_TARGET_NAME%";
	
}
