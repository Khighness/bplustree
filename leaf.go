package bplustree

import "fmt"

// @Author KHighness
// @Update 2023-05-20

const (
	MaxKv = 255
)

type kv struct {
	key   int
	value string
}

type kvs [MaxKv]kv

func (a *kvs) Len() int           { return len(a) }
func (a *kvs) Less(i, j int) bool { return k[i].key < k[j].key }
func (a *kvs) Swap(i, j int)      { a[i], a[j] = a[j], a[i] }

func (a *kvs) String() string {
	var s string
	for _, kv := range a {
		s += fmt.Sprintf("%d\t", kv.key)
	}
	return s
}

type leafNode struct {
	kvs   kvs
	count int
	next  *leafNode
	p     *interiorNode
}

func (ln *leafNode) find(key int) (int, bool) {
	panic("implement me")
}

func (ln *leafNode) parent() *interiorNode {
	panic("implement me")
}

func (ln *leafNode) setParent(i *interiorNode) {
	panic("implement me")
}

func (ln *leafNode) full() bool {
	panic("implement me")
}
