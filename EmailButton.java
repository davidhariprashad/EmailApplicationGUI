import javafx.geometry.Pos;
import javafx.scene.control.Button;

public class EmailButton extends Button {

	public EmailButton(String text, String p, String t, int x, int y) {
		super(text);
		path = p;
		type = t;
		setPrefSize(x,y);
		setAlignment(Pos.BASELINE_LEFT);
	}
	
	protected String path;
	protected String type;
	
}