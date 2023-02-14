Run tests:
```yaml
Test string: aaaaabbbbbccccccdddddddeeeeeeeeeeeeeffsrt
Test codes:
t: 01101
c: 101
d: 00
f: 01111
b: 010
r: 01110
s: 01100
a: 100
e: 11
Encoded: 10010010010010001001001001001010110110110110110100000000000000111111111111111111111111110111101111011000111001101
  --------------------
ALGORITHM: HUFFMAN Rust
Size of data: 1000
Max consecutive chars: 5
Over 1000 iterations:

Time costs: 0 ms ± 0 ms
Algorithm efficiency: 0.41 % ± 0 %
  --------------------
  --------------------
ALGORITHM: HUFFMAN Rust
Size of data: 10000000
Max consecutive chars: 1000
Over 10 iterations:

Time costs: 283.8 ms ± 5.176 ms
Algorithm efficiency: 0.404 % ± 0 %
  --------------------
  --------------------
ALGORITHM: HUFFMAN Rust
Size of data: 100000000
Max consecutive chars: 1000
Over 3 iterations:

Time costs: 2716.667 ms ± 35.253 ms
Algorithm efficiency: 0.404 % ± 0 %
  --------------------
```