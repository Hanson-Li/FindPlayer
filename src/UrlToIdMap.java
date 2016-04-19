import java.io.IOException;
import java.util.LinkedList;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;


public class UrlToIdMap {

	private HTree UrlToId;
	private RecordManager manager;
	
	private static UrlToIdMap instance;
	
	public static UrlToIdMap getInstance() throws IOException{
		if (instance == null) {
			instance = new UrlToIdMap();
		}
		return instance;
	}
	
	private UrlToIdMap() throws IOException{
		
		manager = RecordManagerFactory.createRecordManager("UrlToId");

		long pbID = manager.getNamedObject("Index_Url");
		if (pbID == 0) {
			UrlToId = HTree.createInstance(manager);
			manager.setNamedObject("Index_Url", UrlToId.getRecid());
		}
		else {
			UrlToId = HTree.load(manager, pbID);
		}
	}
	
	public void addEntry(String url,int index) throws IOException{
				addEntryIntoIndex(url, index);
				
	}

	private void addEntryIntoIndex(String url, int index) throws IOException {
		if(UrlToId.get(url) == null){
			UrlToId.put(url, index);
		}
		else{
			UrlToId.put(url, index);
		}	
	}
	
	public void finalize() throws IOException
	{
		manager.commit();
		manager.close();				
	} 
	
	public boolean ifUrlExist(String url) throws IOException{
//		FastIterator it = UrlToId.keys();
//		String key = null;
//		while((key = (String) it.next()) != null){
//			System.out.println(key);
//		}
		if (this.UrlToId.get(url) != null) {
			return true;
		}
		return false;
	}
}
