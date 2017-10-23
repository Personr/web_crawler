package edu.upenn.cis.cis455.storage;

public interface StorageInterface {
    
    /**
     * How many documents so far?
     */
	public int getCorpusSize();
	
	/**
	 * Changes the content of a document (updates the last modified date)
	 */
	public void modifyDocument(String url, String document);
	
	/**
	 * Add a new document, getting its ID
	 */
	public int addDocument(String url, String documentContents);
	
	/**
	 * Returns date corresponding to the last time the document has been modified
	 * Returns null if the document is not present
	 */
	public String getDocumentLastModified(String url);
	
	/**
	 * How many keywords so far?
	 */
	public int getLexiconSize();
	
	/**
	 * Gets the ID of a word (adding a new ID if this is a new word)
	 */
	public int addOrGetKeywordId(String keyword);
	
	/**
	 * Adds a user and returns an ID
	 */
	public int addUser(String username, String password, String firstName, String lastName);
	
	/**
	 * Tries to log in the user, or else throws a HaltException
	 */
	public boolean getSessionForUser(String username, String password);
	
	public boolean usernameTaken(String username);
	
	/**
	 * Retrieves a document's contents by URL
	 */
	public String getDocument(String url);
	
	public void close();
}
