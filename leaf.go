package bplustree

import (
	"fmt"
	"sort"
)

// @Author KHighness
// @Update 2023-05-20

const (
	MaxKV = 255
)

type kv struct {
	key   int
	value string
}

type kvs [MaxKV]kv

func (a *kvs) Len() int           { return len(a) }
func (a *kvs) Less(i, j int) bool { return a[i].key < a[j].key }
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

func newLeafNode(p *interiorNode) *leafNode {
	return &leafNode{
		p: p,
	}
}

func (ln *leafNode) find(key int) (int, bool) {
	c := func(i int) bool {
		return ln.kvs[i].key >= key
	}

	i := sort.Search(ln.count, c)

	if i < ln.count && ln.kvs[i].key == key {
		return i, true
	}

	return i, false
}

func (ln *leafNode) parent() *interiorNode {
	return ln.p
}

func (ln *leafNode) setParent(p *interiorNode) {
	ln.p = p
}

func (ln *leafNode) full() bool {
	return ln.count == MaxKV
}

func (ln *leafNode) insert(key int, value string) (int, bool) {
	i, exist := ln.find(key)

	if exist {
		ln.kvs[i].value = value
		return 0, false
	}

	if !ln.full() {
		copy(ln.kvs[i+1:], ln.kvs[i:ln.count])
		ln.kvs[i].key = key
		ln.kvs[i].value = value
		ln.count++
		return 0, false
	}

	next := ln.split()

	if key < next.kvs[0].key {
		ln.insert(key, value)
	} else {
		next.insert(key, value)
	}

	return next.kvs[0].key, true
}

func (ln *leafNode) split() *leafNode {
	next := newLeafNode(nil)

	copy(next.kvs[0:], ln.kvs[ln.count/2+1:])

	next.count = MaxKV - ln.count/2 - 1
	next.next = ln.next

	ln.count = ln.count/2 + 1
	ln.next = next

	return next
}
