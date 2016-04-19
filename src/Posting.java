import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 */

/**
 * @author shawn
 * This posting support phrase search like 'Hong Kong'
 */
public class Posting implements Serializable{

	private int DocID;
	private ArrayList<Integer> Positions;
	
	public Posting(int docID, ArrayList<Integer> positions) {
		// TODO Auto-generated constructor stub
		DocID = docID;
		Positions = positions;
		
	}

	public int getDocID() {
		return DocID;
	}

	public void setDocID(int docID) {
		DocID = docID;
	}

	public ArrayList<Integer> getPositions() {
		return Positions;
	}

	public void setPositions(ArrayList<Integer> positions) {
		Positions = positions;
	}
	
	
}
