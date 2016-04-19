
public class DataUnit {
	private String url;
	private String pageContent;
	private int index;

	public DataUnit() {
		this.url = null;
		this.pageContent = null;
		this.index = -1;
	}
	
	public DataUnit(String url, String pageContent, int index){
		this.url = url;
		this.pageContent = pageContent;
		this.index = index;
	}
	
	public String getUrl(){
		return this.url;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public String getContent(){
		return this.pageContent;
	}
	
	public void setContent(String pageContent){
		this.pageContent = pageContent;
	}
	
	public int getIndex(){
		return this.index;
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public String toString() {
		return ("Index = "+this.index+"  URL = "+this.url+"  Page Content = "+this.pageContent);
	}
}
