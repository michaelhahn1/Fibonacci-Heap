import java.util.Arrays;
import java.util.LinkedList;

/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over non-negative integers.
 */
public class FibonacciHeap {
	private static int totalCuts = 0;
	private static int totalLinks = 0;

	private static final double MAX_RANK_RATIO = 1.4404;

	private HeapNode _first;
	private HeapNode _min;
	private int _size;
	private int _marked;
	private int _trees;

	public FibonacciHeap() {
		this._first = null;
		this._min = null;
		this._size = 0;
		this._marked = 0;
		this._trees = 0;
	}

	/**
	 * public boolean empty()
	 *
	 * precondition: none
	 * 
	 * The method returns true if and only if the heap is empty.
	 * 
	 */
	public boolean empty() {
		return size() == 0;
	}

	/**
	 * public HeapNode insert(int key)
	 *
	 * Creates a node (of type HeapNode) which contains the given key, and inserts
	 * it into the heap.
	 */
	public HeapNode insert(int key) {
		HeapNode node = new HeapNode(key);
		if (empty()) {
			this._first = node;
			this._min = node;
			node.setNext(node);
			node.setPrev(node);
			this._trees++;
		} else {
			changeToRootNode(node);
		}

		this._size++;
		return node;
	}
	
	/**
	 * inserts a node to the heap as a root
	 */
	
	private void changeToRootNode(HeapNode node) {
		this._trees++;

		if (node.getKey() < findMin().getKey())
			this._min = node;

		node.setParent(null);
		node.setPrev(getLast());
		getLast().setNext(node);
		node.setNext(getFirst());
		getFirst().setPrev(node);
	}

	/**
	 * public void deleteMin()
	 *
	 * Delete the node containing the minimum key.
	 *
	 */
	public void deleteMin() {
		if (empty())
			return;

		this._trees--;

		if (size() == 1) {
			this._min = null;
			this._first = null;
		} else {
			detachCurrentMinNode();
			HeapNode[] newRoots = consolidate();
			concatRoots(newRoots);
		}

		this._size--;
	}
	
	/**
	 * delete the minimal node from the heap
	 * and inserts his children to the heap as roots
	 */

	private void detachCurrentMinNode() {
		HeapNode node = findMin();
		appendChildNodesToRoot(node);
		node.getPrev().setNext(node.getNext());
		node.getNext().setPrev(node.getPrev());

		if (this._first == node)
			this._first = node.getNext();

		node.setNext(null);
		node.setPrev(null);
		this._min = null;
	}
	
	/**
	 * inserts the children of a given node to the heap as roots
	 */

	private void appendChildNodesToRoot(HeapNode node) {
		HeapNode child = node.getFirstChild();
		if (child != null) {
			do {
				HeapNode next = child.getNext();
				changeToRootNode(child);
				child = next;
			} while (child != node.getFirstChild());
		}
		node.setFirstChild(null);
	}
	
	/**
	 * consolidates the trees in the heap, using successive linking
	 */

	private HeapNode[] consolidate() {
		HeapNode[] nodes = new HeapNode[getMaxRank()];
		HeapNode node = getFirst();
		if (node != null) {
			do {
				HeapNode next = node.getNext();
				HeapNode curr = node;
				successiveLink(curr, nodes);
				node = next;

			} while (node != getFirst());
		}
		this._first = null;
		return nodes;
	}
	
	/**
	 * implements successive linking, as was described in the lecture
	 */

	private void successiveLink(HeapNode curr, HeapNode[] nodes) {
		int rank = curr.getRank();

		while (nodes[rank] != null) {
			curr = link(curr, nodes[rank]);
			nodes[rank] = null;
			rank++;
		}
		nodes[rank] = curr;
	}
	
	/**
	 * link two trees with the same rank to a single tree
	 */

	private HeapNode link(HeapNode node1, HeapNode node2) {
		HeapNode parent = node1;
		HeapNode child = node2;

		if (node2.getKey() < node1.getKey()) {
			parent = node2;
			child = node1;
		}

		parent.appendChild(child);
		this._trees--;
		totalLinks++;
		return parent;
	}
	
	/**
	 * rebuilds the heap, by inserting all the trees in the given array
	 * finds minimal node in the heap
	 */

	private void concatRoots(HeapNode[] newRoots) {
		this._min = null;
		this._first = null;
		HeapNode previous = null;
		HeapNode root = null;

		for (int i = 0; i < newRoots.length; i++) {
			root = newRoots[i];

			if (root == null)
				continue;

			if (root.isMarked())
				unmarkNode(root);

			if (this._first == null)
				this._first = root;

			if (this._min == null || this._min.getKey() > root.getKey())
				this._min = root;

			if (previous == null)
				previous = root;

			previous.setNext(root);
			root.setPrev(previous);
			previous = root;
		}

		if (previous != null) {
			previous.setNext(this._first);
			this._first.setPrev(previous);
		}
	}

	/**
	 * public HeapNode findMin()
	 *
	 * Return the node of the heap whose key is minimal.
	 *
	 */
	public HeapNode findMin() {
		return this._min;
	}

	/**
	 * public void meld (FibonacciHeap heap2)
	 *
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2) {
		if (heap2.empty())
			return;

		if (empty()) {
			this._first = heap2.getFirst();
			this._min = heap2.findMin();
		} else {
			getLast().setNext(heap2.getFirst());
			heap2.getLast().setNext(getFirst());

			if (findMin().getKey() > heap2.findMin().getKey())
				this._min = heap2.findMin();
		}

		this._size += heap2.size();
		this._trees += heap2.getTrees();
	}

	/**
	 * public int size()
	 *
	 * Return the number of elements in the heap
	 * 
	 */
	public int size() {
		return this._size;
	}

	/**
	 * public int[] countersRep()
	 *
	 * Return a counters array, where the value of the i-th entry is the number of
	 * trees of order i in the heap.
	 * 
	 */
	public int[] countersRep() {
		if (empty())
			return new int[1];

		int[] arr = new int[getMaxRank()];
		Arrays.fill(arr, 0);
		HeapNode node = getFirst();
		do {
			int rank = node.getRank();
			arr[rank]++;
			node = node.getNext();
		} while (node != getFirst());
		
		return arr;
	}

	/**
	 * public void delete(HeapNode x)
	 *
	 * Deletes the node x from the heap.
	 *
	 */
	public void delete(HeapNode x) {
		decreaseKey(x, Integer.MAX_VALUE);
		if (findMin() != x) // if x is 0, but 0 was already a key in the heap(only non negative keys are
							// allowed)
			decreaseKey(x, 1);

		deleteMin();
	}

	/**
	 * public void decreaseKey(HeapNode x, int delta)
	 *
	 * The function decreases the key of the node x by delta. The structure of the
	 * heap should be updated to reflect this chage (for example, the cascading cuts
	 * procedure should be applied if needed).
	 */
	public void decreaseKey(HeapNode x, int delta) {
		x.setKey(x.getKey() - delta);

		if (x.isRootNode()) {
			if (x.getKey() < findMin().getKey())
				this._min = x;
		} else {
			if (x.getKey() < x.getParent().getKey())
				cut(x);
		}
	}
	
	/**
	 * a recursive function which implements cascading cuts,
	 * as was described in the lecture
	 */

	private void cut(HeapNode x) {
		totalCuts++;
		HeapNode parent = x.getParent();
		x.getNext().setPrev(x.getPrev());
		x.getPrev().setNext(x.getNext());

		if (parent.getFirstChild() == x)
			parent.setFirstChild(parent.getRank() == 1 ? null : x.getNext());

		parent.decRank();
		changeToRootNode(x);

		if (x.isMarked())
			unmarkNode(x);

		if (parent.isMarked())
			cut(parent);
		else if (!parent.isRootNode())
			markNode(parent);

	}


	/**
	 * public int potential()
	 *
	 * This function returns the current potential of the heap, which is: Potential
	 * = #trees + 2*#marked The potential equals to the number of trees in the heap
	 * plus twice the number of marked nodes in the heap.
	 */
	public int potential() {
		return this._trees + 2 * this._marked;
	}

	/**
	 * public static int totalLinks()
	 *
	 * This static function returns the total number of link operations made during
	 * the run-time of the program. A link operation is the operation which gets as
	 * input two trees of the same rank, and generates a tree of rank bigger by one,
	 * by hanging the tree which has larger value in its root on the tree which has
	 * smaller value in its root.
	 */
	public static int totalLinks() {
		return totalLinks;
	}

	/**
	 * public static int totalCuts()
	 *
	 * This static function returns the total number of cut operations made during
	 * the run-time of the program. A cut operation is the operation which
	 * diconnects a subtree from its parent (during decreaseKey/delete methods).
	 */
	public static int totalCuts() {
		return totalCuts;
	}

	private HeapNode getFirst() {
		return this._first;
	}

	private HeapNode getLast() {
		return getFirst().getPrev() != null ? getFirst().getPrev() : getFirst();
	}

	private int getMaxRank() {		
		return (int) Math.round(log2(size()) * MAX_RANK_RATIO);
	}

	private static int log2(int x) {
		int y = 0;

		y = -1;
		while (x > 0) {
			x >>= 1;
			y++;
		}
		return y + 1;
	}

	private void markNode(HeapNode node) {
		node.mark();
		this._marked++;
	}

	private void unmarkNode(HeapNode node) {
		node.unmark();
		this._marked--;
	}

	private int getTrees() {
		return this._trees;
	}

	/**
	 * public class HeapNode
	 * 
	 * If you wish to implement classes other than FibonacciHeap (for example
	 * HeapNode), do it in this file, not in another file
	 * 
	 */
	public class HeapNode {

		private HeapNode _prev;
		private HeapNode _next;
		private HeapNode _parent;
		private HeapNode _firstChild;
		private int _rank;
		private boolean _isMarked;

		public int key;

		public HeapNode(int key) {
			this.key = key;
			this._prev = null;
			this._next = null;
			this._parent = null;
			this._firstChild = null;
			this._rank = 0;
			this._isMarked = false;
		}

		public int getKey() {
			return this.key;
		}

		private void setKey(int key) {
			this.key = key;
		}

		private HeapNode getNext() {
			return this._next;
		}

		private void setNext(HeapNode node) {
			this._next = node;
		}

		private HeapNode getPrev() {
			return this._prev;
		}

		private void setPrev(HeapNode node) {
			this._prev = node;
		}

		private HeapNode getParent() {
			return this._parent;
		}

		private HeapNode getFirstChild() {
			return this._firstChild;
		}

		private void setFirstChild(HeapNode node) {
			this._firstChild = node;
		}

		private void setParent(HeapNode node) {
			this._parent = node;
		}

		private void incRank() {
			this._rank++;
		}

		private void decRank() {
			this._rank--;
		}

		private int getRank() {
			return this._rank;
		}

		private void mark() {
			this._isMarked = true;
		}

		private void unmark() {
			this._isMarked = false;
		}

		private boolean isMarked() {
			return this._isMarked;
		}

		private boolean isRootNode() {
			return this._parent == null;
		}
		
		/**
		 * append a node as child to the current node
		 */

		private void appendChild(HeapNode node) {
			if (getRank() == 0) {
				this._firstChild = node;
				node.setNext(node);
				node.setPrev(node);
			} else {
				HeapNode last = this._firstChild.getPrev();
				last.setNext(node);
				node.setPrev(last);
				node.setNext(this._firstChild);
				this._firstChild.setPrev(node);
			}
			node.setParent(this);
			incRank();
		}
	}
}
