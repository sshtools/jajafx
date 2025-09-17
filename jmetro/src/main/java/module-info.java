import com.sshtools.jajafx.AppStyle;
import com.sshtools.jajafx.jmetro.JMetroAppStyle;

open module com.sshtools.jajafx.jmetro {
	exports com.sshtools.jajafx.jmetro;
	requires transitive org.jfxtras.styles.jmetro;
	requires com.sshtools.jajafx;
	provides AppStyle with JMetroAppStyle;
}