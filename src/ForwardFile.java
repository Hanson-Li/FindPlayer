import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class ForwardFile implements Serializable{

	public final int TO_PAGE_BODY = 0;
	public final int TO_PAGE_TITLE = 1;
	
	private class Documents implements Serializable{
		int docID;
		int tfMax;
		String URL;
		int size;
		Date date;
		String title;
		ArrayList<String> allTerms;
		ArrayList<String> childURLs;
		HashMap<String, ArrayList<Integer>> map;
		
		public Documents(int id, int tfmax, String url, Date Date, String Title, ArrayList<String> terms, ArrayList<String> childURLs, int Size, HashMap<String, ArrayList<Integer>> map) {
			docID = id;
			tfMax = tfmax;
			URL = url;
			date = Date;
			title = Title;
			size = Size;
			allTerms = terms;
			this.childURLs = childURLs;
			this.map = map;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getURL() {
			return URL;
		}

		public void setURL(String uRL) {
			URL = uRL;
		}

		public int getDocID() {
			return docID;
		}

		public void setDocID(int docID) {
			this.docID = docID;
		}

		public int getTfMax() {
			return tfMax;
		}

		public void setTfMax(int tfMax) {
			this.tfMax = tfMax;
		}
		
		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public void addTerms(String term){
			if(allTerms != null) allTerms.add(term);
		}
		
		public void deleteTerms(String term){
			if(allTerms != null) allTerms.add(term);
		}
		
		public ArrayList<String> getAllTerms() {
			return allTerms;
		}

		public void setAllTerms(ArrayList<String> allTerms) {
			this.allTerms = allTerms;
		}
		
		public ArrayList<String> getChildURLs() {
			return childURLs;
		}

		public void setChildURLs(ArrayList<String> childURLs) {
			this.childURLs = childURLs;
		}
		
		public HashMap<String, ArrayList<Integer>> getMap() {
			return this.map;
		}

	}
	
	private transient RecordManager recman;
	private transient HTree docPageIndices;
	private transient HTree docTitleIndices;
	private static ForwardFile instance;
	
	public static ForwardFile getInstance() throws IOException{
		if (instance == null) {
			instance = new ForwardFile();
		}
		return instance;
	}
	
	private ForwardFile() throws IOException {
		recman = RecordManagerFactory.createRecordManager("FindPlayersForwardIndex");
		long recid = recman.getNamedObject("Page_Forward_Index");
		if(recid == 0){
			docPageIndices = HTree.createInstance(recman);
			recman.setNamedObject("Page_Forward_Index", docPageIndices.getRecid());
		}
		else{
			docPageIndices = HTree.load(recman, recid);
		}
		recid = recman.getNamedObject("Title_Forward_Index");
		if(recid == 0){
			docTitleIndices = HTree.createInstance(recman);
			recman.setNamedObject("Title_Forward_Index", docTitleIndices.getRecid());
		}
		else{
			docTitleIndices = HTree.load(recman, recid);
		}
	}
	
	public void insertDoc(int id, int tfmax,String url, ArrayList<String> terms, int type, Date Date, String Title, ArrayList<String> childURLs, int Size, HashMap<String, ArrayList<Integer>> map) throws IOException{
		Documents doc = new Documents(id, tfmax, url,Date, Title, terms, childURLs, Size, map);
		switch(type){
		case TO_PAGE_BODY:
			Documents test = (Documents) docPageIndices.get(id);
			if(test != null) {System.out.println("Warning: same Doc ID, documents alredy exist"); return;}
			docPageIndices.put(id, doc);
			break;
		case TO_PAGE_TITLE:
			Documents test2 = (Documents) docTitleIndices.get(id);
			if(test2 != null) {System.out.println("Warning: same Doc ID, documents alredy exist"); return;}
			docTitleIndices.put(id, doc);
		}
	}
	
	public void deleteDoc(int id, int type) throws IOException{
		InvertedFile invf = InvertedFile.getInstance();;
		ArrayList<String> terms;
		if (type == TO_PAGE_BODY) {
			Object data = docPageIndices.get(id);
			if (data == null) {
				System.out.println("Warning: the doc ID doesn't exist in page indices");
				return;
			}
			Documents body = (Documents) data;
			terms = body.getAllTerms();
			for (String term : terms) {
				invf.deleteEntry(term, id, TO_PAGE_BODY);
			}
			docPageIndices.remove(id);
		}
		if (type == TO_PAGE_TITLE) {
			Object data2 = docTitleIndices.get(id);
			if (data2 == null) {
				System.out.println("Warning: the doc ID doesn't exist in title indices");
				return;
			}
			Documents title = (Documents) data2;
			terms = title.getAllTerms();
			for (String term : terms) {
				invf.deleteEntry(term, id, TO_PAGE_TITLE);
			}
			docTitleIndices.remove(id);
		}
	}
	
	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();				
	}

	public boolean isModified(int docId, Date date) throws IOException {
		Documents doc = (Documents)docPageIndices.get(docId);
		if(date.after(doc.getDate())) return true;
		return false;
	}

	public HTree getDocPageIndices() {
		return docPageIndices;
	}
	
	public static void printFile() throws IOException{
		ForwardFile ff = ForwardFile.getInstance();
		HTree docs = ff.getDocPageIndices();
		FastIterator it = docs.values();
		Documents doc = null;
		while((doc = (Documents) it.next()) != null){
			System.out.println(doc.getTitle());
			System.out.println(doc.getURL());
			System.out.println(doc.getDate() + " " + doc.getSize());
			for (String term : doc.getMap().keySet()) {
				System.out.print(term + " " + doc.getMap().get(term).size() + "; ");
			}
			System.out.println();
			if	(doc.getChildURLs() != null) {
				for (String childurl : doc.getChildURLs()) {
					System.out.println(childurl);
				}	
			}
		}
	}
}
