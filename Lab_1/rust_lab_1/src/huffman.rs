use std::collections::HashMap;
use typed_arena::Arena;
use crate::binary_node::BinaryNode;

fn frequency_queue<'g>(string: &str, arena: &'g Arena<BinaryNode<'g, char>>) -> Vec<&'g BinaryNode<'g, char>> {
    let mut counter = HashMap::new();

    string.chars().for_each(|c| {
        *counter.entry(c).or_insert(0) += 1;
    });

    let mut queue: Vec<&'g BinaryNode<'g, char>> = counter
        .iter()
        .map(|(k, v)| BinaryNode::leaf(*k, *v, arena))
        .collect();

    queue.sort_by(|a, b| b.cmp(a));
    queue
}

fn make_tree<'g>(nodes: &'g mut Vec<&'g BinaryNode<'g, char>>,
                 arena: &'g Arena<BinaryNode<'g, char>>) -> &'g BinaryNode<'g, char> {
    while nodes.len() > 1 {
        let node1 = nodes.pop().unwrap();
        let node2 = nodes.pop().unwrap();
        let parent = BinaryNode::parent(&node1, &node2, arena);
        let index = nodes.binary_search_by(|node| parent.cmp(node)).unwrap_or_else(|x| x);
        nodes.insert(index, parent);
    }
    nodes.pop().unwrap()
}

fn huffman_code_tree<'g>(node: &'g BinaryNode<'g, char>, bit_string: String) -> HashMap<char, String> {
    if node.is_leaf() {
        return HashMap::from([(node.value.unwrap(), bit_string)]);
    }

    huffman_code_tree(node.left.unwrap(), bit_string.clone() + "0")
        .into_iter()
        .chain(huffman_code_tree(node.right.unwrap(), bit_string + "1"))
        .collect()
}

pub fn huffman_encoding(input_str: &str) -> (String, HashMap<char, String>) {
    let graph_arena = Arena::<BinaryNode<char>>::new();
    let mut fq = frequency_queue(input_str, &graph_arena);
    let tree = make_tree(&mut fq, &graph_arena);
    let code_map = huffman_code_tree(tree, String::new());
    let encoded = input_str.chars().map(|c| code_map.get(&c).unwrap().as_str()).collect::<String>();
    (encoded, code_map)
}