package edu.upenn.cis.cis455.storage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.je.EnvironmentConfig;

import edu.upenn.cis.cis455.model.CorpusEntry;
import edu.upenn.cis.cis455.model.KeywordEntry;
import edu.upenn.cis.cis455.model.LexiconEntry;
import edu.upenn.cis.cis455.model.UrlEntry;
import edu.upenn.cis.cis455.model.User;

public class DBWrapper implements StorageInterface {
	
	private String envDirectory = null;
	
	private Environment myEnv;
	private EntityStore store;
	
	private Map<String,User> userMap = new HashMap<>();
	private Map<Integer,CorpusEntry> corpus = new HashMap<>();
	private Map<Integer,UrlEntry> urls = new HashMap<>();
	private Map<String,Integer> invUrls = new HashMap<>();
	private Map<Integer,LexiconEntry> lex = new HashMap<>();
	private Map<String,Integer> invLex = new HashMap<>();
	private Map<Integer,List<KeywordEntry>> inverted = new HashMap<>();
	private StoredSortedMap<Integer,KeywordEntry> invertedIndex;
	
	private static final String USER_STORE = "user_store";
    private static final String CORPUS_STORE = "corpus_store";
    private static final String URL_STORE = "url_store";
    private static final String INV_URL_STORE = "inv_url_store";
    private static final String LEX_STORE = "lex_store";
    private static final String INV_LEX_STORE = "inv_lex_store";
    private static final String OCCURRENCE_STORE = "occurrence_store";
	
	private static final String CLASS_CATALOG = "java_class_catalog";
    private StoredClassCatalog javaCatalog;
    
    private Database userDb;
    private Database corpusDb;
    private Database urlDb;
    private Database invUrlDb;
    private Database lexDb;
    private Database invLexDb;
    private Database occurrenceDb;
	
	public DBWrapper(String envDirectory) {
	    this.envDirectory = envDirectory;
	    
	    EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);

        myEnv = new Environment(new File(envDirectory), envConfig);
        
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        
        Database catalogDb = myEnv.openDatabase(null, CLASS_CATALOG, 
                                              dbConfig);

        javaCatalog = new StoredClassCatalog(catalogDb);
        
        userDb = myEnv.openDatabase(null, USER_STORE, dbConfig);
        corpusDb = myEnv.openDatabase(null, CORPUS_STORE, dbConfig);
        urlDb = myEnv.openDatabase(null, URL_STORE, dbConfig);
        invUrlDb = myEnv.openDatabase(null, INV_URL_STORE, dbConfig);
        lexDb = myEnv.openDatabase(null, LEX_STORE, dbConfig);
        invLexDb = myEnv.openDatabase(null, INV_LEX_STORE, dbConfig);
        
        occurrenceDb = myEnv.openDatabase(null, OCCURRENCE_STORE, dbConfig);        
        
        bindViews();
	}
	
	public void bindViews() {
	    EntryBinding<String> stringBinding = new StringBinding();
	    EntryBinding<Integer> intBinding = new IntegerBinding();
	    EntryBinding<User> userBinding = new SerialBinding<User>(javaCatalog, User.class);
	    EntryBinding<CorpusEntry> corpusBinding = new SerialBinding<CorpusEntry>(javaCatalog, CorpusEntry.class);
	    EntryBinding<UrlEntry> urlBinding = new SerialBinding<UrlEntry>(javaCatalog, UrlEntry.class);
	    EntryBinding<LexiconEntry> lexiconBinding = new SerialBinding<LexiconEntry>(javaCatalog, LexiconEntry.class);
	    EntryBinding<KeywordEntry> keywordBinding = new SerialBinding<KeywordEntry>(javaCatalog, KeywordEntry.class);
	    
	    userMap = new StoredSortedMap<String,User>(userDb, stringBinding, userBinding, true);
	    corpus = new StoredSortedMap<Integer,CorpusEntry>(corpusDb, intBinding, corpusBinding, true);
	    urls = new StoredSortedMap<Integer,UrlEntry>(urlDb, intBinding, urlBinding, true);
	    invUrls = new StoredSortedMap<String,Integer>(urlDb, stringBinding, intBinding, true);
	    lex = new StoredSortedMap<Integer,LexiconEntry>(urlDb, intBinding, lexiconBinding, true);
	    invLex = new StoredSortedMap<String,Integer>(urlDb, stringBinding, intBinding, true);
	    invertedIndex = new StoredSortedMap<Integer,KeywordEntry>(urlDb, intBinding, keywordBinding, true);
	}
	
	public void close()
        throws DatabaseException
    {
        userDb.close();
        corpusDb.close();
        urlDb.close();
        invUrlDb.close();
        lexDb.close();
        invLexDb.close();
        occurrenceDb.close();
        javaCatalog.close();
        myEnv.close();
    } 
	
	public final StoredClassCatalog getClassCatalog() {
        return javaCatalog;
    } 
	
	@Override
	public int getCorpusSize() {
	    return corpus.size();
	}
	
	@Override
	public int addDocument(String url, String document) {
	    int docId = getCorpusSize() + 1;
	    
	    corpus.put(docId, new CorpusEntry(docId, document));
	    urls.put(docId, new UrlEntry(docId, url));
	    invUrls.put(url, docId);
	    
	    return docId;
	}
	
	@Override
	public String getDocument(String url) {
	    Integer docId = invUrls.get(url);
	    
	    if (docId == null)
	       return null;
	    else
	       return corpus.get(docId).getContent();
	}

    @Override
    public int getLexiconSize() {
        return lex.size();
    }

    @Override
    public int addOrGetKeywordId(String keyword) {
        if (invLex.containsKey(keyword))
            return invLex.get(keyword);
        else {
            int count = lex.size();
            lex.put(count, new LexiconEntry(count, keyword));
            invLex.put(keyword, count);
            
            return count;
        }
    }
    
//    @Override
//    public void addHit(String keyword, Integer docId) {
//        
//    }

    @Override
    public int addUser(String username, String password) {
        int next = userMap.size();
        
        userMap.put(username, new User(next, username, password));
        return next;
    }

    @Override
    public boolean getSessionForUser(String username, String password) {
        if (userMap.get(username) != null &&
            userMap.get(username).getPassword().equals(password))
            return true;
        else
            return false;
    }
	
}
