-- PostgreSQL: insert a detailed shipping address into the member receive-address table
-- for verifying display on the order confirmation page (`/order/confirm`).
-- Table/columns map to entity MemberReceiveAddressEntity → ums_member_receive_address.
--
-- Important: the order service filters addresses by logged-in user (only rows where
-- member_id equals the current user). See OrderWorkflowServiceImpl.fetchMemberAddresses(...)
-- (loginUserId.equals(vo.getMemberId())).
-- So member_id below MUST match ums_member.id for the account you use to log in;
-- otherwise the list is empty and the UI falls back to placeholder addresses.
--
-- 1) Look up your member id (adjust WHERE by username / mobile / email as needed):
--    SELECT id, username, mobile, email FROM ums_member ORDER BY id DESC LIMIT 20;
--
-- 2) Replace the literal member_id in the INSERT (example uses 4) with your id from step 1.

-- Optional: remove a previous test row before re-inserting (use with care)
-- DELETE FROM ums_member_receive_address WHERE phone = '13900139000' AND name = 'Test Receiver — Detailed Address';

INSERT INTO ums_member_receive_address (
    member_id,
    name,
    phone,
    post_code,
    province,
    city,
    region,
    detail_address,
    areacode,
    default_status
) VALUES (
    4,                                            -- TODO: set to your ums_member.id
    'Test Receiver — Detailed Address',
    '13900139000',
    '100020',
    'Shanghai',
    'Shanghai',
    'Pudong New Area',
    'No. 1000 Lujiazui Ring Road, HSBC Building, 15/F, Suite 1508 (Deliver Mon–Fri 10:00–18:00; call ahead on weekends; show SMS code if collected at lobby)',
    '310115',
    1
);

-- Another sample receiver (secondary/non-default)
INSERT INTO ums_member_receive_address (
    member_id,
    name,
    phone,
    post_code,
    province,
    city,
    region,
    detail_address,
    areacode,
    default_status
) VALUES (
    4,                                            -- TODO: set to your ums_member.id
    'Alex Johnson',
    '13900139001',
    '518000',
    'Guangdong',
    'Shenzhen',
    'Nanshan District',
    'Room 1206, Block B, Xinghai Plaza, No. 88 Keyuan South Road (Deliver before 19:00; concierge can receive parcels)',
    '440305',
    0
);

-- default_status: 1 = default address (per entity; change if your DB uses the opposite convention)
