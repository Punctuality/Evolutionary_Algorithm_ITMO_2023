use std::ops::RangeInclusive;
use std::time::Duration;
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
        let generated = String::from((&mut rng).sample(AsciiLowercase)).repeat(n_char);
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
    let mut time_costs: Vec<Duration> = Vec::with_capacity(n_iter);
    let mut memory_savings: Vec<f64> = Vec::with_capacity(n_iter);

    for _ in 0..n_iter {
        let data = generate_data(str_len, max_consec_chars);
        let start_time = std::time::Instant::now();
        let transformed = transform_func(&data);
        let end_time = std::time::Instant::now();
        time_costs.push(end_time - start_time);
        memory_savings.push(1.0 - sizing_func(&transformed) as f64 / string_bit_size(&data) as f64);
    }

    let millis = time_costs.iter()
        .map(|dur| dur.as_millis() as f64).collect::<Vec<f64>>();
    let micros = time_costs.iter()
        .map(|dur| dur.as_micros() as f64).collect::<Vec<f64>>();
    let mut mean_time_costs = mean(&millis);
    let is_millis = mean_time_costs != 0.0;
    if !is_millis {
        mean_time_costs = mean(&micros);
    }
    let std_time_costs =
        if is_millis { std(&millis, mean_time_costs) } else { std(&micros, mean_time_costs) };
    let time_notation = if is_millis { "ms" } else { "μs" };
    let mean_memory_savings = mean(&memory_savings);
    let std_memory_savings = std(&memory_savings, mean_memory_savings);

    println!("--------------------\n\
             ALGORITHM: {}\n\
             Size of data: {}\n\
             Max consecutive chars: {}\n\
             Over {} iterations:\n\
             \n\
             Time costs: {} {} ± {} {}\n\
             Algorithm efficiency: {} % ± {} %\n\
             --------------------",
             name,
             str_len,
             max_consec_chars,
             n_iter,
             round_digits(mean_time_costs, 3), time_notation,
             round_digits(std_time_costs, 3), time_notation,
             round_digits(mean_memory_savings, 3), round_digits(std_memory_savings, 3));
}