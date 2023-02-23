module com.sshtools.jajafx {
	requires info.picocli;
	requires java.prefs;
	requires java.net.http;
	requires org.jfxtras.styles.jmetro;
	requires javafx.base;
	requires transitive com.sshtools.sequins;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires transitive javafx.fxml;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.fontawesome5;
	requires org.controlsfx.controls;
	requires com.miglayout.javafx;
	requires com.goxr3plus.fxborderlessscene;
	opens com.sshtools.jajafx;
	exports com.sshtools.jajafx;

//    requires org.scenicview.scenicview;
}