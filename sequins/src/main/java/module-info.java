open module com.sshtools.jajafx.sequins {
	requires transitive com.sshtools.jajafx;
	requires transitive com.sshtools.sequins;
	exports com.sshtools.jajafx.progress;
	requires static transitive org.jfxtras.styles.jmetro;
	requires static transitive com.pixelduke.transit;
}