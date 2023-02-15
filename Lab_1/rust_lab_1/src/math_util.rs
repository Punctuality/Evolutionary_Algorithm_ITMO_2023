pub fn mean(values: &[f64]) -> f64 {
    values.iter().sum::<f64>() / values.len() as f64
}

pub fn std(values: &[f64], mean: f64) -> f64 {
    values.iter().map(|x| (x - mean).powi(2)).sum::<f64>().sqrt() / values.len() as f64
}

pub fn round_digits(n: f64, digits: usize) -> f64 {
    let factor = 10.0_f64.powi(digits as i32);
    (n * factor).round() / factor
}