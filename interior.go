package bplustree

// @Author KHighness
// @Update 2023-05-20

const (
	MaxKc = 511
)

type kc struct {
	key   int
	child node
}

type kcs [MaxKc + 1]kc

func (a *kcs) Len() int {
	return len(a)
}

func (a *kcs) Less(i, j int) bool {
	if a[i].key == 0 {
		return false
	}

	if a[j].key == 0 {
		return true
	}

	return a[i].key < a[j].key
}

func (a *kcs) Swap(i, j int) {
	a[i], a[j] = a[j], a[i]
}

type interiorNode struct {
	kcs   kcs
	count int
	p     *interiorNode
}

func newInteriorNode(p *interiorNode, largestChild node) *interiorNode {
	i := &interiorNode{
		count: 1,
		p:     p,
	}

	if largestChild != nil {
		i.kcs[0].child = largestChild
	}

	return i
}

func (in *interiorNode) find(key int) (int, bool) {
	panic("implement me")
}

func (in *interiorNode) parent() *interiorNode {
	panic("implement me")
}

func (in *interiorNode) setParent(i *interiorNode) {
	panic("implement me")
}

func (in *interiorNode) full() bool {
	panic("implement me")
}
