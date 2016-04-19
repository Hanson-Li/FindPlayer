import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class InvertedFile {

	public final int TO_PAGE_BODY = 0;
	public final int TO_PAGE_TITLE = 1;
	private RecordManager manager;
	private HTree PageBodyIndices;
	private HTree PageTitleIndices;

	private static InvertedFile instance;
	
	public static InvertedFile getInstance() throws IOException{
		if (instance == null) {
			instance = new InvertedFile();
		}
		return instance;
	}
	
	private InvertedFile() throws IOException {
		
		manager = RecordManagerFactory.createRecordManager("FindPlayersInvertedIndex");

		long pbID = manager.getNamedObject("Page_Body_Index");
		if (pbID == 0) {
			PageBodyIndices = HTree.createInstance(manager);
			manager.setNamedObject("Page_Body_Index", PageBodyIndices.getRecid());
		}
		else {
			PageBodyIndices = HTree.load(manager, pbID);
		}
		long ptID = manager.getNamedObject("Page_Title_Index");
		if(ptID == 0){
			PageTitleIndices = HTree.createInstance(manager);
			manager.setNamedObject("Page_Title_Index", PageTitleIndices.getRecid());
		}
		else {
			PageTitleIndices = HTree.load(manager, ptID);
		}
	}
	
	public void addEntry(String term, Posting posting, int type) throws IOException{
		switch(type){
			case TO_PAGE_BODY:
				addEntryIntoBody(term, posting);
				break;
			case TO_PAGE_TITLE:
				addEntryIntoTitle(term, posting);
		}
			
				
	}

	private void addEntryIntoBody(String term, Posting posting) throws IOException {
		Object data = PageBodyIndices.get(term);
		if( data == null){
			LinkedList<Posting> postings = new LinkedList<Posting>();
			postings.add(posting);
			
			PageBodyIndices.put(term, postings);
		}
		else{
			Object Data = PageBodyIndices.get(term);
			LinkedList<Posting> postings = (LinkedList<Posting>)Data;
			postings.add(posting);
			PageBodyIndices.put(term, postings);
		}	
	}

	private void addEntryIntoTitle(String term, Posting posting) throws IOException {
		Object data = PageTitleIndices.get(term);
		if(data == null){
			LinkedList<Posting> postings = new LinkedList<Posting>();
			postings.add(posting);
			PageTitleIndices.put(term, postings);
			return;
		}
		else{
			LinkedList<Posting> postings = (LinkedList<Posting>)data;
			postings.add(posting);
			PageTitleIndices.put(term, postings);
		}
	}
	
	public void deleteTerm(String term, int Type) throws IOException{
		switch(Type){
		case TO_PAGE_BODY:
			PageBodyIndices.remove(term);
			break;
		case TO_PAGE_TITLE:
			PageTitleIndices.remove(term);
			break;
		default:
			break;
		}
	}
	
	public void deleteEntry(String term, int docID, int type) throws IOException{
		LinkedList<Posting> postings = null;
		switch(type){
		case TO_PAGE_BODY:
			postings = (LinkedList<Posting>) PageBodyIndices.get(term);
			if(postings == null) return;
			for(Posting posting : postings){
				if(posting.getDocID() == docID) {postings.remove(posting); return;}
			}
			PageBodyIndices.put(term, postings);
			break;
		case TO_PAGE_TITLE:
			postings = (LinkedList<Posting>) PageTitleIndices.get(term);
			if(postings == null) return;
			for(Posting posting : postings){
				if(posting.getDocID() == docID) {postings.remove(posting); return;}
			}
			PageTitleIndices.put(term, postings);
			break;
		}
	}
	
	public Posting getEntry(String term, int Type) throws IOException{
		Posting p = null;
		switch(Type){
		case TO_PAGE_BODY:
			p = (Posting) PageBodyIndices.get(term);
			break;
		case TO_PAGE_TITLE:
			p = (Posting) PageTitleIndices.get(term);
			break;		
		}
		return p;
		
	}
	
	public void finalize() throws IOException
	{
		manager.commit();
		manager.close();				
	} 
	
	public void print() throws IOException
	{
		FastIterator it = PageBodyIndices.keys();
		String str;
		while ((str = (String) it.next()) != null) { 
			System.out.print(str);
			LinkedList<Posting> postings = (LinkedList<Posting>) PageBodyIndices.get(str);
			for (Posting post: postings) {
				System.out.print("\t-> "+post.getDocID() + ":");
				for (Integer pos : post.getPositions()) {
					System.out.print(pos + "; ");
				}
			}
			System.out.println();
		}
	}
}
