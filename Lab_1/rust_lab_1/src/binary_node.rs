use std::cmp::Ordering;
use std::fmt::{Display, Formatter};
use typed_arena::Arena;

#[derive(Clone, Debug, Hash)]
pub struct BinaryNode<'g, T> {
    pub value: Option<T>,
    pub weight: usize,
    pub left: Option<&'g BinaryNode<'g, T>>,
    pub right: Option<&'g BinaryNode<'g, T>>,
}

// Basic operations
impl<'g, T> BinaryNode<'g, T> {
    pub fn is_leaf(&self) -> bool {
        self.value.is_some() && self.left.is_none() && self.right.is_none()
    }

    pub fn new(
        value: Option<T>,
        weight: usize,
        left: Option<&'g BinaryNode<'g, T>>,
        right: Option<&'g BinaryNode<'g, T>>,
        arena: &'g Arena<BinaryNode<'g, T>>
    ) -> &'g BinaryNode<'g, T> {
        arena.alloc(BinaryNode { value, weight, left, right })
    }

    pub fn parent(
        left: &'g BinaryNode<'g, T>,
        right: &'g BinaryNode<'g, T>,
        arena: &'g Arena<BinaryNode<'g, T>>
    ) -> &'g BinaryNode<'g, T> {
        Self::new (None, left.weight + right.weight, Some(left), Some(right), arena)
    }

    pub fn leaf(
        value: T,
        weight: usize,
        arena: &'g Arena<BinaryNode<'g, T>>
    ) -> &'g BinaryNode<'g, T> {
        Self::new ( Some(value), weight, None, None, arena)
    }
}

// Define a Display trait for BinaryNode
impl<'g, T: Display> Display for BinaryNode<'g, T> {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "Node({}, {}, {}, {})",
               self.value.as_ref().map(|value| value.to_string()).unwrap_or("None".to_string()),
               self.weight,
               self.left.map(|left| left.to_string()).unwrap_or("None".to_string()),
               self.right.map(|right| right.to_string()).unwrap_or("None".to_string())
        )
    }
}


// Define custom PartialOrd for BinaryNode
impl<'g, T> Eq for BinaryNode<'g, T> {}

impl<'g, T> PartialEq<Self> for BinaryNode<'g, T> {
    fn eq(&self, other: &Self) -> bool {
        self.weight == other.weight
    }
}

impl<'g, T> PartialOrd<Self> for BinaryNode<'g, T> {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        self.weight.partial_cmp(&other.weight)
    }
}

impl<'g, T> Ord for BinaryNode<'g, T> {
    fn cmp(&self, other: &Self) -> Ordering {
        self.weight.cmp(&other.weight)
    }
}