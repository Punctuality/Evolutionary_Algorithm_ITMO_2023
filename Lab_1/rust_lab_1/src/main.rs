mod perf_test;
mod huffman;
mod binary_node;
mod math_util;

fn main() {
    let test_str: &str = "aaaaabbbbbccccccdddddddeeeeeeeeeeeeeffsrt";
    let (encoded, code_map) = huffman::huffman_encoding(test_str);

    println!("Test string: {}", &test_str);
    println!("Test codes:");
    for (k, v) in code_map.iter() {
        println!("{}: {}", k, v);
    }
    println!("Encoded: {}", encoded);

    let encoding = |input: &str| huffman::huffman_encoding(input).0;
    let sizing = perf_test::bit_str_repr_size;

    let test_cases = [
        ("HUFFMAN Rust", encoding, sizing, 1000, 1e+3 as usize, 5),
        ("HUFFMAN Rust", encoding, sizing, 10, 1e+7 as usize, 1000),
        ("HUFFMAN Rust", encoding, sizing, 3, 1e+8 as usize, 1000),
    ];

    for (name, func, sizing,
        n_iter, str_len, max_consec_chars) in test_cases.iter() {
        perf_test::test_time_and_memory(name, func, sizing,
                                        *n_iter, *str_len, *max_consec_chars);
    }
}
