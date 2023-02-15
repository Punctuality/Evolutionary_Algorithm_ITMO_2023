use std::collections::HashMap;
use std::hash::{BuildHasher, BuildHasherDefault};
use nohash::{NoHashHasher};
use typed_arena::Arena;
use crate::binary_node::BinaryNode;

fn frequency_queue<'g, H: BuildHasher>(string: &str,
                       arena: &'g Arena<BinaryNode<'g, char>>,
                       hasher: H
) -> Vec<&'g BinaryNode<'g, char>> {
    let mut counter: HashMap<u32, usize, H> = HashMap::with_hasher(hasher);

    string.chars().for_each(|c| {
        *counter.entry(u32::from(c)).or_insert(0) += 1;
    });

    let mut queue: Vec<&'g BinaryNode<'g, char>> = counter
        .iter()
        .map(|(k, v)| BinaryNode::leaf(char::from_u32(*k).unwrap(), *v, arena))
        .collect();

    queue.sort_by(|a, b| b.cmp(a));
    queue
}

fn make_htree<'g>(nodes: &'g mut Vec<&'g BinaryNode<'g, char>>,
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

fn huffman_code_table<'g, H>(
    node: &'g BinaryNode<'g, char>,
    bit_string: String,
    hasher: H
) -> HashMap<char, String, H>
    where
        H: Clone + BuildHasher + Default,
        HashMap<char, String, H>: Extend<(char, String)> {
    if node.is_leaf() {
        let mut single_elem = HashMap::with_hasher(hasher);
        single_elem.extend([(node.value.unwrap(), bit_string)]);
        return single_elem;
    }

    huffman_code_table(node.left.unwrap(), bit_string.clone() + "0", hasher.clone())
        .into_iter()
        .chain(huffman_code_table(node.right.unwrap(), bit_string + "1", hasher))
        .collect()
}

type NoCharHash = BuildHasherDefault<NoHashHasher<u32>>;
pub fn huffman_encoding(input_str: &str) -> (String, HashMap<char, String, NoCharHash>) {
    let hasher: NoCharHash = BuildHasherDefault::default();
    let graph_arena = Arena::<BinaryNode<char>>::new();
    let mut fq = frequency_queue(input_str, &graph_arena, hasher.clone());
    let tree = make_htree(&mut fq, &graph_arena);
    let code_map = huffman_code_table(tree, String::new(), hasher);
    let encoded = input_str.chars().map(|c| code_map.get(&c).unwrap().as_str()).collect::<String>();
    (encoded, code_map)
}