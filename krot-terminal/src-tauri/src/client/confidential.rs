pub const SESSION_SIZE: usize = size_of::<Session>();
pub const CREDENTIALS_SIZE: usize = size_of::<Credentials>();

#[repr(C)]
#[derive(Default, Debug, Copy, Clone)]
pub struct Session {
    pub id: [u8; 16],
    pub reference_key: [u8; 16],
    pub encryption_key: [u8; 32],
    pub expiration: i64,
}

#[repr(C)]
#[derive(Debug, Copy, Clone)]
pub struct Credentials {
    pub name: [u8; 128],
    pub pwd: [u8; 128],
    pub addr: [u8; 4],
    pub port: u16,
    pub secured: bool,
}

impl Default for Credentials {
    fn default() -> Self {
        Self {
            name: [0u8; 128],
            pwd: [0u8; 128],
            addr: [0u8; 4],
            port: 0,
            secured: false,
        }
    }
}
