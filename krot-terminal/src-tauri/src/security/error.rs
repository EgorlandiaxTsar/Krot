use serde::Serialize;

#[derive(Debug, PartialEq, Eq, Serialize)]
pub enum SecurityError {
    FetchFailed,
    WriteFailed,
    DeleteFailed,
    NotFound,
    AlreadyExists,
}
