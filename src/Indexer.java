import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Indexer {

	public final int TO_PAGE_BODY = 0;
	public final int TO_PAGE_TITLE = 1;
	
	InvertedFile InFile;
	ArrayList<String> stopWords;
	Porter porter;
	
	public Indexer(){
		this.InFile = null;
		this.stopWords = null;
		porter = new Porter();
	}
	

	public void readInStopwords(String fileName) throws IOException{
		this.stopWords = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String line;
		while((line = in.readLine()) != null)
		{
			for (String str:line.split("\n")) {
				stopWords.add(str);
			}
		}
		in.close();
	}
	
	public ArrayList removeStopwords(String text){
		try{
			readInStopwords("stopwords.txt");
		}catch (IOException e) {
			e.printStackTrace();
			System.out.println("file not exist!!!");
		}
		ArrayList<String> afterStop = new ArrayList<String>();

		if (text != null) {
			for (String str:text.split("\\s+")) {
				afterStop.add(str);
			}
		}
		ArrayList<String> Stopaf = new ArrayList<String>();
		for (String str:afterStop) {
			if (!str.isEmpty() && !stopWords.contains(str)) {
				Stopaf.add(str);
			}
		}
		return Stopaf;
	}
	
	
	public void stemming(ArrayList<String> arr){
		for(String str : arr){
			str=this.porter.stripAffixes(str);
		}
	}
	
	public void IndexingPage(Page page, ArrayList<String> words, int type) throws IOException{
		// check the URL existence and the modification date
		ForwardFile ff = ForwardFile.getInstance();
		UrlToIdMap Map = UrlToIdMap.getInstance();
		if(Map.ifUrlExist(page.url)){
			if(!ff.isModified(page.index, page.date)) {return;}
			ff.deleteDoc(page.index, type);
		}
		
		HashMap<String, ArrayList<Integer>> map = new HashMap<>();
		for(int i = 0; i < words.size(); i++) {
			if(!map.containsKey(words.get(i))){
				map.put(words.get(i), new ArrayList<Integer>());
			}
			ArrayList<Integer> positions = map.get(words.get(i));
			positions.add(i);
			map.put(words.get(i), positions);
		}
		
		// insert into inverted file
		InvertedFile ivf = InvertedFile.getInstance();
		for(String term : map.keySet()){
			Posting pos = new Posting(page.index, map.get(term));
			ivf.addEntry(term, pos, type);
		}
		
		// insert into forwarded file 	
		int tfmax = 0;
		for(String term : map.keySet()){
			ArrayList<Integer> data = map.get(term);
			if(data.size() > tfmax){
				tfmax = data.size();
			}
		}
		ArrayList<String> terms = new ArrayList<String>();
		for(String term : words){ terms.add(term);}
		ArrayList<String> childURLs = new ArrayList<>();
		for(Page p : page.children){
			childURLs.add(p.url);
		}
		ff.insertDoc(page.index, tfmax, page.url, terms, type, page.date, page.title, childURLs, page.size, map);
	}
	
	public void indexingPageRoot(Page root) throws IOException{
		ArrayList<String> body =   removeStopwords(root.page); 
		ArrayList<String> title =  removeStopwords(root.title);
		if (body.size() > 0) {
			stemming(body);
		}
		if (title.size() > 0) {
			stemming(title);
		}
		IndexingPage(root, body, TO_PAGE_BODY);
		
		IndexingPage(root, title, TO_PAGE_TITLE);
		if(root.children.size() == 0) {return;}
		
		for(Page child : root.children){
			indexingPageRoot(child);
		}		
	}
}
