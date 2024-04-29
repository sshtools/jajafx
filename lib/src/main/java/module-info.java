open module com.sshtools.jajafx {
	requires transitive info.picocli;
	requires transitive java.prefs;
	requires java.net.http;
	requires org.jfxtras.styles.jmetro;
//	requires transitive com.pixelduke.transit;
	requires javafx.base;
	requires transitive com.sshtools.jaul;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires transitive javafx.fxml;
	requires transitive com.install4j.runtime;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.fontawesome5;
	requires org.controlsfx.controls;
	requires com.miglayout.javafx;
	requires com.goxr3plus.fxborderlessscene;
	requires org.slf4j;
	requires static org.scenicview.scenicview; 
	requires jul.to.slf4j;
	exports com.sshtools.jajafx;

}