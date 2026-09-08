pub const fn varchar_cost(byte_len: usize) -> usize { 2 + byte_len * 6 }
pub const fn uuid_cost() -> usize { 38 } // 36 hex/hyphen chars + 2 quotes
pub const fn i64_cost() -> usize { 20 }  // -9223372036854775808
pub const fn i32_cost() -> usize { 11 }  // -2147483648
pub const fn bool_cost() -> usize { 5 }  // false

pub const BRACES_OVERHEAD: usize = 2;
pub const FIELD_OVERHEAD: usize = 32;
pub const MAX_PAGE_LIMIT: usize = 1000;

pub trait MemorySized: Sized {
    const SIZE: usize = size_of::<Self>();
}

pub trait JsonSized: Sized {
    const JSON_SIZE: usize;
}
