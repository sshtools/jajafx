import com.sshtools.jajafx.AppStyle;
import com.sshtools.jajafx.CaspianAppStyle;
import com.sshtools.jajafx.ModernaAppStyle;

open module com.sshtools.jajafx {
	requires transitive info.picocli;
	requires transitive java.prefs;
	requires java.net.http;
	requires javafx.base;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires transitive javafx.fxml;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.fontawesome5;
	requires org.controlsfx.controls;
	requires com.miglayout.javafx;
	requires com.goxr3plus.fxborderlessscene;
	requires org.slf4j;
	requires static org.scenicview.scenicview; 
	requires jul.to.slf4j;
	exports com.sshtools.jajafx;

	uses AppStyle;
	provides AppStyle with ModernaAppStyle, CaspianAppStyle;
}