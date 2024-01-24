package com.sshtools.jajafx;

import java.net.URL;

import javafx.scene.Node;

public interface PlatformStyle {
	TitleBar titleBar();
	
	Node accessory(Node accessory);
	
	void configureStageRootStyles(Node root);

	URL css();
}
