/*
 * SecretSantaMailer.java
 * Copyright, John Ibsen 2011
 */
package net.projectfuse.mail.secretsanta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

public class SecretSantaMailer {
	
	public class MyPopupAuthenticator extends Authenticator {
		
		private String cachedUsername = "";
		private String cachedPassword = "";
		
		public PasswordAuthentication getPasswordAuthentication() {
			if (cachedUsername.isEmpty()) {
				cachedUsername = JOptionPane.showInputDialog("Enter username for gmail");
			}
			if (cachedPassword.isEmpty()) {
				cachedPassword = JOptionPane.showInputDialog("Enter password:");
			}
			return new PasswordAuthentication(cachedUsername, cachedPassword);
		}
	}

	private static final String PARTICIPANTS_FILENAME = "participants.txt";
	private static final String CONFIG_FILENAME = "config.txt";
	
	private MailConfiguration m_mailConfig;
	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	
	/* 
	 * Won't send emails while this is set to true - will only print results
	 * When off - results are NOT printed to System.out and emails are sent
	 */
	private static final boolean DEBUG = true;

	private ArrayList<Participant> m_participants = new ArrayList<Participant>();
	private Random rand;
	private MyPopupAuthenticator m_authenticator;
	
	public SecretSantaMailer(){
		loadParticipants();
		
		m_mailConfig = new MailConfiguration(new File(CONFIG_FILENAME));
		
		rand = new Random(System.currentTimeMillis());
		
		m_authenticator = new MyPopupAuthenticator();
	}
	
	private void sendAnnouncementEmail(){
		// Making a list
		int retryCount = 0;
		while(!assignParticipants()){
			retryCount++;
			// Checking it twice
			if (retryCount == m_participants.size() * 2) {
				throw new IllegalStateException("Could not make a list with the restricitions");
			}
		}
		System.out.println("Had to retry " + retryCount + " times!");
		for (Participant p : m_participants) {
			// LALALALALALAL I CAN'T SEE THIS
			if (DEBUG) {
				System.out.println(p.getName() + " --> " + p.getTargetParticipant().getName());
			} else {
				try {
					sendEmail(p);
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private boolean assignParticipants() {
		try {
			for (Participant giver : m_participants) {
				giver.setTargetParticipant(getNextAvailableRandomParticipant(giver));
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private Participant getNextAvailableRandomParticipant(Participant giver) throws Exception{
		Participant getter = m_participants.get(rand.nextInt(m_participants.size()));
		int infLoopCheck = 0;
		while(!giver.canAssign(getter)){
			getter = m_participants.get(rand.nextInt(m_participants.size()));
			infLoopCheck++;
			if(infLoopCheck == m_participants.size()){
				//Reset giver states
				resetParticipants();
				throw new Exception("Ran out of assignments");
			}
		}
		return getter;
	}
	
	private void resetParticipants(){
		for (Participant p : m_participants) {
			p.setHasGiver(false);
			p.setTargetParticipant(null);
		}
	}
	
	private void sendEmail(Participant giver) throws AddressException, MessagingException {
		String host = m_mailConfig.getSmtpHost();
		String from = m_mailConfig.getSenderFromEmail();
		String to = giver.getEmail();
		
		// Get system properties
		Properties props = System.getProperties();

		// Setup mail server
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		//props.put("mail.debug", "true");
		props.put("mail.smtp.port", m_mailConfig.getSmtpPort());
		props.put("mail.smtp.socketFactory.port", m_mailConfig.getSmtpPort());
		props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.put("mail.smtp.socketFactory.fallback", "false");

		// Get session
		Session session = Session.getDefaultInstance(props, m_authenticator);
		
		//session.setDebug(true);
		
		// Define message
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, 
		  new InternetAddress(to));
		String subject = MailConfiguration.substituePatterns(
				giver.getName(),
				giver.getTargetParticipant().getName(),
				m_mailConfig.getEmailSubject());
		message.setSubject(subject);
		String body = 
			MailConfiguration.substituePatterns(
					giver.getName(), 
					giver.getTargetParticipant().getName(),
					m_mailConfig.getEmailBody());
		message.setContent(body, "text/html");

		// Send message
		Transport.send(message);

		System.out.println("Email sent to " + giver.getName() + "<" + giver.getEmail() + ">");
	}
	
	public static void main(String[] args){
		SecretSantaMailer ssm = new SecretSantaMailer();
		ssm.sendAnnouncementEmail();
		
		if (DEBUG) {
			//Test Email section
			/*try {
				Participant testParticipant = new Participant("test1", "test1@null.com");
				Participant testGetter = new Participant("test2", "test2@gmail.com");
				testParticipant.setTargetParticipant(testGetter);
				testGetter.setTargetParticipant(testParticipant);
				ssm.sendEmail(testParticipant);
				ssm.sendEmail(testGetter);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}*/
		}
	}
	
	private void loadParticipants() {
		File participantFile = new File(PARTICIPANTS_FILENAME);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(participantFile));
			String line = null;
			while((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith(MailConfiguration.COMMENT_CHAR) || line.isEmpty()) {
					continue;
				}
				m_participants.add(new Participant(line));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
