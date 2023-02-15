import huffman_naive
import huffman_optimized
from perf_test import *


def main():
    naive_func = lambda string: huffman_naive.huffman_encoding(string)[0]
    naive_sizing = bit_str_repr_size
    opt_func = lambda string: huffman_optimized.huffman_encoding(string)[0]
    opt_sizing = bit_str_repr_size

    test_cases = [
        ("HUFFMAN Python naive", naive_func, naive_sizing, 1000, int(1e+3), 5),
        ("HUFFMAN Python optimized", opt_func, opt_sizing, 1000, int(1e+3), 5),
        ("HUFFMAN Python naive", naive_func, naive_sizing, 10, int(1e+7), 1000),
        ("HUFFMAN Python optimized", opt_func, opt_sizing, 10, int(1e+7), 1000),
        ("HUFFMAN Python naive", naive_func, naive_sizing, 3, int(1e+8), 1000),
        ("HUFFMAN Python optimized", opt_func, opt_sizing, 3, int(1e+8), 1000),
    ]

    for name, func, sizing, n_iter, str_len, max_consec_chars in test_cases:
        test_time_and_memory(name, func, sizing, n_iter, str_len, max_consec_chars)


if __name__ == '__main__':
    test_str = 'aaaaabbbbbccccccdddddddeeeeeeeeeeeeeffsrt'
    test_enc, test_codes = huffman_optimized.huffman_encoding(test_str)
    test_codes = '\n'.join([f"{k} -> {v}"for k, v in test_codes.items()])
    print(f"Test string: {test_str}\nTest codes:\n{test_codes}\nTest encoding: {test_enc}")

    main()
