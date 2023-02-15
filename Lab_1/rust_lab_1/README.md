### Вывод запуска `cargo run --release`
```yaml
Test string: aaaaabbbbbccccccdddddddeeeeeeeeeeeeeffsrt
Test codes:
a: 100
b: 010
r: 01101
d: 00
s: 01110
t: 01100
f: 01111
e: 11
c: 101
Encoded: 10010010010010001001001001001010110110110110110100000000000000111111111111111111111111110111101111011100110101100
  
  --------------------
ALGORITHM: HUFFMAN Rust
Size of data: 1000
Max consecutive chars: 5
Over 1000 iterations:

Time costs: 35.935 μs ± 0.317 μs
Algorithm efficiency: 0.417 % ± 0 %
  --------------------
  --------------------
ALGORITHM: HUFFMAN Rust
Size of data: 10000000
Max consecutive chars: 1000
Over 10 iterations:

Time costs: 131 ms ± 5.042 ms
Algorithm efficiency: 0.405 % ± 0 %
  --------------------
  --------------------
ALGORITHM: HUFFMAN Rust
Size of data: 100000000
Max consecutive chars: 1000
Over 3 iterations:

Time costs: 1299.667 ms ± 44.784 ms
Algorithm efficiency: 0.404 % ± 0 %
  --------------------
```