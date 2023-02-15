### Вывод запуска `python3 main.py`
```yaml
Test string: aaaaabbbbbccccccdddddddeeeeeeeeeeeeeffsrt
Test codes:
d -> 00
f -> 0100
s -> 01010
t -> 010110
r -> 010111
b -> 011
a -> 100
c -> 101
e -> 11
Test encoding: 10010010010010001101101101101110110110110110110100000000000000111111111111111111111111110100010001010010111010110

--------------------
ALGORITHM: HUFFMAN Python naive
Size of data: 1000
Max consecutive chars: 5
Over 1000 iterations:
        
Time costs: 0.179 ms ± 0.046 ms
Algorithm efficiency: 0.417 % ± 0.003 %
--------------------
--------------------
ALGORITHM: HUFFMAN Python optimized
Size of data: 1000
Max consecutive chars: 5
Over 1000 iterations:
        
Time costs: 0.161 ms ± 0.036 ms
Algorithm efficiency: 0.417 % ± 0.003 %
--------------------
--------------------
ALGORITHM: HUFFMAN Python naive
Size of data: 10000000
Max consecutive chars: 1000
Over 10 iterations:
        
Time costs: 825.5 ms ± 17.288 ms
Algorithm efficiency: 0.405 % ± 0.0 %
--------------------
--------------------
ALGORITHM: HUFFMAN Python optimized
Size of data: 10000000
Max consecutive chars: 1000
Over 10 iterations:
        
Time costs: 839.834 ms ± 13.312 ms
Algorithm efficiency: 0.405 % ± 0.0 %
--------------------
--------------------
ALGORITHM: HUFFMAN Python naive
Size of data: 100000000
Max consecutive chars: 1000
Over 3 iterations:
        
Time costs: 8524.109 ms ± 44.272 ms
Algorithm efficiency: 0.404 % ± 0.0 %
--------------------
--------------------
ALGORITHM: HUFFMAN Python optimized
Size of data: 100000000
Max consecutive chars: 1000
Over 3 iterations:
        
Time costs: 8485.963 ms ± 100.24 ms
Algorithm efficiency: 0.404 % ± 0.0 %
--------------------
```