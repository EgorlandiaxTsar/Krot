#[derive(Debug, PartialEq, Eq)]
pub enum SecurityError {
    FetchFailed,
    WriteFailed,
    DeleteFailed,
    NotFound,
    AlreadyExists,
}
