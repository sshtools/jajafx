package com.sshtools.jajafx;

import java.util.Iterator;
import java.util.Stack;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class SlideyStack extends AnimPane {

	class Op {
		PageTransition dir;
		Node node;

		Op(PageTransition dir, Node node) {
			this.dir = dir;
			this.node = node;
		}

		@Override
		public String toString() {
			return "Op [dir=" + dir + ", node=" + node + "]";
		}
	}

	private Stack<Op> ops = new Stack<>();

	public boolean isEmpty() {
		return ops.isEmpty();
	}

	public void remove(Node node) {
		for (Iterator<Op> opIt = ops.iterator(); opIt.hasNext();) {
			Op op = opIt.next();
			if (op.node == node) {
				opIt.remove();
				getChildren().remove(op.node);
				break;
			}
		}
	}
	
	public int size() {
		return ops.size();
	}

	public void pop() {
		Op op = ops.pop();
		doAnim(op.dir.opposite(), op.node);
	}

	public void push(PageTransition dir, Node node) {
		StackPane.setAlignment(node, Pos.TOP_LEFT);
		ops.push(new Op(dir, doAnim(dir, node)));
		
		/* NOTE: Without this new content is not fully properly initialize. E.g. CheckListView
		 * won't turn items into real children. Took days to find this!
		 */
		layout();
		applyCss();
	}

}
