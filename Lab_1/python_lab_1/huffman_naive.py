from collections import Counter

from graph import BinaryNode


def huffman_code_tree(node, bin_string=''):
    if node.is_leaf:
        return {node.value: bin_string}
    (l, r) = node.children()
    d = dict()
    # Recursive call without tail optimization (won't matter)
    # Can use binary arrays instead of strings (was actually slower)
    d.update(huffman_code_tree(l, bin_string + '0'))
    d.update(huffman_code_tree(r, bin_string + '1'))
    return d


def make_tree(nodes):
    while len(nodes) > 1:
        # Usage of lists where ordered queue can be used (nope, didn't give perf boost)
        node1 = nodes[-1]
        node2 = nodes[-2]
        nodes = nodes[:-2]
        new_node = BinaryNode(None, node1.weight + node2.weight, node1, node2)
        nodes.append(new_node)
        # No need to SORT every time
        nodes = sorted(nodes, key=lambda x: x.weight, reverse=True)
    return nodes[0]


def frequency_count(string):
    freq = dict(Counter(string))
    # Can probably use sorted function from the start (using custom comparator)
    freq = sorted(freq.items(), key=lambda x: x[1], reverse=True)
    # Use map function
    freq = [BinaryNode(k, v) for k, v in freq]
    return freq


def huffman_encoding(input_str):
    fc = frequency_count(input_str)
    tree = make_tree(fc)
    codes = huffman_code_tree(tree)
    encoded = ''.join([codes[c] for c in input_str])
    return encoded, codes

