/*
 * Participant.java
 * Copyright, John Ibsen 2011
 */
package net.projectfuse.mail.secretsanta;

public class Participant {

	private String m_name;
	private String m_email;
	private String m_disallowedName;
	private Participant m_targetParticipant;
	private boolean m_hasGiver;
	
	public Participant (String name, String email) {
		m_name = name;
		m_email = email;
		m_disallowedName = "";
	}
	
	public Participant (String name, String email, String notAllowed){
		m_name = name;
		m_email = email;
		m_disallowedName = notAllowed;
	}
	
	public String getName(){
		return m_name;
	}
	
	public String getEmail(){
		return m_email;
	}
	
	public String getDisallowedName(){
		return m_disallowedName;
	}
	
	public Participant getTargetParticipant() {
		return m_targetParticipant;
	}
	
	public void setTargetParticipant(Participant target) {
		m_targetParticipant = target;
		if(target != null){
			target.setHasGiver(true);
		}
	}
	
	public boolean hasGiver() {
		return m_hasGiver;
	}
	
	public void setHasGiver(boolean hasGiver){
		m_hasGiver = hasGiver;
	}
	
	public boolean equals(Object rhs){
		if (rhs instanceof Participant){
			return m_name.equals(((Participant) rhs).getName());
		}
		return false;
	}
	
	/**
	 * Checks that the passed participant name does not match
	 * the disallowedName. ie. couples.
	 * @param partner
	 * @return true if this is a valid target assignment
	 */
	public boolean canAssign(Participant partner){
		return !( m_disallowedName.equals(partner.getName())
				|| partner.getDisallowedName().equals(m_name)
				|| partner.equals(this)
				|| partner.hasGiver());
	}
}