package bplustree

// @Author KHighness
// @Update 2023-05-20

type node interface {
	find(key int) (int, bool)
	parent() *interiorNode
	setParent(*interiorNode)
	full() bool
}
