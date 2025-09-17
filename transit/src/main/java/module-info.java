import com.sshtools.jajafx.AppStyle;
import com.sshtools.jajafx.transit.TransitAppStyle;

open module com.sshtools.jajafx.transit {

	provides AppStyle with TransitAppStyle;
	requires com.pixelduke.transit;
	requires com.sshtools.jajafx;
}