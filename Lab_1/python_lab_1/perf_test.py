from time import time_ns
import numpy as np
def generate_data(str_len: int, max_consec_chars: int):
    import random
    import string

    acc = ''
    while len(acc) < str_len:
        n_char = random.randint(1, max_consec_chars)
        n_char = min(n_char, str_len - len(acc))
        generated = random.choice(string.ascii_lowercase) * n_char
        acc += generated

    return acc


def string_bit_size(string):
    return len(string.encode('ascii')) * 8


def bit_str_repr_size(string):
    return len(string)


def test_time_and_memory(name, transform_func, sizing_func, n_iter=100, str_len=1000, max_consec_chars=10):
    time_costs = []
    memory_savings = []

    for _ in range(n_iter):
        data = generate_data(str_len, max_consec_chars)
        start_time = time_ns()
        transformed = transform_func(data)
        end_time = time_ns()
        time_costs.append((end_time - start_time) / 1e6)
        memory_savings.append(1 - sizing_func(transformed) / string_bit_size(data))

    print(
        f"""--------------------
ALGORITHM: {name}
Size of data: {str_len}
Max consecutive chars: {max_consec_chars}
Over {n_iter} iterations:
        
Time costs: {np.mean(time_costs).round(3)} ms ± {np.std(time_costs).round(3)} ms
Algorithm efficiency: {np.mean(memory_savings).round(3)} % ± {np.std(memory_savings).round(3)} %
--------------------""")

    return time_costs, memory_savings

