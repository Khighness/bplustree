package bplustree

// @Author KHighness
// @Update 2023-05-21

type BPlusTree struct {
	root     *interiorNode
	first    *leafNode
	leaf     int
	interior int
	height   int
}

func New() *BPlusTree {
	leaf := newLeafNode(nil)
	root := newInteriorNode(nil, leaf)
	leaf.p = root
	return &BPlusTree{
		root:     root,
		first:    leaf,
		leaf:     1,
		interior: 1,
		height:   2,
	}
}

func (bt *BPlusTree) First() *leafNode {
	return bt.first
}

func (bt *BPlusTree) Insert(key int, value string) {
	_, oldIndex, leaf := search(bt.root, key)
	p := leaf.parent()

	mid, split := leaf.insert(key, value)
	if !split {
		return
	}

	var midNode node
	midNode = leaf

	p.kcs[oldIndex].child = leaf.next
	leaf.next.setParent(p)

	interior, interiorP := p, p.parent()

	for {
		var oldIndex int
		var newNode *interiorNode

		isRoot := interiorP == nil

		if !isRoot {
			oldIndex, _ = interiorP.find(key)
		}

		mid, newNode, split = interior.insert(mid, midNode)
		if !split {
			return
		}

		if !isRoot {
			interiorP.kcs[oldIndex].child = newNode
			newNode.setParent(interiorP)

			midNode = interior
		} else {
			bt.root = newInteriorNode(nil, newNode)
			newNode.setParent(bt.root)

			bt.root.insert(mid, interior)
			return
		}

		interior, interiorP = interiorP, interiorP.parent()
	}
}

func (bt *BPlusTree) Search(key int) (string, bool) {
	kv, _, _ := search(bt.root, key)
	if kv == nil {
		return "", false
	}
	return kv.value, true
}

func search(n node, key int) (*kv, int, *leafNode) {
	curr := n
	oldIndex := -1

	for {
		switch t := curr.(type) {
		case *leafNode:
			i, ok := t.find(key)
			if !ok {
				return nil, oldIndex, t
			}
			return &t.kvs[i], oldIndex, t
		case *interiorNode:
			i, _ := t.find(key)
			curr = t.kcs[i].child
			oldIndex = i
		default:
			panic("unexpected type")
		}
	}
}
