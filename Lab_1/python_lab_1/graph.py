class BinaryNode:
    def __init__(self, value, weight=0, left=None, right=None):
        self.value = value
        self.weight = weight
        self.left = left
        self.right = right

    @property
    def is_leaf(self):
        return self.value is not None and self.left is None and self.right is None

    def children(self):
        return self.left, self.right

    def __repr__(self):
        return f"BinaryNode({self.value}, {self.weight}, {self.left}, {self.right})"

    # Redefine comparison operator
    def __lt__(self, other):
        return self.weight < other.weight

    def __le__(self, other):
        return self.weight <= other.weight

    def __gt__(self, other):
        return self.weight > other.weight

    def __ge__(self, other):
        return self.weight >= other.weight

    def __eq__(self, other):
        return self.weight == other.weight

    def __ne__(self, other):
        return self.weight != other.weight

