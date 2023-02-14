from collections import Counter
from typing import *
from bisect import insort_right

from graph import BinaryNode


def huffman_code_tree(node, bin_string='') -> Dict[str, str]:
    if node.is_leaf:
        return {node.value: bin_string}
    d = dict()
    # Recursive call without tail optimization (but it's okay the stack limit won't be reached)
    # As it turned out using str is okay, and using binary arrays will be slower
    d.update(huffman_code_tree(node.left, bin_string + '0'))
    d.update(huffman_code_tree(node.right, bin_string + '1'))
    return d


def make_tree(nodes) -> BinaryNode:
    while len(nodes) > 1:
        # Using binary search insert with negative weights
        node2, node1 = nodes[-2:]
        nodes = nodes[:-2]
        insort_right(nodes, BinaryNode(None, node1.weight + node2.weight, node1, node2))
    return nodes[0]


def frequency_queue(string: str) -> List[BinaryNode]:
    # Most of the performance penalty comes from Counter (which is the same in both implementations)
    return [BinaryNode(k, -v, None, None) for k, v in Counter(string).most_common()]


def huffman_encoding(input_str: str) -> (str, dict):
    pq = frequency_queue(input_str)
    tree = make_tree(pq)
    codes = huffman_code_tree(tree)
    encoded = ''.join([codes[c] for c in input_str])
    return encoded, codes
