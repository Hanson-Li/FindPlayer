import java.util.ArrayList;
import java.util.Date;

public class Page {
	public Page parent;
	public String url;
	public String page;
	public ArrayList<Page> children;
	public String title;
	public int index;
	public Date date;
	public int size;
	
	public Page(String url) {
		this.url = url;
		this.page = null;
		this.title = null;
		this.parent = null;
		this.children = new ArrayList<Page>();
		this.index = -1;
	}
	
	public void setPageID(int id){
		this.index = id;
	}
	
}
