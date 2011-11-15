/*
 * SecretSantaMailer.java
 * Copyright, John Ibsen 2011
 */
package net.projectfuse.mail.secretsanta;

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

	private static final String SMTP_PORT = "465";
	private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	private static final String SENDER_FROM_EMAIL = "null@null.com";

	private ArrayList<Participant> m_participants = new ArrayList<Participant>();
	private Random rand;
	private MyPopupAuthenticator m_authenticator;
	
	public SecretSantaMailer(){
		m_participants.add(new Participant("Life Partner 1", "null@null.com", "Life Partner 2"));
		m_participants.add(new Participant("Life Partner 2", "null@null.com", "Life Partner 1"));
		/*
		 * Add all participants here
		 */
		m_participants.add(new Participant("Friend 1", "null@null.com"));
		m_participants.add(new Participant("Friend 2", "null@null.com"));
		rand = new Random(System.currentTimeMillis());
		
		m_authenticator = new MyPopupAuthenticator();
	}
	
	public void sendAnnouncementEmail(){
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
			//System.out.println(p.getName() + " --> " + p.getTargetParticipant().getName());
			try {
				sendEmail(p);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
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
	
	public void sendEmail(Participant giver) throws AddressException, MessagingException {
		String host = "smtp.gmail.com";
		String from = SENDER_FROM_EMAIL;
		String to = giver.getEmail();
		
		// Get system properties
		Properties props = System.getProperties();

		// Setup mail server
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		//props.put("mail.debug", "true");
		props.put("mail.smtp.port", SMTP_PORT);
		props.put("mail.smtp.socketFactory.port", SMTP_PORT);
		props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.put("mail.smtp.socketFactory.fallback", "false");

		// Get session
		//Authenticator auth = new MyPopupAuthenticator();
		Session session = Session.getDefaultInstance(props, m_authenticator);
		
		//session.setDebug(true);
		
		// Define message
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, 
		  new InternetAddress(to));
		message.setSubject("CONFIDENTIAL: Your secret santa assignment");
		String body =
				"Merry Christmas " + giver.getName() + "!" +
				"<br><br>This is an automated message generated from John's super secret secret santa picker." +
				"Rest assured, much care has been put to ensure that John does not see these results." +
				"<br><br>Your assignment for secret santa is <b>" + giver.getTargetParticipant().getName() + "</b>." +
				"<br><br>Since this is a prototype program, please send a reply to John IN A NEW EMAIL (he doesn't want to know who you got) to confirm that you received this email." +
				"<br>Further details about price limits and gift exchange date to come soon (from Geno)!" +
				"<br>If for some reason your assignment conflicts with the couples rule, please email John and he'll fix me and re-run me." +
				"<br><br>Have fun!" +
				"<br>--John's Super Secret Secret Santa Picker (JSSSSP)";
		message.setContent(body, "text/html");

		// Send message
		Transport.send(message);

		System.out.println("Email sent to " + giver.getName() + "<" + giver.getEmail() + ">");
	}
	
	public static void main(String[] args){
		SecretSantaMailer ssm = new SecretSantaMailer();
		ssm.sendAnnouncementEmail();
		
		//Test Email section
		/*try {
			Participant testParticipant = new Participant("test1", "test@null.com");
			Participant testGetter = new Participant("test2", "test2@null.com");
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
