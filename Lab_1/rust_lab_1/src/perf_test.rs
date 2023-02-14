use std::ops::RangeInclusive;
use rand::prelude::Distribution;
use rand::Rng;
use crate::math_util::{mean, std, round_digits};


struct AsciiLowercase;

const ASCII_LOWERCASE_CHARSET: &[u8; 26] = b"abcdefghijklmnopqrstuvwxyz";

impl Distribution<char> for AsciiLowercase {
    fn sample<R: Rng + ?Sized>(&self, rng: &mut R) -> char {
        loop {
            let var = rng.next_u32() >> (26);
            if var < 26 {
                return ASCII_LOWERCASE_CHARSET[var as usize] as char;
            }
        }
    }
}

fn generate_data(str_len: usize, max_consec_chars: usize) -> String {
    let mut rng = rand::thread_rng();
    let mut acc = String::with_capacity(str_len);
    while acc.len() < str_len {
        let n_char = (&mut rng).gen_range(RangeInclusive::new(1, max_consec_chars));
        let n_char = std::cmp::min(n_char, str_len - acc.len());
        let generated = (&mut rng).sample_iter(AsciiLowercase).take(n_char).collect::<String>();
        acc += &generated;
    }

    acc
}

fn string_bit_size(string: &str) -> usize {
    string.len() * 8
}

pub fn bit_str_repr_size(string: &str) -> usize {
    string.len()
}

pub fn test_time_and_memory(
    name: &str,
    transform_func: &dyn Fn(&str) -> String,
    sizing_func: &dyn Fn(&str) -> usize,
    n_iter: usize, str_len: usize,
    max_consec_chars: usize,
) {
    let mut time_costs = Vec::with_capacity(n_iter);
    let mut memory_savings = Vec::with_capacity(n_iter);

    for _ in 0..n_iter {
        let data = generate_data(str_len, max_consec_chars);
        let start_time = std::time::Instant::now();
        let transformed = transform_func(&data);
        let end_time = std::time::Instant::now();
        time_costs.push((end_time - start_time).as_millis() as f64);
        memory_savings.push(1.0 - sizing_func(&transformed) as f64 / string_bit_size(&data) as f64);
    }

    let mean_time_costs = mean(&time_costs);
    let std_time_costs = std(&time_costs, mean_time_costs);
    let mean_memory_savings = mean(&memory_savings);
    let std_memory_savings = std(&memory_savings, mean_memory_savings);

    println!("--------------------\n\
             ALGORITHM: {}\n\
             Size of data: {}\n\
             Max consecutive chars: {}\n\
             Over {} iterations:\n\
             \n\
             Time costs: {} ms ± {} ms\n\
             Algorithm efficiency: {} % ± {} %\n\
             --------------------",
             name,
             str_len,
             max_consec_chars,
             n_iter,
             round_digits(mean_time_costs, 3), round_digits(std_time_costs, 3),
             round_digits(mean_memory_savings, 3), round_digits(std_memory_savings, 3));
}